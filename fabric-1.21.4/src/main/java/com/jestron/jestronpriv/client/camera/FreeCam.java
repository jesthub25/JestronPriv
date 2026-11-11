package com.jestron.jestronpriv.client.camera;

import com.jestron.jestronpriv.client.JestronPrivClient;
import com.jestron.jestronpriv.client.ModConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Serbest 3. sahis kamera (F5 arka gorunum).
 * <ul>
 *   <li>Fare kamerayi dondurur, oyuncuyu degil: durunca kamera oyuncunun etrafinda serbestce doner.</li>
 *   <li>Yururken hareket kameraya goredir (W = kameranin baktigi yon); oyuncu yumusakca gittigi yone doner.</li>
 *   <li>Vurunca / kullaninca oyuncu once kameranin nisan aldigi noktaya doner, sonra etkilesir
 *       (blok kirma, saldiri, ok, yemek...). Kisa bir sure o yone bakmaya devam eder.</li>
 *   <li>Elytra ile ucarken oyuncu yumusakca kameranin baktigi yone doner (istege bagli).</li>
 * </ul>
 * Birinci sahisa donunce oyuncu kameranin baktigi yone bakar (gecis kesintisiz). Binek ustunde,
 * uyurken, izleyici modunda ve baska varliga bakarken devre disidir.
 */
public final class FreeCam {

    private static float camYaw, camPitch;
    private static boolean wasActive, pausedByLook;
    private static int aimTicks;

    private FreeCam() {
    }

    private static ModConfig cfg() {
        JestronPrivClient mod = JestronPrivClient.get();
        return mod == null ? null : mod.config();
    }

    /** Su an serbest kamera calisiyor mu (gecis senkronu yapmaz). Bas-bak sirasinda duraklar. */
    public static boolean active() {
        return !FreeLook.busy() && activeIgnoringLook();
    }

    /** Bas-bak olmasaydi serbest kamera calisir miydi (bas-bak donus hedefi icin). */
    public static boolean activeIgnoringLook() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        ModConfig c = cfg();
        if (p == null || mc.level == null || c == null || !c.freeCam) return false;
        if (mc.options.getCameraType() != CameraType.THIRD_PERSON_BACK) return false;
        if (mc.getCameraEntity() != p) return false;
        if (p.isPassenger() || p.isSleeping() || !p.isAlive() || p.isSpectator()) return false;
        return !p.isFallFlying() || c.freeCamElytra;
    }

    /** Aktiflik degisimlerini yakalar; aktifse true. Kamera, girdi ve fare yolunda cagrilir. */
    public static boolean sync() {
        boolean a = active();
        if (a != wasActive) {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer p = mc.player;
            if (p != null) {
                if (a) {
                    // bas-bak sonrasi: kamera kaldigi yerden devam eder
                    if (!pausedByLook) {
                        camYaw = p.getYRot();
                        camPitch = p.getXRot();
                    }
                    pausedByLook = false;
                    aimTicks = 0;
                } else if (FreeLook.busy()) {
                    pausedByLook = true;
                } else if (mc.options.getCameraType().isFirstPerson() && !p.isPassenger() && p.isAlive()) {
                    // F5 ile 1. sahisa donus: oyuncu kameranin baktigi yone bakar (gorus kesintisiz).
                    // Diger cikislarda (elytra, binek, olum...) oyuncunun yonune dokunulmaz.
                    setRotation(p, camYaw, camPitch, true);
                }
            }
            wasActive = a;
        }
        return a;
    }

    /** 3. sahista ortada nisangah gosterilsin mi (nisan alma acikken anlamli). */
    public static boolean showCrosshair() {
        ModConfig c = cfg();
        return c != null && c.freeCamCrosshair && c.freeCamAim && active();
    }

    public static float yaw() {
        return camYaw;
    }

    public static float pitch() {
        return camPitch;
    }

    /** Fare hareketi (Entity.turn ile ayni olcek). true donerse oyuncu donmez. */
    public static boolean onTurn(double dYaw, double dPitch) {
        if (!sync()) return false;
        camYaw += (float) dYaw * 0.15f;
        camPitch = clamp(camPitch + (float) dPitch * 0.15f, -90f, 90f);
        if (camYaw > 36000f || camYaw < -36000f) camYaw = wrap(camYaw);
        return true;
    }

    // ------------------------------------------------------------------ hareket (her oyuncu tick'i)

    /** KeyboardInput.tick sonunda: hareketi kameraya gore cevirir, oyuncuyu dondurur. */
    public static void onInput(ClientInput in) {
        if (!sync()) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        ModConfig c = cfg();
        if (p == null || c == null) return;
        float l = in.leftImpulse, f = in.forwardImpulse;
        boolean moving = Math.abs(l) > 1e-4f || Math.abs(f) > 1e-4f;
        float turn = Math.max(0.05f, Math.min(1f, c.freeCamTurnSpeed));

        if (aimTicks > 0) aimTicks--;
        boolean holding = mc.options.keyAttack.isDown() || (mc.options.keyUse.isDown() && p.isUsingItem());
        if (c.freeCamAim && (holding || aimTicks > 0)) {
            // nisan: oyuncu kameranin hedefine bakar, hareket yine kameraya gore
            if (holding) aimTicks = Math.max(aimTicks, 2);
            aimAtCamera(p, false);
            if (moving) remap(in, l, f, p.getYRot());
            return;
        }
        if (p.isFallFlying()) {
            // elytra: yumusak dumen, kameranin baktigi yone
            float ny = p.getYRot() + wrap(camYaw - p.getYRot()) * turn;
            float np = p.getXRot() + (camPitch - p.getXRot()) * turn;
            setRotation(p, ny, np, false);
            return;
        }
        if (!moving) return;
        float desired = camYaw - (float) Math.toDegrees(Math.atan2(l, f));
        float ny = p.getYRot() + wrap(desired - p.getYRot()) * turn;
        float np = p.getXRot() + (0f - p.getXRot()) * Math.min(1f, turn * 0.6f);
        setRotation(p, ny, np, false);
        remap(in, l, f, ny);
    }

    /**
     * Kameraya gore istenen yonu, oyuncunun su anki bakisina gore ileri/yan TUS girdisine cevirir.
     * <p>
     * 1.21.2+ istemci basili tuslari (ileri/geri/sol/sag) sunucuya ayrica bildirir; hareket simulasyonu
     * yapan anti-cheat'ler bu tuslarla oyuncunun bakis yonunden hareketi yeniden hesaplar. Bu yuzden girdi
     * analog (cos/sin) degil, vanilla klavyedeki gibi 8 yonden birine (-1/0/1) yuvarlanir ve sunucuya giden
     * tus bilgisi de buna esitlenir: sunucu tam olarak "bu tuslara basan normal bir oyuncu" gorur, geri
     * cekme (takilma / yurüyememe) olmaz. Oyuncu her tick istenen yone dondugu icin sapma hizla sifirlanir.
     */
    private static void remap(ClientInput in, float l, float f, float playerYaw) {
        float desired = camYaw - (float) Math.toDegrees(Math.atan2(l, f));
        float delta = wrap(desired - playerYaw);
        // 45 derecelik dilime yuvarla (8 yon)
        int sector = Math.round(delta / 45f);
        double a = Math.toRadians(sector * 45.0);
        double c = Math.cos(a), sn = -Math.sin(a);
        float fq = c > 0.3 ? 1f : (c < -0.3 ? -1f : 0f);
        float lq = sn > 0.3 ? 1f : (sn < -0.3 ? -1f : 0f);
        in.forwardImpulse = fq;
        in.leftImpulse = lq;
        Input k = in.keyPresses == null ? Input.EMPTY : in.keyPresses;
        in.keyPresses = new Input(fq > 0f, fq < 0f, lq > 0f, lq < 0f, k.jump(), k.shift(), k.sprint());
    }

    // ------------------------------------------------------------------ nisan (etkilesim)

    /** Saldiri / kullanma oncesi: oyuncuyu kameranin hedefine dondurur ve hedefi yeniler. */
    public static void beforeInteract() {
        if (!sync()) return;
        ModConfig c = cfg();
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (c == null || !c.freeCamAim || p == null) return;
        if (aimAtCamera(p, true)) {
            if (mc.gameRenderer != null) mc.gameRenderer.pick(1.0f);
        }
        aimTicks = Math.max(aimTicks, Math.round(Math.max(0.25f, Math.min(3f, c.freeCamAimTime)) * 20f));
    }

    /** Kamera isininin degdigi noktaya bakar. sendNow: donusu etkilesimden once sunucuya bildir. */
    private static boolean aimAtCamera(LocalPlayer p, boolean sendNow) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameRenderer == null || mc.level == null) return false;
        Camera cam = mc.gameRenderer.getMainCamera();
        Vec3 cpos = cam.getPosition();
        if (cpos == null) return false;
        Vec3 dir = direction(camYaw, camPitch);
        Vec3 eye = p.getEyePosition();
        double reach = Math.max(p.blockInteractionRange(), p.entityInteractionRange());
        double len = Math.max(cpos.distanceTo(eye) + reach + 1.5, 64.0);
        Vec3 end = cpos.add(dir.x * len, dir.y * len, dir.z * len);
        Vec3 target = end;
        BlockHitResult bh = mc.level.clip(new ClipContext(cpos, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, p));
        if (bh != null && bh.getType() != HitResult.Type.MISS) target = bh.getLocation();
        AABB box = new AABB(cpos, target).inflate(1.0);
        EntityHitResult eh = ProjectileUtil.getEntityHitResult(p, cpos, target, box,
                e -> e != p && e != p.getVehicle() && !e.isSpectator() && e.isPickable(), cpos.distanceToSqr(target));
        if (eh != null) target = eh.getLocation();
        double dx = target.x - eye.x, dy = target.y - eye.y, dz = target.z - eye.z;
        double h = Math.sqrt(dx * dx + dz * dz);
        if (h * h + dy * dy < 0.25) return false;          // hedef oyuncunun icinde: dokunma
        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = clamp((float) -Math.toDegrees(Math.atan2(dy, h)), -90f, 90f);
        // aci farki: oyuncunun mevcut acisina en yakin esdeger (360 sicramasi olmasin)
        yaw = p.getYRot() + wrap(yaw - p.getYRot());
        boolean changed = Math.abs(wrap(yaw - p.getYRot())) > 0.5f || Math.abs(pitch - p.getXRot()) > 0.5f;
        setRotation(p, yaw, pitch, false);
        if (sendNow && changed) {
            ClientPacketListener con = mc.getConnection();
            if (con != null) con.send(new ServerboundMovePlayerPacket.Rot(yaw, pitch, p.onGround(), p.horizontalCollision));
        }
        return true;
    }

    // ------------------------------------------------------------------ yardimcilar

    private static void setRotation(LocalPlayer p, float yaw, float pitch, boolean snap) {
        p.setYRot(yaw);
        p.setXRot(clamp(pitch, -90f, 90f));
        p.setYHeadRot(yaw);
        if (snap) {
            p.yRotO = p.getYRot();
            p.xRotO = p.getXRot();
        }
    }

    static Vec3 direction(float yaw, float pitch) {
        double y = Math.toRadians(yaw), x = Math.toRadians(pitch);
        double cx = Math.cos(x);
        return new Vec3(-Math.sin(y) * cx, -Math.sin(x), Math.cos(y) * cx);
    }

    static float wrap(float deg) {
        float d = deg % 360f;
        if (d >= 180f) d -= 360f;
        if (d < -180f) d += 360f;
        return d;
    }

    private static float clamp(float v, float a, float b) {
        return v < a ? a : (v > b ? b : v);
    }
}
