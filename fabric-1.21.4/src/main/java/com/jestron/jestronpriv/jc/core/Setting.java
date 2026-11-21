package com.jestron.jestronpriv.jc.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;

import java.util.function.Supplier;
import com.google.gson.JsonPrimitive;

/** Bir modül ayarı. Değerler JSON'a yazılır, okunurken sınırlarla doğrulanır. */
public abstract class Setting<T> {
    public final String key;
    public final String label;
    protected final T def;
    protected T value;
    /** Menüde gösterilsin mi (iç durum ayarları gizlidir). */
    public boolean visible = true;

    protected Setting(String key, String label, T def) {
        this.key = key;
        this.label = label;
        this.def = def;
        this.value = def;
    }

    public T get() {
        return value;
    }

    public void set(T v) {
        this.value = sanitize(v);
    }

    public void reset() {
        this.value = def;
    }

    protected T sanitize(T v) {
        return v == null ? def : v;
    }

    public abstract JsonElement toJson();

    /** Bozuk veya yanlış türde değerlerde varsayılana döner. */
    public abstract void fromJson(JsonElement e);

    public Setting<T> hidden() {
        this.visible = false;
        return this;
    }

    // ------------------------------------------------------------------ türler

    public static final class Bool extends Setting<Boolean> {
        public Bool(String key, String label, boolean def) {
            super(key, label, def);
        }

        public boolean on() {
            return Boolean.TRUE.equals(value);
        }

        public void toggle() {
            value = !on();
        }

        @Override
        public JsonElement toJson() {
            return new JsonPrimitive(on());
        }

        @Override
        public void fromJson(JsonElement e) {
            try {
                value = e != null && e.isJsonPrimitive() && e.getAsJsonPrimitive().isBoolean() ? e.getAsBoolean() : def;
            } catch (RuntimeException ex) {
                value = def;
            }
        }
    }

    public static final class Num extends Setting<Double> {
        public final double min, max, step;
        public final String suffix;

        public Num(String key, String label, double def, double min, double max, double step, String suffix) {
            super(key, label, def);
            this.min = min;
            this.max = max;
            this.step = step;
            this.suffix = suffix == null ? "" : suffix;
        }

        public double d() {
            return value;
        }

        public int i() {
            return (int) Math.round(value);
        }

        @Override
        protected Double sanitize(Double v) {
            if (v == null || v.isNaN() || v.isInfinite()) return def;
            double c = Math.max(min, Math.min(max, v));
            if (step > 0) c = min + Math.round((c - min) / step) * step;
            return Math.max(min, Math.min(max, c));
        }

        /** 0..1 arası kaydırıcı konumu. */
        public double fraction() {
            return max <= min ? 0 : (value - min) / (max - min);
        }

        public void setFraction(double f) {
            set(min + Math.max(0, Math.min(1, f)) * (max - min));
        }

        public String display() {
            if (step >= 1) return Math.round(value) + suffix;
            if (step >= 0.1) return String.format(java.util.Locale.ROOT, "%.1f", value) + suffix;
            return String.format(java.util.Locale.ROOT, "%.2f", value) + suffix;
        }

        @Override
        public JsonElement toJson() {
            return new JsonPrimitive(value);
        }

        @Override
        public void fromJson(JsonElement e) {
            try {
                value = e != null && e.isJsonPrimitive() && e.getAsJsonPrimitive().isNumber() ? sanitize(e.getAsDouble()) : def;
            } catch (RuntimeException ex) {
                value = def;
            }
        }
    }

    public static final class Choice extends Setting<Integer> {
        public final String[] options;

        public Choice(String key, String label, int def, String... options) {
            super(key, label, def);
            this.options = options;
        }

        public int index() {
            return value;
        }

        public String display() {
            return options[value];
        }

        public void next(int dir) {
            int n = options.length;
            value = ((value + dir) % n + n) % n;
        }

        @Override
        protected Integer sanitize(Integer v) {
            if (v == null || v < 0 || v >= options.length) return def;
            return v;
        }

        @Override
        public JsonElement toJson() {
            return new JsonPrimitive(value);
        }

        @Override
        public void fromJson(JsonElement e) {
            try {
                value = e != null && e.isJsonPrimitive() && e.getAsJsonPrimitive().isNumber() ? sanitize(e.getAsInt()) : def;
            } catch (RuntimeException ex) {
                value = def;
            }
        }
    }

    public static final class Text extends Setting<String> {
        public final int maxLength;

        public Text(String key, String label, String def, int maxLength) {
            super(key, label, def);
            this.maxLength = maxLength;
        }

        @Override
        protected String sanitize(String v) {
            if (v == null) return def;
            // kontrol karakterleri ve satır sonu sohbeti bozar
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < v.length() && sb.length() < maxLength; i++) {
                char c = v.charAt(i);
                if (c >= 32 && c != 127 && c != '§') sb.append(c);
            }
            return sb.toString();
        }

        @Override
        public JsonElement toJson() {
            return new JsonPrimitive(value);
        }

        @Override
        public void fromJson(JsonElement e) {
            try {
                value = e != null && e.isJsonPrimitive() && e.getAsJsonPrimitive().isString() ? sanitize(e.getAsString()) : def;
            } catch (RuntimeException ex) {
                value = def;
            }
        }
    }

    /**
     * Tuş ataması. Değer Minecraft'ın kendi tuş atamasında (options.txt) tutulur; böylece Kontroller menüsüyle
     * ve çakışma kontrolüyle aynı kalır. Menüde "tuşa bas" ile atanır.
     */
    public static final class Bind extends Setting<Integer> {
        private final Supplier<KeyMapping> mapping;
        public final boolean allowUnbind;

        public Bind(String key, String label, Supplier<KeyMapping> mapping, boolean allowUnbind) {
            super(key, label, -1);
            this.mapping = mapping;
            this.allowUnbind = allowUnbind;
        }

        public KeyMapping mapping() {
            return mapping.get();
        }

        public boolean bound() {
            KeyMapping k = mapping();
            return k != null && !k.isUnbound();
        }

        public String display() {
            KeyMapping k = mapping();
            if (k == null) return "—";
            return k.isUnbound() ? "Atanmadı" : k.getTranslatedKeyMessage().getString();
        }

        public void bindKey(InputConstants.Key key) {
            KeyMapping k = mapping();
            if (k == null || key == null) return;
            k.setKey(key);
            KeyMapping.resetMapping();
            try {
                Minecraft.getInstance().options.save();
            } catch (Throwable ignored) {
            }
        }

        public void unbind() {
            bindKey(InputConstants.UNKNOWN);
        }

        /** Aynı tuşu kullanan başka bir atama varsa adı. */
        public String conflict() {
            KeyMapping k = mapping();
            if (k == null || k.isUnbound()) return null;
            for (KeyMapping o : Minecraft.getInstance().options.keyMappings) {
                if (o != k && o.same(k)) return I18n.get(o.getName());
            }
            return null;
        }

        @Override
        public Integer get() {
            KeyMapping k = mapping();
            return k == null || k.isUnbound() ? -1 : net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper.getBoundKeyOf(k).getValue();
        }

        @Override
        public void set(Integer v) {
            if (v == null || v < 0) unbind();
            else bindKey(InputConstants.Type.KEYSYM.getOrCreate(v));
        }

        @Override
        public void reset() {
            KeyMapping k = mapping();
            if (k != null) bindKey(k.getDefaultKey());
        }

        @Override
        public JsonElement toJson() {
            return JsonNull.INSTANCE; // options.txt'de saklanır
        }

        @Override
        public void fromJson(JsonElement e) {
        }
    }
}
