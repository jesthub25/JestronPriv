package com.jestron.jestronpriv.client.audio;

import net.minecraft.client.sounds.AudioStream;

import javax.sound.sampled.AudioFormat;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Bellekteki sesi oyunun ses motoruna (OpenAL) akitir: secilen aralik, ses seviyesi (100% ustu yumusak sinirlama),
 * yumusak giris / cikis, istenirse tek kanala (konumlu ses) indirme. Ses is parcaciginda okunur; veri degismez.
 */
public final class PcmStream implements AudioStream {

    private final short[] src;
    private final int srcCh, outCh, rate;
    private final int startFrame, endFrame, fadeInFrames, fadeOutFrames;
    private final float gain;
    private int pos;
    private final AudioFormat format;

    public PcmStream(AudioDecoder.Pcm pcm, double startSec, double lengthSec, float gain, double fadeIn, double fadeOut, boolean mono) {
        this.src = pcm.data;
        this.srcCh = Math.max(1, pcm.channels);
        this.outCh = mono ? 1 : Math.min(2, srcCh);
        this.rate = Math.max(1, pcm.rate);
        int total = pcm.frames();
        int s = (int) Math.max(0, Math.min(total, Math.round(startSec * rate)));
        int e = lengthSec > 0 ? (int) Math.min(total, s + Math.round(lengthSec * rate)) : total;
        this.startFrame = s;
        this.endFrame = Math.max(s, e);
        int len = endFrame - startFrame;
        this.fadeInFrames = (int) Math.min(len / 2, Math.max(Math.round(0.006 * rate), Math.round(Math.max(0, fadeIn) * rate)));
        this.fadeOutFrames = (int) Math.min(len / 2, Math.max(Math.round(0.006 * rate), Math.round(Math.max(0, fadeOut) * rate)));
        this.gain = Float.isFinite(gain) ? Math.max(0f, Math.min(2f, gain)) : 1f;
        this.pos = startFrame;
        this.format = new AudioFormat(rate, 16, outCh, true, ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN);
    }

    public double seconds() {
        return (endFrame - startFrame) / (double) rate;
    }

    @Override
    public AudioFormat getFormat() {
        return format;
    }

    /** 100% ustunde yumusak diz: patlamadan yukselir. */
    static float soft(float v) {
        float a = Math.abs(v);
        if (a <= 0.85f) return v;
        float c = 0.85f + 0.15f * (float) Math.tanh((a - 0.85f) / 0.15f);
        return Math.copySign(c, v);
    }

    /** Konumdaki zarf (giris / cikis). */
    float envelope(int frame) {
        float k = 1f;
        int fromStart = frame - startFrame, toEnd = endFrame - 1 - frame;
        if (fromStart < fadeInFrames) k *= fromStart / (float) Math.max(1, fadeInFrames);
        if (toEnd < fadeOutFrames) k *= Math.max(0, toEnd) / (float) Math.max(1, fadeOutFrames);
        // kulaga dogal: karesel egri
        return k * k * (3f - 2f * k);
    }

    @Override
    public ByteBuffer read(int capacity) {
        int frameBytes = 2 * outCh;
        int frames = Math.max(0, Math.min(endFrame - pos, Math.max(frameBytes, capacity) / frameBytes));
        ByteBuffer out = ByteBuffer.allocateDirect(frames * frameBytes).order(ByteOrder.nativeOrder());
        for (int i = 0; i < frames; i++, pos++) {
            float env = envelope(pos) * gain;
            int base = pos * srcCh;
            if (outCh == 1) {
                float v = 0;
                for (int c = 0; c < srcCh; c++) v += src[base + c];
                v = soft(v / srcCh / 32768f * env);
                out.putShort((short) Math.round(v * 32767f));
            } else {
                for (int c = 0; c < outCh; c++) {
                    float v = soft(src[base + Math.min(c, srcCh - 1)] / 32768f * env);
                    out.putShort((short) Math.round(v * 32767f));
                }
            }
        }
        out.flip();
        return out;
    }

    @Override
    public void close() {
        pos = endFrame;
    }
}
