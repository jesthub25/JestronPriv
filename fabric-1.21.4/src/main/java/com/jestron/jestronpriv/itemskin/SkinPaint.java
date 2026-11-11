package com.jestron.jestronpriv.itemskin;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * Piksel duzenleyicinin cizim mantigi (Minecraft'a bagimsiz, test edilebilir).
 * <ul>
 * <li>Araclar: kalem / silgi (firca 1-4, ayna), kova (4 yonlu), damlalik, cizgi, dikdortgen, renk degistir,
 * golgelendir (sol: koyulastir, sag: aciklastir).</li>
 * <li>Islemler: yatay / dikey cevir, 90 derece dondur, dis hat ekle, kaydir (ok tuslari), temizle.</li>
 * <li>Animasyon: kare ekle / cogalt / sil / tasi, kare suresi.</li>
 * <li>Geri al / ileri al: en cok 64 adim. Degisiklikten once {@link #push()}: duzenlenen kare kopyalanir, eski hali
 * dokunulmadan saklanir (tum animasyonu her adimda kopyalamaz, bellek dusuk).</li>
 * </ul>
 */
public final class SkinPaint {

    public enum Tool {PEN, ERASER, FILL, PICK, LINE, RECT, REPLACE, SHADE}

    public static final int MAX_UNDO = 64;

    private record Snap(List<SkinImage> frames, List<Integer> ms, int cur) {
    }

    private List<SkinImage> frames = new ArrayList<>();
    private List<Integer> ms = new ArrayList<>();
    private int cur;
    private final ArrayDeque<Snap> undo = new ArrayDeque<>(), redo = new ArrayDeque<>();
    private boolean[] visited;
    public Tool tool = Tool.PEN;
    public boolean mirror, grid = true, onion = true;
    public int brush = 1;

    public SkinPaint(SkinImage start) {
        this(SkinAnim.of(start));
    }

    public SkinPaint(SkinAnim start) {
        load(start);
    }

    private void load(SkinAnim a) {
        frames = new ArrayList<>();
        ms = new ArrayList<>();
        for (int i = 0; i < a.count(); i++) {
            frames.add(a.frames.get(i).copy());
            ms.add(a.ms[i]);
        }
        cur = 0;
    }

    public SkinImage image() {
        return frames.get(cur);
    }

    public SkinImage frame(int i) {
        return frames.get(i);
    }

    public int current() {
        return cur;
    }

    public int count() {
        return frames.size();
    }

    public int ms(int i) {
        return ms.get(i);
    }

    public int size() {
        return image().w;
    }

    /** Su anki hali (kopya). */
    public SkinAnim anim() {
        List<SkinImage> out = new ArrayList<>();
        for (SkinImage f : frames) out.add(f.copy());
        int[] m = new int[ms.size()];
        for (int i = 0; i < m.length; i++) m[i] = ms.get(i);
        return new SkinAnim(out, m);
    }

    /**
     * Canli gorunum: kare goruntuleri kopyalanmaz (onizleme / oynatma icin, her cizimde kopya maliyeti olmasin).
     * Donen nesne saklanmamali; kalici kopya icin {@link #anim()}.
     */
    /** Toplam animasyon suresi (kopyasiz). */
    public int totalMs() {
        int t = 0;
        for (int m : ms) t += m;
        return t;
    }

    /** Bu andaki kare, {@link SkinAnim#frameAt} ile ayni (kopyasiz; her karede cagrilir). */
    public int frameAt(long millis) {
        int n = ms.size();
        if (n <= 1) return 0;
        int total = totalMs();
        long t = Math.floorMod(millis, (long) Math.max(1, total));
        for (int i = 0; i < n; i++) {
            int m = ms.get(i);
            if (t < m) return i;
            t -= m;
        }
        return n - 1;
    }

    public SkinAnim live() {
        int[] m = new int[ms.size()];
        for (int i = 0; i < m.length; i++) m[i] = ms.get(i);
        return new SkinAnim(frames, m);
    }

    // ------------------------------------------------------------------ geri al

    /** Degisiklikten once cagrilir (geri al noktasi). */
    public void push() {
        undo.push(new Snap(new ArrayList<>(frames), new ArrayList<>(ms), cur));
        while (undo.size() > MAX_UNDO) undo.removeLast();
        redo.clear();
        // yazarken kopyala: saklanan kare bir daha degismez
        frames.set(cur, frames.get(cur).copy());
    }

    private void restore(Snap s) {
        frames = new ArrayList<>(s.frames);
        ms = new ArrayList<>(s.ms);
        cur = Math.max(0, Math.min(frames.size() - 1, s.cur));
    }

    public boolean undo() {
        if (undo.isEmpty()) return false;
        redo.push(new Snap(new ArrayList<>(frames), new ArrayList<>(ms), cur));
        restore(undo.pop());
        return true;
    }

    public boolean redo() {
        if (redo.isEmpty()) return false;
        undo.push(new Snap(new ArrayList<>(frames), new ArrayList<>(ms), cur));
        restore(redo.pop());
        return true;
    }

    public int undoSize() {
        return undo.size();
    }

    // ------------------------------------------------------------------ kareler

    public void select(int i) {
        if (i >= 0 && i < frames.size()) cur = i;
    }

    /** Bos kare (su ankinin arkasina). */
    public boolean addFrame() {
        if (frames.size() >= SkinAnim.MAX_FRAMES) return false;
        push();
        frames.add(cur + 1, SkinImage.blank(size()));
        ms.add(cur + 1, ms.get(cur));
        cur++;
        return true;
    }

    public boolean duplicateFrame() {
        if (frames.size() >= SkinAnim.MAX_FRAMES) return false;
        push();
        frames.add(cur + 1, image().copy());
        ms.add(cur + 1, ms.get(cur));
        cur++;
        return true;
    }

    public boolean deleteFrame() {
        if (frames.size() <= 1) return false;
        push();
        frames.remove(cur);
        ms.remove(cur);
        cur = Math.min(cur, frames.size() - 1);
        return true;
    }

    public boolean moveFrame(int dir) {
        int to = cur + dir;
        if (to < 0 || to >= frames.size()) return false;
        push();
        java.util.Collections.swap(frames, cur, to);
        java.util.Collections.swap(ms, cur, to);
        cur = to;
        return true;
    }

    public void setMs(int i, int v) {
        if (i < 0 || i >= ms.size()) return;
        ms.set(i, SkinAnim.clampMs(v));
    }

    public void setAllMs(int v) {
        for (int i = 0; i < ms.size(); i++) ms.set(i, SkinAnim.clampMs(v));
    }

    /** Ice aktarilan goruntu / animasyon (tum kareler degisir). */
    public void replace(SkinAnim a) {
        push();
        load(a);
    }

    public void replace(SkinImage img) {
        replace(SkinAnim.of(img));
    }

    /** Tum kareleri yeni cozunurluge olcekler. */
    public void resize(int n) {
        push();
        for (int i = 0; i < frames.size(); i++) frames.set(i, frames.get(i).resize(n));
    }

    // ------------------------------------------------------------------ cizim

    public void clear() {
        java.util.Arrays.fill(image().argb, 0);
    }

    /** Surukleme basinda: golgelendirme ayni pikseli iki kez koyulastirmasin. */
    public void beginStroke() {
        SkinImage img = image();
        if (visited == null || visited.length != img.argb.length) visited = new boolean[img.argb.length];
        else java.util.Arrays.fill(visited, false);
    }

    private void set(int x, int y, int c) {
        SkinImage img = image();
        if (x < 0 || y < 0 || x >= img.w || y >= img.h) return;
        img.argb[y * img.w + x] = c;
        if (mirror) img.argb[y * img.w + (img.w - 1 - x)] = c;
    }

    private void shadeAt(int x, int y, boolean lighten) {
        SkinImage img = image();
        if (x < 0 || y < 0 || x >= img.w || y >= img.h) return;
        shadeOne(img, x, y, lighten);
        int mx = img.w - 1 - x;
        if (mirror && mx != x) shadeOne(img, mx, y, lighten);
    }

    private void shadeOne(SkinImage img, int x, int y, boolean lighten) {
        int i = y * img.w + x;
        if (visited != null && visited.length == img.argb.length) {
            if (visited[i]) return;
            visited[i] = true;
        }
        int p = img.argb[i];
        if ((p >>> 24) == 0) return;
        int r = (p >> 16) & 255, g = (p >> 8) & 255, b = p & 255;
        if (lighten) {
            r += (255 - r) * 18 / 100;
            g += (255 - g) * 18 / 100;
            b += (255 - b) * 18 / 100;
        } else {
            r = r * 82 / 100;
            g = g * 82 / 100;
            b = b * 82 / 100;
        }
        img.argb[i] = (p & 0xFF000000) | (r << 16) | (g << 8) | b;
    }

    private void dab(int x, int y, int c) {
        int o = (brush - 1) / 2;
        for (int dy = 0; dy < brush; dy++) for (int dx = 0; dx < brush; dx++) set(x - o + dx, y - o + dy, c);
    }

    /** Iki nokta arasi cizgi (firca boyutuyla). */
    public void stroke(int x0, int y0, int x1, int y1, int c) {
        for (int[] p : line(x0, y0, x1, y1)) dab(p[0], p[1], c);
    }

    /** Golgelendirme cizgisi (firca boyutuyla). */
    public void shade(int x0, int y0, int x1, int y1, boolean lighten) {
        int o = (brush - 1) / 2;
        for (int[] p : line(x0, y0, x1, y1))
            for (int dy = 0; dy < brush; dy++) for (int dx = 0; dx < brush; dx++) shadeAt(p[0] - o + dx, p[1] - o + dy, lighten);
    }

    public void rect(int x0, int y0, int x1, int y1, int c) {
        for (int[] p : rectPoints(x0, y0, x1, y1)) dab(p[0], p[1], c);
    }

    /** Cizgi / dikdortgen onizleme noktalari (ayna dahil degil). */
    public List<int[]> preview(int x0, int y0, int x1, int y1) {
        return tool == Tool.RECT ? rectPoints(x0, y0, x1, y1) : line(x0, y0, x1, y1);
    }

    static List<int[]> line(int x0, int y0, int x1, int y1) {
        List<int[]> out = new ArrayList<>();
        int dx = Math.abs(x1 - x0), dy = -Math.abs(y1 - y0), sx = x0 < x1 ? 1 : -1, sy = y0 < y1 ? 1 : -1, err = dx + dy;
        int guard = 0;
        while (guard++ < 1024) {
            out.add(new int[]{x0, y0});
            if (x0 == x1 && y0 == y1) break;
            int e2 = 2 * err;
            if (e2 >= dy) {
                err += dy;
                x0 += sx;
            }
            if (e2 <= dx) {
                err += dx;
                y0 += sy;
            }
        }
        return out;
    }

    static List<int[]> rectPoints(int x0, int y0, int x1, int y1) {
        List<int[]> out = new ArrayList<>();
        int ax = Math.min(x0, x1), bx = Math.max(x0, x1), ay = Math.min(y0, y1), by = Math.max(y0, y1);
        if (bx - ax > 256 || by - ay > 256) return out;
        for (int x = ax; x <= bx; x++) {
            out.add(new int[]{x, ay});
            if (by != ay) out.add(new int[]{x, by});
        }
        for (int y = ay + 1; y < by; y++) {
            out.add(new int[]{ax, y});
            if (bx != ax) out.add(new int[]{bx, y});
        }
        return out;
    }

    /** Ayni renkteki bitisik alani doldurur (ayna aciksa yansimasi da). */
    public void fill(int x, int y, int c) {
        flood(x, y, c);
        if (mirror) flood(image().w - 1 - x, y, c);
    }

    private void flood(int x, int y, int c) {
        SkinImage img = image();
        if (x < 0 || y < 0 || x >= img.w || y >= img.h) return;
        int w = img.w, h = img.h;
        int target = img.argb[y * w + x];
        if (target == c) return;
        int[] stack = new int[w * h * 4 + 4];
        int sp = 0;
        stack[sp++] = y * w + x;
        while (sp > 0) {
            int i = stack[--sp];
            if (img.argb[i] != target) continue;
            img.argb[i] = c;
            int px = i % w, py = i / w;
            if (px > 0) stack[sp++] = i - 1;
            if (px < w - 1) stack[sp++] = i + 1;
            if (py > 0) stack[sp++] = i - w;
            if (py < h - 1) stack[sp++] = i + w;
        }
    }

    /** Karedeki bu renkteki tum pikselleri yeni renge cevirir. */
    public int replaceColor(int x, int y, int c) {
        SkinImage img = image();
        if (x < 0 || y < 0 || x >= img.w || y >= img.h) return 0;
        int target = img.argb[y * img.w + x], n = 0;
        if (target == c) return 0;
        for (int i = 0; i < img.argb.length; i++)
            if (img.argb[i] == target) {
                img.argb[i] = c;
                n++;
            }
        return n;
    }

    // ------------------------------------------------------------------ islemler (su anki kare)

    public void flipH() {
        SkinImage img = image();
        int w = img.w;
        for (int y = 0; y < img.h; y++)
            for (int x = 0; x < w / 2; x++) {
                int a = y * w + x, b = y * w + (w - 1 - x);
                int t = img.argb[a];
                img.argb[a] = img.argb[b];
                img.argb[b] = t;
            }
    }

    public void flipV() {
        SkinImage img = image();
        int w = img.w, h = img.h;
        int[] row = new int[w];
        for (int y = 0; y < h / 2; y++) {
            System.arraycopy(img.argb, y * w, row, 0, w);
            System.arraycopy(img.argb, (h - 1 - y) * w, img.argb, y * w, w);
            System.arraycopy(row, 0, img.argb, (h - 1 - y) * w, w);
        }
    }

    /** Saat yonunde 90 derece. */
    public void rotate() {
        SkinImage img = image();
        int n = img.w;
        int[] o = new int[n * n];
        for (int y = 0; y < n; y++) for (int x = 0; x < n; x++) o[x * n + (n - 1 - y)] = img.argb[y * n + x];
        System.arraycopy(o, 0, img.argb, 0, o.length);
    }

    /** Ciziminin cevresine 1 piksel dis hat (cizimin disindaki bos piksellere). */
    public int outline(int c) {
        SkinImage img = image();
        int w = img.w, h = img.h, n = 0;
        boolean[] add = new boolean[w * h];
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++) {
                if (img.opaque(x, y)) continue;
                if (img.opaque(x - 1, y) || img.opaque(x + 1, y) || img.opaque(x, y - 1) || img.opaque(x, y + 1)) add[y * w + x] = true;
            }
        for (int i = 0; i < add.length; i++)
            if (add[i]) {
                img.argb[i] = c;
                n++;
            }
        return n;
    }

    /** Kareyi kaydirir (tasan kenar karsidan girer). */
    public void shift(int dx, int dy) {
        SkinImage img = image();
        int w = img.w, h = img.h;
        int[] o = new int[w * h];
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++) o[Math.floorMod(y + dy, h) * w + Math.floorMod(x + dx, w)] = img.argb[y * w + x];
        System.arraycopy(o, 0, img.argb, 0, o.length);
    }

    /** RGB -> HSV (0..1). */
    public static float[] hsv(int argb) {
        float r = ((argb >> 16) & 255) / 255f, g = ((argb >> 8) & 255) / 255f, b = (argb & 255) / 255f;
        float max = Math.max(r, Math.max(g, b)), min = Math.min(r, Math.min(g, b)), d = max - min;
        float h = 0f;
        if (d > 1e-6f) {
            if (max == r) h = ((g - b) / d) % 6f;
            else if (max == g) h = (b - r) / d + 2f;
            else h = (r - g) / d + 4f;
            h /= 6f;
            if (h < 0) h += 1f;
        }
        return new float[]{h, max <= 0f ? 0f : d / max, max};
    }
}
