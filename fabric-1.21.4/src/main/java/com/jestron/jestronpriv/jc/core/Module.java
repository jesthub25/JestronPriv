package com.jestron.jestronpriv.jc.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Menüdeki her kart bir modüldür. */
public abstract class Module {
    public final String id;
    public final String name;
    public final String description;
    public final Category category;
    private final List<Setting<?>> settings = new ArrayList<>();
    private boolean enabled;
    private final boolean defaultEnabled;

    protected Module(String id, String name, String description, Category category, boolean defaultEnabled) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.defaultEnabled = defaultEnabled;
        this.enabled = defaultEnabled;
    }

    protected <S extends Setting<?>> S add(S s) {
        settings.add(s);
        return s;
    }

    public List<Setting<?>> settings() {
        return Collections.unmodifiableList(settings);
    }

    public boolean hasVisibleSettings() {
        for (Setting<?> s : settings) if (s.visible) return true;
        return false;
    }

    /** Açılıp kapanabilen bir özellik mi (Görünüm gibi kartlar sadece ayardır). */
    public boolean toggleable() {
        return true;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public final void setEnabled(boolean on) {
        if (on == enabled) return;
        enabled = on;
        try {
            if (on) onEnable();
            else onDisable();
        } catch (Throwable t) {
            com.jestron.jestronpriv.jc.JestronClient.LOG.warn("Modül {} durum değişiminde hata", id, t);
        }
    }

    public final void toggle() {
        setEnabled(!enabled);
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    /** Her istemci tick'inde (açıkken) çağrılır. */
    public void tick() {
    }

    /** Bir ayar menüden değişince. */
    public void onSettingChanged(Setting<?> s) {
    }

    // ------------------------------------------------------------------ kayıt

    public JsonObject save() {
        JsonObject o = new JsonObject();
        o.addProperty("enabled", enabled);
        JsonObject st = new JsonObject();
        for (Setting<?> s : settings) st.add(s.key, s.toJson());
        o.add("settings", st);
        return o;
    }

    /** Yüklerken onEnable çağrılmaz; durum JestronClient başlangıçta uygular. */
    public void load(JsonObject o) {
        if (o == null) return;
        try {
            JsonElement en = o.get("enabled");
            if (en != null && en.isJsonPrimitive() && en.getAsJsonPrimitive().isBoolean()) enabled = en.getAsBoolean();
            JsonElement st = o.get("settings");
            if (st != null && st.isJsonObject()) {
                JsonObject so = st.getAsJsonObject();
                for (Setting<?> s : settings) if (so.has(s.key)) s.fromJson(so.get(s.key));
            }
        } catch (RuntimeException ex) {
            com.jestron.jestronpriv.jc.JestronClient.LOG.warn("Modül {} ayarları okunamadı, varsayılanlar kullanılıyor", id, ex);
        }
    }

    public void resetToDefaults() {
        for (Setting<?> s : settings) s.reset();
        setEnabled(defaultEnabled);
    }

    /** Başlangıçta (ayarlar yüklendikten sonra) açık modüller için. */
    public void applyLoadedState() {
        if (enabled) {
            try {
                onEnable();
            } catch (Throwable t) {
                com.jestron.jestronpriv.jc.JestronClient.LOG.warn("Modül {} başlatılamadı", id, t);
            }
        }
    }
}
