package com.jestron.jestronpriv.jc.modules;

import com.jestron.jestronpriv.jc.core.Category;
import com.jestron.jestronpriv.jc.core.Module;
import com.jestron.jestronpriv.jc.core.Setting;
import com.jestron.jestronpriv.jc.mixin.OptionInstanceAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;

/** Tam parlaklık: parlaklık ayarını oyunun izin verdiği sınırın üstüne çıkarır, kapatınca geri yükler. */
public final class FullbrightModule extends Module {
    private static final double BRIGHT = 15.0;
    private final Setting.Num prev = add(new Setting.Num("prev", "", 0.5, 0.0, 1.0, 0.0, ""));
    private final Setting.Bool captured = add(new Setting.Bool("captured", "", false));

    public FullbrightModule() {
        super("fullbright", "Tam parlaklık", "Karanlık mağaralar ve gece tamamen aydınlık.", Category.VISUAL, false);
        prev.visible = false;
        captured.visible = false;
    }

    @SuppressWarnings("unchecked")
    private static OptionInstance<Double> gamma() {
        Options o = Minecraft.getInstance().options;
        return o == null ? null : o.gamma();
    }

    @Override
    protected void onEnable() {
        OptionInstance<Double> g = gamma();
        if (g == null) return;
        if (!captured.on()) {
            Double cur = g.get();
            prev.set(cur == null || cur > 1.0 ? 0.5 : cur);
            captured.set(true);
        }
        force(g, BRIGHT);
    }

    @Override
    public void tick() {
        OptionInstance<Double> g = gamma();
        if (g == null) return;
        Double cur = g.get();
        // video ayarlarından kaydırıcı oynatılırsa yeniden uygula
        if (cur == null || cur < BRIGHT) force(g, BRIGHT);
    }

    @Override
    protected void onDisable() {
        OptionInstance<Double> g = gamma();
        if (g == null) return;
        double back = captured.on() ? prev.d() : 0.5;
        force(g, back);
        captured.set(false);
    }

    /** Oyun kapanırken ayar dosyasına 15 yazılmasın. */
    public void restoreForShutdown() {
        if (!isEnabled()) return;
        OptionInstance<Double> g = gamma();
        if (g != null) force(g, captured.on() ? prev.d() : 0.5);
    }

    private static void force(OptionInstance<Double> g, double v) {
        ((OptionInstanceAccessor) (Object) g).jestron$setValue(v);
    }
}
