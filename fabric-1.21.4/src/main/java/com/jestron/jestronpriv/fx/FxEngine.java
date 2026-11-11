package com.jestron.jestronpriv.fx;

import com.jestron.jestronpriv.fx.api.ItemStack;
import com.jestron.jestronpriv.fx.api.Location;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

/** Aktif efekt oturumlarini yonetir; istemci her tick'te step() cagirir. */
public final class FxEngine {

    private final FxBackend backend;
    private final EffectRegistry registry = new EffectRegistry();
    private final List<FxSession> active = new ArrayList<>();
    private final Set<String> reported = new HashSet<>();
    private BiConsumer<String, Throwable> errorSink = (id, t) -> { };
    private float volume = 1.0f;
    private int maxSessions = 12;

    public FxEngine(FxBackend backend) {
        this.backend = backend;
    }

    public EffectRegistry registry() {
        return registry;
    }

    public void setErrorSink(BiConsumer<String, Throwable> sink) {
        this.errorSink = sink;
    }

    public float volume() {
        return volume;
    }

    public void setVolume(float v) {
        this.volume = Math.max(0f, Math.min(2f, v));
    }

    public void setMaxSessions(int n) {
        this.maxSessions = Math.max(1, n);
    }

    /** Efekti baslatir. forward = oyuncudan efekt noktasina yatay yon. */
    public FxSession start(KillEffect fx, String victimName, ItemStack head, Location origin,
                           double forwardX, double forwardZ, boolean screenMode) {
        if (fx == null || active.size() >= maxSessions) return null;
        FxSession s = new FxSession(this, backend, fx, victimName, head, origin, forwardX, forwardZ, screenMode);
        active.add(s);
        s.start();
        return s;
    }

    public void step() {
        if (active.isEmpty()) return;
        for (FxSession s : new ArrayList<>(active)) s.step();
    }

    void ended(FxSession s) {
        active.remove(s);
    }

    public void stopAll() {
        for (FxSession s : new ArrayList<>(active)) s.finish();
        active.clear();
    }

    public int activeCount() {
        return active.size();
    }

    void reportError(String effectId, Throwable t) {
        if (reported.add(effectId + ":" + t.getClass().getName())) errorSink.accept(effectId, t);
    }
}
