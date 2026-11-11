package com.jestron.jestronpriv.client.look;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;

/**
 * Gokyuzu efektleri icin cizim turu: konum + renk, toplamali isik karisimi (yildirim gibi), derinlige yazmaz, arka
 * yuz atilmaz. Vanilla'nin kendi durum parcalarindan kurulur.
 */
final class SkyRenderType extends RenderType {

    private SkyRenderType(String name, Runnable setup, Runnable clear) {
        super(name, DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 4096, false, false, setup, clear);
    }

    static final RenderType GLOW = new SkyRenderType("jestronpriv_sky_glow", () -> {
        POSITION_COLOR_SHADER.setupRenderState();
        LIGHTNING_TRANSPARENCY.setupRenderState();
        LEQUAL_DEPTH_TEST.setupRenderState();
        NO_CULL.setupRenderState();
        COLOR_WRITE.setupRenderState();
    }, () -> {
        COLOR_WRITE.clearRenderState();
        NO_CULL.clearRenderState();
        LEQUAL_DEPTH_TEST.clearRenderState();
        LIGHTNING_TRANSPARENCY.clearRenderState();
        POSITION_COLOR_SHADER.clearRenderState();
    });
}
