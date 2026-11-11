package com.jestron.jestronpriv.cape;

/**
 * Pelerin kumasi: govdeye bagli, satir satir bukulen bir zincir (Minecraft'a bagimsiz, test edilebilir).
 * <p>
 * Koordinatlar govde uzayinda, blok biriminde: x sag-sol, y asagi, z sirttan geriye. Ust kenar omuz hizasinda,
 * sirtin hemen arkasindadir. Vanilla'nin kendi pelerin degerleri (egilme / cirpma / yan yatma) temel alinir; ustune
 * kumas bukulmesi, asagi dogru ilerleyen ruzgar dalgasi, enine dalgalanma ve kenar kivrilmasi eklenir. Kumas asla
 * sirtin icine girmez (her nokta sirtin arkasinda kalir).
 */
public final class CapeCloth {

    public static final int COLS = 8, ROWS = 14;
    /** Vanilla pelerin olculeri (piksel / 16). */
    public static final float BASE_W = 10f / 16f, BASE_H = 16f / 16f;
    /** Kalinligin yarisi (vanilla 1 piksel kalinlik). */
    public static final float HALF_T = 0.5f / 16f;
    /** Ust kenarin sirttan uzakligi: vanilla kutunun orta duzlemi (2.5 piksel). */
    public static final float BACK_Z = 2.5f / 16f;

    /** Girdi. */
    public static final class Input {
        /** Vanilla PlayerRenderState degerleri (derece). */
        public float lean, flap, lean2;
        /** Saniye. */
        public float time;
        /** Ruzgar 0..2, fizik gucu 0..2, uzunluk 0.7..1.4, genislik 0.8..1.2. */
        public float wind = 1f, physics = 1f, length = 1f, width = 1f;
    }

    /** Orta yuzey noktalari ve normaller: (ROWS+1) x (COLS+1) x 3. */
    public final float[] pos = new float[(ROWS + 1) * (COLS + 1) * 3];
    public final float[] nrm = new float[(ROWS + 1) * (COLS + 1) * 3];
    private final float[] rowY = new float[ROWS + 1], rowZ = new float[ROWS + 1], rowA = new float[ROWS + 1];

    public static int idx(int row, int col) {
        return (row * (COLS + 1) + col) * 3;
    }

    private static float clamp(float v, float a, float b) {
        return v < a ? a : (v > b ? b : v);
    }

    private static float fin(float v, float def) {
        return Float.isFinite(v) ? v : def;
    }

    public void solve(Input in) {
        float lean = clamp(fin(in.lean, 0f), 0f, 150f);
        float flap = clamp(fin(in.flap, 0f), -40f, 60f);
        float lean2 = clamp(fin(in.lean2, 0f), -20f, 20f);
        float t = fin(in.time, 0f) % 3600f;
        float wind = clamp(fin(in.wind, 1f), 0f, 2f);
        float phys = clamp(fin(in.physics, 1f), 0f, 2f);
        float len = BASE_H * clamp(fin(in.length, 1f), 0.7f, 1.4f);
        float wid = BASE_W * clamp(fin(in.width, 1f), 0.8f, 1.2f);

        float motion = clamp(lean / 80f, 0f, 1f);
        float rad = (float) (Math.PI / 180.0);
        // vanilla temel aci; fizik gucu sadece hareket kismini olcekler
        float a0 = clamp(6f + (lean / 2f + flap) * phys, 2f, 88f) * rad;
        float lift = motion * 0.16f * phys;
        float waveAmp = wind * (0.035f + 0.11f * motion) * (0.6f + 0.4f * phys);
        float omega = 3.2f + 5.5f * motion;
        float seg = len / ROWS;

        rowY[0] = 0f;
        rowZ[0] = 0f;
        for (int i = 0; i <= ROWS; i++) {
            float s = (float) i / ROWS;
            // kumas ustte sirta yaslanir, asagida acilir
            float bend = a0 * (0.55f + 0.45f * (float) Math.pow(s, 0.85)) + lift * s;
            float wave = waveAmp * s * (float) Math.sin(omega * t - 5.5f * s) + waveAmp * 0.35f * s * (float) Math.sin(omega * 1.7f * t - 9f * s + 1.3f);
            rowA[i] = clamp(bend + wave, 1.5f * rad, 100f * rad);
        }
        for (int i = 1; i <= ROWS; i++) {
            float a = (rowA[i - 1] + rowA[i]) * 0.5f;
            rowY[i] = rowY[i - 1] + seg * (float) Math.cos(a);
            rowZ[i] = rowZ[i - 1] + seg * (float) Math.sin(a);
        }

        float ripAmp = wind * (0.008f + 0.03f * motion);
        float curl = 0.035f * motion * phys;
        float om2 = 4.1f + 4f * motion;
        float roll = lean2 * 0.5f * rad;
        float twist = -lean2 * 0.5f * rad;
        float cr = (float) Math.cos(roll), sr = (float) Math.sin(roll);
        float ct = (float) Math.cos(twist), st = (float) Math.sin(twist);
        for (int i = 0; i <= ROWS; i++) {
            float s = (float) i / ROWS;
            for (int j = 0; j <= COLS; j++) {
                float u = (float) j / COLS;
                float x = (u - 0.5f) * wid;
                float e = Math.abs(u - 0.5f) * 2f;
                float dz = ripAmp * s * (float) Math.sin(om2 * t + x * 16f * 0.55f + s * 3f) + curl * e * e * s;
                float y = rowY[i];
                float z = rowZ[i] + dz;
                // yan yatma (vanilla capeLean2): dikey eksende hafif burulma + one-arkaya eksende yatma
                float x1 = x * ct + z * st, z1 = -x * st + z * ct;
                float x2 = x1 * cr - y * sr, y2 = x1 * sr + y * cr;
                // sirtin icine asla girmez
                if (z1 < 0f) z1 = 0f;
                int k = idx(i, j);
                pos[k] = x2;
                pos[k + 1] = y2;
                pos[k + 2] = z1 + BACK_Z;
            }
        }
        normals();
    }

    private void normals() {
        for (int i = 0; i <= ROWS; i++) {
            for (int j = 0; j <= COLS; j++) {
                int a = idx(i, Math.max(0, j - 1)), b = idx(i, Math.min(COLS, j + 1));
                int c = idx(Math.max(0, i - 1), j), d = idx(Math.min(ROWS, i + 1), j);
                float ux = pos[b] - pos[a], uy = pos[b + 1] - pos[a + 1], uz = pos[b + 2] - pos[a + 2];
                float vx = pos[d] - pos[c], vy = pos[d + 1] - pos[c + 1], vz = pos[d + 2] - pos[c + 2];
                // du x dv: x saga, y asagi -> +z (geriye, dis yuz)
                float nx = uy * vz - uz * vy, ny = uz * vx - ux * vz, nz = ux * vy - uy * vx;
                float l = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
                int k = idx(i, j);
                if (l < 1e-8f || !Float.isFinite(l)) {
                    nrm[k] = 0f;
                    nrm[k + 1] = 0f;
                    nrm[k + 2] = 1f;
                } else {
                    nrm[k] = nx / l;
                    nrm[k + 1] = ny / l;
                    nrm[k + 2] = nz / l;
                }
            }
        }
    }
}
