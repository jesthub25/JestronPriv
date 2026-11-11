package com.jestron.jestronpriv.client.itemskin;

import com.jestron.jestronpriv.client.JestronPrivClient;
import com.jestron.jestronpriv.client.ModConfig;
import com.jestron.jestronpriv.itemskin.SkinAnim;
import com.jestron.jestronpriv.itemskin.SkinImage;
import com.jestron.jestronpriv.itemskin.SkinMesh;
import com.jestron.jestronpriv.mixin.ItemStackRenderStateAccessor;
import com.jestron.jestronpriv.mixin.LayerRenderStateAccessor;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Esya gorunumleri: istedigin esyanin dokusunu kendin cizersin ya da bir PNG / kaynak paketinden yuklersin; sadece
 * senin ekraninda degisir (istemci). Gorunumler ayar dosyasinda metin olarak durur, bu yuzden ayar profiliyle /
 * paylasim koduyla arkadasina aynen gecer.
 * <p>
 * Cizim: esya modeli cozumlendikten sonra katmanlari tek bir ozel katmanla degistirilir. Konum / donus (elde, yerde,
 * cercevede, GUI'de) esyanin kendi duz modelinden, blok esyalarinda duz bir esyanin (cubuk) modelinden alinir.
 * Buyulu esyalarda parlama korunur. Doku ve model bir kez uretilir, degismedikce yeniden uretilmez.
 */
public final class ItemSkins {

    /**
     * Bir gorunum. Animasyonda kareler tek dokuda alt alta durur, cizimde o anki karenin dilimi kullanilir. Doku GPU'ya
     * ilk cizimde yuklenir; her karenin modeli ilk gerektiginde uretilir.
     */
    public static final class Skin {
        final String key;
        SkinAnim anim;
        final ResourceLocation tex;
        DynamicTexture dt;
        int texW, texH;
        SkinMesh[] meshes;
        boolean broken;
        long brokenAt;
        int fails;
        /** Onizleme: degisen kareler (yeniden yukle + model). */
        boolean dirtyAll;
        final java.util.BitSet dirty = new java.util.BitSet();

        Skin(String key, SkinAnim anim, ResourceLocation tex) {
            this.key = key;
            this.anim = anim;
            this.tex = tex;
            this.meshes = new SkinMesh[anim.count()];
        }
    }

    private static final Map<Item, Skin> SKINS = new IdentityHashMap<>();
    private static final Map<String, Skin> BY_CODE = new HashMap<>();
    private static Map<String, String> source;
    private static int sourceSize = -1;
    private static boolean dirty = true;
    /** Duzenleyicide animasyon duraklatilmissa gosterilecek kare (-1: oynat). */
    private static int previewFrame = -1;
    private static int texCounter;

    // duzenleyici onizlemesi
    private static Item previewItem;
    private static Skin preview;

    private ItemSkins() {
    }

    private static ModConfig cfg() {
        JestronPrivClient m = JestronPrivClient.get();
        return m == null ? null : m.config();
    }

    /** Ayar degisti (duzenleyici kaydetti / profil yuklendi). */
    public static void invalidate() {
        dirty = true;
    }

    public static String idOf(Item item) {
        ResourceLocation k = BuiltInRegistries.ITEM.getKey(item);
        return k == null ? "" : k.toString();
    }

    public static Item itemOf(String id) {
        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl == null || !BuiltInRegistries.ITEM.containsKey(rl)) return null;
        Item it = BuiltInRegistries.ITEM.getValue(rl);
        return it == Items.AIR ? null : it;
    }

    private static void sync(ModConfig c) {
        Map<String, String> m = c.itemSkins;
        if (!dirty && m == source && m.size() == sourceSize) return;
        dirty = false;
        source = m;
        sourceSize = m.size();
        Map<String, Skin> keep = new HashMap<>();
        SKINS.clear();
        for (Map.Entry<String, String> e : m.entrySet()) {
            Item it = itemOf(e.getKey());
            if (it == null || e.getValue() == null) continue;
            Skin s = BY_CODE.get(e.getValue());
            if (s == null) {
                SkinAnim anim = SkinAnim.decode(e.getValue());
                if (anim == null) continue;
                s = new Skin(e.getValue(), anim, ResourceLocation.fromNamespaceAndPath("jestronpriv", "dynamic/item_skin_" + (texCounter++)));
            }
            keep.put(e.getValue(), s);
            SKINS.put(it, s);
        }
        // artik kullanilmayan dokulari birak
        for (Map.Entry<String, Skin> e : BY_CODE.entrySet()) if (!keep.containsKey(e.getKey())) release(e.getValue());
        BY_CODE.clear();
        BY_CODE.putAll(keep);
    }

    private static void release(Skin s) {
        if (s == null || s.dt == null) return;
        try {
            Minecraft.getInstance().getTextureManager().release(s.tex);
        } catch (Throwable ignored) {
        }
        s.dt = null;
    }

    /** Bu esyanin gorunumu (yoksa null). */
    public static Skin skinFor(Item item) {
        if (item == null) return null;
        if (preview != null && item == previewItem) return preview;
        ModConfig c = cfg();
        if (c == null || !c.itemSkinsEnabled || c.itemSkins == null || c.itemSkins.isEmpty()) return null;
        sync(c);
        return SKINS.get(item);
    }

    public static boolean has(Item item) {
        ModConfig c = cfg();
        return c != null && c.itemSkins != null && c.itemSkins.containsKey(idOf(item));
    }

    // ------------------------------------------------------------------ duzenleyici onizlemesi

    /**
     * Duzenleyicide cizilen animasyonu bu esyada canli goster (null = kapat).
     *
     * @param changedFrame degisen kare (-1: hepsi / yapi degisti, -2: pikseller ayni, sadece sure / secim)
     */
    public static void setPreview(Item item, SkinAnim anim, int changedFrame) {
        if (item == null || anim == null) {
            if (preview != null) release(preview);
            preview = null;
            previewItem = null;
            previewFrame = -1;
            return;
        }
        boolean same = preview != null && previewItem == item && preview.anim.count() == anim.count() && preview.anim.w == anim.w;
        if (!same) {
            if (preview != null) release(preview);
            preview = new Skin("preview", anim, ResourceLocation.fromNamespaceAndPath("jestronpriv", "dynamic/item_skin_preview"));
            previewItem = item;
            preview.dirtyAll = true;
            return;
        }
        preview.anim = anim;
        if (changedFrame == -1) preview.dirtyAll = true;
        else if (changedFrame >= 0 && changedFrame < anim.count()) preview.dirty.set(changedFrame);
    }

    public static void setPreviewFrame(int frame) {
        previewFrame = frame;
    }

    // ------------------------------------------------------------------ model cozumleme kancasi

    private static final ItemStackRenderState[] STATES = new ItemStackRenderState[32];
    private static final int[] STARTS = new int[32];
    private static int depth;
    private static boolean inFlat;
    private static BakedModel flatModel;

    /** appendItemLayers basi: katman sayisini hatirla. */
    public static void beginAppend(ItemStackRenderState st) {
        if (depth >= STATES.length) depth = 0; // bozulmus yigin: guvenle sifirla
        STATES[depth] = st;
        STARTS[depth] = ((ItemStackRenderStateAccessor) st).jestronpriv$count();
        depth++;
    }

    /** appendItemLayers sonu: bu esyanin gorunumu varsa eklenen katmanlari ozel katmanla degistir. */
    public static void endAppend(ItemStackRenderState st, ItemStack stack, ItemModelResolver resolver) {
        int start = -1;
        while (depth > 0) {
            depth--;
            ItemStackRenderState s = STATES[depth];
            STATES[depth] = null;
            if (s == st) {
                start = STARTS[depth];
                break;
            }
        }
        if (start < 0 || inFlat || stack == null || stack.isEmpty()) return;
        Skin skin = skinFor(stack.getItem());
        // hatali doku beklerken vanilla gorunum kalir (gorunmez esya olmaz)
        if (skin == null || (skin.broken && (skin.fails >= 3 || Util.getMillis() - skin.brokenAt < 5000L))) return;
        ItemStackRenderStateAccessor acc = (ItemStackRenderStateAccessor) st;
        int count = acc.jestronpriv$count();
        if (count <= start) return;
        ItemStackRenderState.LayerRenderState[] layers = acc.jestronpriv$layers();
        BakedModel m = ((LayerRenderStateAccessor) layers[start]).jestronpriv$model();
        BakedModel tm = m != null && !m.usesBlockLight() ? m : flat(resolver);
        if (tm == null) return;
        for (int i = start; i < count; i++) layers[i].clear();
        acc.jestronpriv$setCount(start);
        ItemStackRenderState.LayerRenderState l = st.newLayer();
        l.setupSpecialModel(RENDERER, skin, tm);
        l.setFoilType(stack.hasFoil() ? ItemStackRenderState.FoilType.STANDARD : ItemStackRenderState.FoilType.NONE);
    }

    /** Duz bir esyanin (cubuk) modeli: blok esyasina cizilen duz gorunum elde / yerde dogru dursun. */
    private static BakedModel flat(ItemModelResolver resolver) {
        if (flatModel != null) return flatModel;
        inFlat = true;
        try {
            ItemStackRenderState tmp = new ItemStackRenderState();
            resolver.appendItemLayers(tmp, new ItemStack(Items.STICK), ItemDisplayContext.GUI, null, null, 0);
            ItemStackRenderStateAccessor acc = (ItemStackRenderStateAccessor) tmp;
            if (acc.jestronpriv$count() > 0) flatModel = ((LayerRenderStateAccessor) acc.jestronpriv$layers()[0]).jestronpriv$model();
        } catch (Throwable ignored) {
        } finally {
            inFlat = false;
        }
        return flatModel;
    }

    /** Kaynaklar yeniden yuklendi: modeller degisti. */
    public static void onResourceReload() {
        flatModel = null;
    }

    // ------------------------------------------------------------------ cizim

    private static final Vector3f TP = new Vector3f(), TN = new Vector3f(), TN2 = new Vector3f();

    private static boolean ready(Skin s) {
        if (s.broken) {
            // gecici hata (kaynak yeniden yukleme vb.): kalici kapatma yok, biraz sonra yeniden dene
            if (s.fails >= 3 || Util.getMillis() - s.brokenAt < 5000L) return false;
            s.broken = false;
            release(s);
            s.dirtyAll = true;
        }
        try {
            SkinAnim a = s.anim;
            int tw = a.w, th = a.h * a.count();
            if (s.dt == null || s.texW != tw || s.texH != th) {
                if (s.dt != null) release(s);
                s.dt = new DynamicTexture(tw, th, false);
                s.texW = tw;
                s.texH = th;
                Minecraft.getInstance().getTextureManager().register(s.tex, s.dt);
                s.dirtyAll = true;
            }
            if (s.meshes.length != a.count()) {
                s.meshes = new SkinMesh[a.count()];
                s.dirtyAll = true;
            }
            if (s.dirtyAll || !s.dirty.isEmpty()) {
                NativeImage ni = s.dt.getPixels();
                if (ni == null) throw new IllegalStateException("doku");
                for (int f = 0; f < a.count(); f++) {
                    if (!s.dirtyAll && !s.dirty.get(f)) continue;
                    SkinImage img = a.frames.get(f);
                    int oy = f * a.h;
                    for (int y = 0; y < a.h; y++) for (int x = 0; x < a.w; x++) ni.setPixel(x, oy + y, img.argb[y * a.w + x]);
                    s.meshes[f] = null;
                }
                s.dt.upload();
                s.dirtyAll = false;
                s.dirty.clear();
            }
            return true;
        } catch (Throwable t) {
            s.broken = true;
            s.brokenAt = Util.getMillis();
            s.fails++;
            return false;
        }
    }

    private static int frameOf(Skin s) {
        if (s == preview && previewFrame >= 0) return Math.min(previewFrame, s.anim.count() - 1);
        return s.anim.frameAt(Util.getMillis());
    }

    static final SpecialModelRenderer<Skin> RENDERER = new SpecialModelRenderer<>() {
        @Override
        public void render(Skin s, ItemDisplayContext ctx, PoseStack ps, MultiBufferSource buffers, int light, int overlay, boolean foil) {
            if (s == null || !ready(s)) return;
            int f = frameOf(s);
            SkinMesh mesh = s.meshes[f];
            if (mesh == null) mesh = s.meshes[f] = SkinMesh.build(s.anim.frames.get(f));
            int n = s.anim.count();
            float v0 = (float) f / n, vs = 1f / n;
            RenderType rt = RenderType.itemEntityTranslucentCull(s.tex);
            VertexConsumer vc = foil ? ItemRenderer.getFoilBuffer(buffers, rt, true, true) : buffers.getBuffer(rt);
            PoseStack.Pose pose = ps.last();
            Matrix4f m = pose.pose();
            float[] d = mesh.data;
            int ov = overlay;
            for (int q = 0; q < mesh.quads; q++) {
                int o = q * SkinMesh.STRIDE;
                Vector3f nn = pose.transformNormal(TN.set(d[o + 20], d[o + 21], d[o + 22]), TN2);
                float nx = nn.x(), ny = nn.y(), nz = nn.z();
                for (int i = 0; i < 4; i++) {
                    int k = o + i * 5;
                    Vector3f p = m.transformPosition(d[k], d[k + 1], d[k + 2], TP);
                    vc.addVertex(p.x(), p.y(), p.z(), 0xFFFFFFFF, d[k + 3], v0 + d[k + 4] * vs, ov, light, nx, ny, nz);
                }
            }
        }

        @Override
        public Skin extractArgument(ItemStack stack) {
            return stack == null ? null : skinFor(stack.getItem());
        }
    };
}
