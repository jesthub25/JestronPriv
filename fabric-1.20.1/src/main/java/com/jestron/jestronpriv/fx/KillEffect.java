package com.jestron.jestronpriv.fx;

import com.jestron.jestronpriv.util.Text;
import com.jestron.jestronpriv.fx.api.Material;

import java.util.Arrays;
import java.util.List;

/**
 * Tum oldurme efektlerinin temeli. Her efekt toplam 2.5 saniye (50 tick) surer;
 * FxSession 50. tick'te tum entity'leri temizler ve uzun sesleri durdurur.
 */
public abstract class KillEffect {

    private final String id;
    private final String defaultName;
    private final Material icon;
    private final boolean threeD;
    private final List<String> description;
    private boolean enabled = true;
    private String customName;

    protected KillEffect(String id, String name, Material icon, boolean threeD, String... description) {
        this.id = id;
        this.defaultName = name;
        this.icon = icon;
        this.threeD = threeD;
        this.description = Arrays.asList(description);
    }

    /** Efekti planlar. Tum zamanlama FxSession uzerinden yapilir (0..49 tick). */
    public abstract void play(FxSession s);

    public String id() { return id; }
    public Material icon() { return icon; }
    public boolean is3D() { return threeD; }
    public List<String> description() { return description; }
    public boolean enabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setCustomName(String customName) { this.customName = customName; }

    public String displayName() {
        return Text.color(customName != null ? customName : defaultName);
    }

    public String plainName() {
        return Text.strip(customName != null ? customName : defaultName);
    }
}
