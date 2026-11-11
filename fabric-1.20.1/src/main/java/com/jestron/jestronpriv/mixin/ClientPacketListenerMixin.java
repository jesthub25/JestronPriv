package com.jestron.jestronpriv.mixin;

import com.jestron.jestronpriv.client.pvp.PvpHub;
import com.jestron.jestronpriv.client.pvp.TotemFx;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Hasar ve varlik olaylari (sona sadece istemci ana thread'inde ulasilir). */
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

    @Inject(method = "handleDamageEvent", at = @At("TAIL"))
    private void jestronpriv$damage(ClientboundDamageEventPacket packet, CallbackInfo ci) {
        try {
            PvpHub.onDamageEvent(packet.entityId(), packet.sourceCauseId(), packet.sourceDirectId());
        } catch (Throwable ignored) {
            // paket isleyicisinde hata baglantiyi koparmasin
        }
    }

    /** Totem paketi islenmeden once: ozel ses kullanilacaksa vanilla totem sesi susturulur. */
    @Inject(method = "handleEntityEvent", at = @At("HEAD"))
    private void jestronpriv$entityEventHead(ClientboundEntityEventPacket packet, CallbackInfo ci) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (!mc.isSameThread() || mc.level == null || packet.getEventId() != 35) return;
            com.jestron.jestronpriv.client.audio.CustomSound.armMute(packet.getEntity(mc.level));
        } catch (Throwable ignored) {
        }
    }

    /** Varlik olayi: 35 = totem patladi (vanilla isledikten sonra, ana thread'de). */
    @Inject(method = "handleEntityEvent", at = @At("TAIL"))
    private void jestronpriv$entityEvent(ClientboundEntityEventPacket packet, CallbackInfo ci) {
        try {
            if (packet.getEventId() != 35) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;
            com.jestron.jestronpriv.client.audio.CustomSound.disarmMute();
            TotemFx.onPop(packet.getEntity(mc.level));
        } catch (Throwable ignored) {
        }
    }

    /** Ping yaniti (ag thread'inde gelir; PingTracker kilitli calisir). */
    @Inject(method = "handlePongResponse", at = @At("HEAD"))
    private void jestronpriv$pong(net.minecraft.network.protocol.ping.ClientboundPongResponsePacket packet, CallbackInfo ci) {
        try {
            com.jestron.jestronpriv.client.pvp.PingTracker.onPong(packet.time());
        } catch (Throwable ignored) {
        }
    }

    /** Yerden alinan esya (sunucu paketi, tam adet). Ana is parcacigindaki ikinci cagrida calisir. */
    @Inject(method = "handleTakeItemEntity", at = @At("HEAD"))
    private void jestronpriv$take(net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket packet, CallbackInfo ci) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (!mc.isSameThread() || mc.level == null || mc.player == null) return;
            if (packet.getPlayerId() != mc.player.getId()) return;
            if (mc.level.getEntity(packet.getItemId()) instanceof net.minecraft.world.entity.item.ItemEntity ie) {
                com.jestron.jestronpriv.client.hud.PickupHud.onPickup(ie.getItem(), Math.min(packet.getAmount(), ie.getItem().getCount()));
            }
        } catch (Throwable ignored) {
        }
    }
}
