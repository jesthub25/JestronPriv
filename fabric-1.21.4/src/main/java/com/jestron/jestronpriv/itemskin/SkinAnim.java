package com.jestron.jestronpriv.itemskin;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Esya gorunumu: bir ya da daha cok kare (animasyon) ve her karenin suresi. Tek kareli gorunum eski {@code v1}
 * bicimiyle yazilir (eski profiller / kodlar aynen calisir); animasyon {@code v2;G;Y;KARE;sure1,sure2,..;veri}.
 * Minecraft'a bagimsiz.
 */
public final class SkinAnim {

    public static final int MAX_FRAMES = 32;
    public static final int MIN_MS = 20, MAX_MS = 10_000, DEFAULT_MS = 100;
    /** Animasyonlu kodun en uzun hali (cok karmasik cizim buna sigmazsa kaydedilmez, kullaniciya soylenir). */
    public static final int MAX_CODE = 400_000;

    public final int w, h;
    public final List<SkinImage> frames;
    public final int[] ms;

    public SkinAnim(List<SkinImage> frames, int[] ms) {
        if (frames == null || frames.isEmpty() || frames.size() > MAX_FRAMES || ms == null || ms.length != frames.size()) throw new IllegalArgumentException("kare");
        int w = frames.get(0).w, h = frames.get(0).h;
        for (SkinImage f : frames) if (f.w != w || f.h != h) throw new IllegalArgumentException("boyut");
        this.w = w;
        this.h = h;
        this.frames = List.copyOf(frames);
        this.ms = ms.clone();
        for (int i = 0; i < this.ms.length; i++) this.ms[i] = clampMs(this.ms[i]);
    }

    public static SkinAnim of(SkinImage single) {
        return new SkinAnim(List.of(single), new int[]{DEFAULT_MS});
    }

    public static int clampMs(int v) {
        return Math.max(MIN_MS, Math.min(MAX_MS, v));
    }

    public int count() {
        return frames.size();
    }

    public boolean animated() {
        return frames.size() > 1;
    }

    public int totalMs() {
        int t = 0;
        for (int m : ms) t += m;
        return t;
    }

    /** Bu andaki kare (milisaniye saatine gore, dongu). */
    public int frameAt(long millis) {
        if (frames.size() == 1) return 0;
        int total = totalMs();
        long t = Math.floorMod(millis, (long) total);
        for (int i = 0; i < ms.length; i++) {
            if (t < ms[i]) return i;
            t -= ms[i];
        }
        return ms.length - 1;
    }

    public boolean isEmpty() {
        for (SkinImage f : frames) if (!f.isEmpty()) return false;
        return true;
    }

    // ------------------------------------------------------------------ kodlama

    /** Kod ya da (cok buyukse) null. */
    public String encode() {
        if (frames.size() == 1) {
            String s = frames.get(0).encode();
            return s.length() <= SkinImage.MAX_CODE ? s : null;
        }
        byte[] raw = new byte[w * h * 4 * frames.size()];
        int o = 0;
        for (SkinImage f : frames) {
            for (int p : f.argb) {
                if ((p >>> 24) == 0) p = 0;
                raw[o++] = (byte) (p >>> 24);
                raw[o++] = (byte) (p >>> 16);
                raw[o++] = (byte) (p >>> 8);
                raw[o++] = (byte) p;
            }
        }
        Deflater d = new Deflater(Deflater.BEST_COMPRESSION);
        d.setInput(raw);
        d.finish();
        ByteArrayOutputStream bo = new ByteArrayOutputStream(raw.length / 8 + 64);
        byte[] buf = new byte[8192];
        while (!d.finished()) bo.write(buf, 0, d.deflate(buf));
        d.end();
        StringBuilder sb = new StringBuilder("v2;").append(w).append(';').append(h).append(';').append(frames.size()).append(';');
        for (int i = 0; i < ms.length; i++) sb.append(i == 0 ? "" : ",").append(ms[i]);
        sb.append(';').append(Base64.getUrlEncoder().withoutPadding().encodeToString(bo.toByteArray()));
        return sb.length() <= MAX_CODE ? sb.toString() : null;
    }

    /** v1 ya da v2 kodu; gecersiz / bozuk / asiri buyukse null. */
    public static SkinAnim decode(String s) {
        if (s == null) return null;
        if (s.startsWith("v1;")) {
            SkinImage i = SkinImage.decode(s);
            return i == null ? null : of(i);
        }
        if (!s.startsWith("v2;") || s.length() > MAX_CODE) return null;
        try {
            String[] p = s.split(";", 6);
            if (p.length != 6) return null;
            int w = Integer.parseInt(p[1]), h = Integer.parseInt(p[2]), n = Integer.parseInt(p[3]);
            if (w < 1 || h < 1 || w > SkinImage.MAX_SIDE || h > SkinImage.MAX_SIDE || n < 1 || n > MAX_FRAMES) return null;
            String[] ds = p[4].split(",");
            if (ds.length != n) return null;
            int[] ms = new int[n];
            for (int i = 0; i < n; i++) ms[i] = clampMs(Integer.parseInt(ds[i].trim()));
            byte[] z = Base64.getUrlDecoder().decode(p[5]);
            int need = w * h * 4 * n;
            byte[] raw = new byte[need];
            Inflater inf = new Inflater();
            int got = 0;
            try {
                inf.setInput(z);
                while (got < need && !inf.finished()) {
                    int k = inf.inflate(raw, got, need - got);
                    if (k == 0 && (inf.needsInput() || inf.needsDictionary())) break;
                    got += k;
                }
                if (got != need) return null;
                if (!inf.finished()) {
                    byte[] extra = new byte[1];
                    if (inf.inflate(extra) > 0) return null;
                }
            } finally {
                inf.end();
            }
            List<SkinImage> frames = new ArrayList<>(n);
            int o = 0;
            for (int f = 0; f < n; f++) {
                int[] a = new int[w * h];
                for (int i = 0; i < a.length; i++, o += 4)
                    a[i] = ((raw[o] & 255) << 24) | ((raw[o + 1] & 255) << 16) | ((raw[o + 2] & 255) << 8) | (raw[o + 3] & 255);
                frames.add(new SkinImage(w, h, a));
            }
            return new SkinAnim(frames, ms);
        } catch (IllegalArgumentException | DataFormatException e) {
            return null;
        }
    }

    // ------------------------------------------------------------------ ice aktarma

    /**
     * Dikey animasyon seridi (vanilla .mcmeta dokulari gibi: yukseklik = kare x genislik) ya da tek goruntu.
     *
     * @param frameMs kare suresi (mcmeta "frametime" x 50 ms)
     */
    public static SkinAnim fromStrip(int w, int h, int[] argb, int frameMs) {
        if (w < 1 || h < 1 || argb == null || argb.length < w * h) return null;
        if (h > w && h % w == 0) {
            int n = h / w;
            int keep = Math.min(MAX_FRAMES, n);
            List<SkinImage> out = new ArrayList<>();
            int[] ms = new int[keep];
            for (int i = 0; i < keep; i++) {
                int src = (int) ((long) i * n / keep);
                int[] f = new int[w * w];
                System.arraycopy(argb, src * w * w, f, 0, w * w);
                SkinImage img = SkinImage.fit(w, w, f);
                if (img == null) return null;
                out.add(img);
                ms[i] = clampMs(frameMs * Math.max(1, n / keep));
            }
            return new SkinAnim(out, ms);
        }
        SkinImage img = SkinImage.fit(w, h, argb);
        return img == null ? null : of(img);
    }

    /** Kare listesinden (GIF gibi); fazla kare seyreltilir, sureler toplanir. Hepsi ayni boyuta uyarlanir. */
    public static SkinAnim fromFrames(List<int[]> frames, int w, int h, List<Integer> delays) {
        if (frames == null || frames.isEmpty()) return null;
        int n = frames.size(), keep = Math.min(MAX_FRAMES, n);
        List<SkinImage> out = new ArrayList<>();
        int[] ms = new int[keep];
        int side = -1;
        for (int i = 0; i < keep; i++) {
            int a = (int) ((long) i * n / keep), b = (int) ((long) (i + 1) * n / keep);
            SkinImage img = SkinImage.fit(w, h, frames.get(a));
            if (img == null) return null;
            if (side < 0) side = img.w;
            if (img.w != side) img = img.resize(side);
            out.add(img);
            int d = 0;
            for (int j = a; j < b; j++) d += delays == null || j >= delays.size() ? DEFAULT_MS : delays.get(j);
            ms[i] = clampMs(d);
        }
        return new SkinAnim(out, ms);
    }

    /** Tum kareleri n x n'e olcekler. */
    public SkinAnim resize(int n) {
        List<SkinImage> out = new ArrayList<>();
        for (SkinImage f : frames) out.add(f.resize(n));
        return new SkinAnim(out, ms);
    }
}
