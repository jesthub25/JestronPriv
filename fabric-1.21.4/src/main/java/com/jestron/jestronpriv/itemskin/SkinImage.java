package com.jestron.jestronpriv.itemskin;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Esya gorunumu goruntusu (kare, en cok 64x64, ARGB) ve metin kodlamasi (ayar dosyasina / profil koduna yazilir).
 * Bicim: {@code v1;GENISLIK;YUKSEKLIK;base64(deflate(ARGB))}. Minecraft'a bagimsiz.
 */
public final class SkinImage {

    public static final int MAX_SIDE = 64;
    public static final int[] SIZES = {16, 32, 64};
    /** Bir kodun en uzun hali (64x64 rastgele gurultu bile buna sigar). */
    public static final int MAX_CODE = 24_000;

    public final int w, h;
    public final int[] argb;

    public SkinImage(int w, int h, int[] argb) {
        if (w < 1 || h < 1 || w > MAX_SIDE || h > MAX_SIDE || argb == null || argb.length != w * h) throw new IllegalArgumentException("boyut");
        this.w = w;
        this.h = h;
        this.argb = argb;
    }

    public static SkinImage blank(int n) {
        return new SkinImage(n, n, new int[n * n]);
    }

    public SkinImage copy() {
        return new SkinImage(w, h, argb.clone());
    }

    public int get(int x, int y) {
        return x < 0 || y < 0 || x >= w || y >= h ? 0 : argb[y * w + x];
    }

    public boolean opaque(int x, int y) {
        return (get(x, y) >>> 24) >= 26;
    }

    public boolean isEmpty() {
        for (int p : argb) if ((p >>> 24) >= 26) return false;
        return true;
    }

    // ------------------------------------------------------------------ kodlama

    public String encode() {
        byte[] raw = new byte[argb.length * 4];
        for (int i = 0; i < argb.length; i++) {
            int p = argb[i];
            // tamamen saydam pikselin rengi onemsiz: sifirla (daha iyi sikisir, ayni gorunum ayni kod)
            if ((p >>> 24) == 0) p = 0;
            raw[i * 4] = (byte) (p >>> 24);
            raw[i * 4 + 1] = (byte) (p >>> 16);
            raw[i * 4 + 2] = (byte) (p >>> 8);
            raw[i * 4 + 3] = (byte) p;
        }
        Deflater d = new Deflater(Deflater.BEST_COMPRESSION);
        d.setInput(raw);
        d.finish();
        ByteArrayOutputStream bo = new ByteArrayOutputStream(raw.length / 4 + 64);
        byte[] buf = new byte[4096];
        while (!d.finished()) bo.write(buf, 0, d.deflate(buf));
        d.end();
        return "v1;" + w + ";" + h + ";" + Base64.getUrlEncoder().withoutPadding().encodeToString(bo.toByteArray());
    }

    /** Gecersiz / bozuk / asiri buyuk ise null. */
    public static SkinImage decode(String s) {
        if (s == null || s.length() > MAX_CODE || !s.startsWith("v1;")) return null;
        try {
            String[] p = s.split(";", 4);
            if (p.length != 4) return null;
            int w = Integer.parseInt(p[1]), h = Integer.parseInt(p[2]);
            if (w < 1 || h < 1 || w > MAX_SIDE || h > MAX_SIDE) return null;
            byte[] z = Base64.getUrlDecoder().decode(p[3]);
            int need = w * h * 4;
            byte[] raw = new byte[need];
            Inflater inf = new Inflater();
            int got = 0;
            try {
                inf.setInput(z);
                while (got < need && !inf.finished()) {
                    int n = inf.inflate(raw, got, need - got);
                    if (n == 0 && (inf.needsInput() || inf.needsDictionary())) break;
                    got += n;
                }
                // fazladan veri (bomba) ya da eksik veri: gecersiz
                if (got != need) return null;
                if (!inf.finished()) {
                    byte[] extra = new byte[1];
                    if (inf.inflate(extra) > 0) return null;
                }
            } finally {
                inf.end();
            }
            int[] a = new int[w * h];
            for (int i = 0; i < a.length; i++)
                a[i] = ((raw[i * 4] & 255) << 24) | ((raw[i * 4 + 1] & 255) << 16) | ((raw[i * 4 + 2] & 255) << 8) | (raw[i * 4 + 3] & 255);
            return new SkinImage(w, h, a);
        } catch (IllegalArgumentException | DataFormatException e) {
            return null;
        }
    }

    // ------------------------------------------------------------------ uyarlama

    /**
     * Herhangi bir goruntuyu esya gorunumune uyarlar: animasyon seridi ise ilk kare, kare degilse ortalanir, 64'ten
     * buyukse kutu ortalamasiyla kucultulur. Kaynak en cok 4096x4096.
     */
    public static SkinImage fit(int w, int h, int[] argb) {
        if (w < 1 || h < 1 || w > 4096 || h > 4096 || argb == null || argb.length < w * h) return null;
        // dikey animasyon seridi (vanilla .mcmeta): ilk kare
        if (h > w && h % w == 0) h = w;
        int n = Math.max(w, h);
        int[] sq = new int[n * n];
        int ox = (n - w) / 2, oy = (n - h) / 2;
        for (int y = 0; y < h; y++) System.arraycopy(argb, y * w, sq, (y + oy) * n + ox, w);
        if (n <= MAX_SIDE) return new SkinImage(n, n, sq);
        int t = MAX_SIDE;
        int[] out = new int[t * t];
        for (int y = 0; y < t; y++) {
            int y0 = y * n / t, y1 = Math.max(y0 + 1, (y + 1) * n / t);
            for (int x = 0; x < t; x++) {
                int x0 = x * n / t, x1 = Math.max(x0 + 1, (x + 1) * n / t);
                long sa = 0, sr = 0, sg = 0, sb = 0;
                int cnt = 0;
                for (int yy = y0; yy < y1; yy++)
                    for (int xx = x0; xx < x1; xx++) {
                        int p = sq[yy * n + xx];
                        int al = p >>> 24;
                        sa += al;
                        sr += (long) ((p >> 16) & 255) * al;
                        sg += (long) ((p >> 8) & 255) * al;
                        sb += (long) (p & 255) * al;
                        cnt++;
                    }
                int al = (int) (sa / cnt);
                out[y * t + x] = sa == 0 ? 0 : (al << 24) | ((int) (sr / sa) << 16) | ((int) (sg / sa) << 8) | (int) (sb / sa);
            }
        }
        return new SkinImage(t, t, out);
    }

    /** En yakin komsu ile n x n'e olcekler (cizim cozunurlugu degisince). */
    public SkinImage resize(int n) {
        if (n == w && n == h) return copy();
        int[] o = new int[n * n];
        for (int y = 0; y < n; y++) for (int x = 0; x < n; x++) o[y * n + x] = argb[(y * h / n) * w + x * w / n];
        return new SkinImage(n, n, o);
    }

    /** Gecerli esya kimligi ("minecraft:diamond_sword"). */
    public static boolean validId(String id) {
        if (id == null || id.length() > 128) return false;
        int c = id.indexOf(':');
        if (c <= 0 || c != id.lastIndexOf(':') || c == id.length() - 1) return false;
        for (int i = 0; i < id.length(); i++) {
            char ch = id.charAt(i);
            if (i == c) continue;
            boolean ok = (ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9') || ch == '_' || ch == '-' || ch == '.' || (i > c && ch == '/');
            if (!ok) return false;
        }
        return true;
    }
}
