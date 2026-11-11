package com.jestron.jestronpriv.fx;

import com.jestron.jestronpriv.fx.effects.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class EffectRegistry {

    private final Map<String, KillEffect> effects = new LinkedHashMap<>();

    public EffectRegistry() {
        register(new CustomTextEffect());
        register(new DiamondSwordEffect());
        register(new BlackHoleEffect());
        register(new TotemEffect());
        register(new ArrowRainEffect());
        register(new LightningEffect());
        register(new MeteorEffect());
        register(new IcePrisonEffect());
        register(new TombstoneEffect());
        register(new SoulAscensionEffect());
        register(new GuillotineEffect());
        register(new AmethystEffect());
        register(new AnvilEffect());
        register(new SwordRingEffect());
        register(new BrokenHeartEffect());
        register(new HellPitEffect());
        register(new DragonEffect());
        register(new TornadoEffect());
        register(new BloodEffect());
        register(new VoidRiftEffect());
        register(new GiantSkullEffect());
        register(new FireworkShowEffect());
        register(new DnaHelixEffect());
        register(new AngelEffect());
        register(new SonicBoomEffect());
        register(new NukeEffect());
        // 1.3.0 yeni efektler
        register(new ThorHammerEffect());
        register(new BloodRoseEffect());
        register(new EndPortalEffect());
        register(new SoulChainsEffect());
        register(new IronFistEffect());
        register(new GoldPyramidEffect());
        register(new TntChainEffect());
        register(new GalaxyEffect());
        register(new BatSwarmEffect());
        register(new VolcanoEffect());
        register(new HourglassEffect());
        register(new OrbitalLaserEffect());
        register(new SnowflakeEffect());
        register(new PhoenixEffect());
        register(new CursedVinesEffect());
        // 1.7.0: 9 efsane efekt (toplam 50)
        register(new SamuraiSlashEffect());
        register(new IceSpearsEffect());
        register(new BlackSunEffect());
        register(new ScytheEffect());
        register(new QuantumEffect());
        register(new StarfallEffect());
        register(new ToxicEffect());
        register(new TidalWaveEffect());
        register(new RuneSealEffect());
    }

    private void register(KillEffect e) {
        effects.put(e.id(), e);
    }

    public KillEffect get(String id) {
        return id == null ? null : effects.get(id.toLowerCase(Locale.ROOT));
    }

    public List<KillEffect> all() {
        return Collections.unmodifiableList(new ArrayList<>(effects.values()));
    }
}
