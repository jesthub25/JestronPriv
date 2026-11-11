package com.jestron.jestronpriv.hitfx;

/**
 * Vurus partikulu stillerinin ses tasarimi (Minecraft'tan bagimsiz). Her stilde iki ses: kisa, net bir
 * vurus tonu ve daha dolgun, katmanli bir kritik tonu. Hepsi oyunun kendi sesleri (1.21.4 listesinde dogrulanir).
 * Katman: anahtar, ses, perde, gecikme (tick). pitch &lt; 0 = pentatonik dizinden rastgele nota (Notalar stili).
 */
public final class HitSounds {

    public record Layer(String key, float vol, float pitch, int delay) {
    }

    private static Layer l(String key, float vol, float pitch) {
        return new Layer("minecraft:" + key, vol, pitch, 0);
    }

    private static Layer l(String key, float vol, float pitch, int delay) {
        return new Layer("minecraft:" + key, vol, pitch, delay);
    }

    /** Notalar stili icin pentatonik dizi (nota blogu perdeleri). */
    public static final float[] PENTA = {0.749f, 0.841f, 1.0f, 1.122f, 1.26f, 1.498f, 1.682f};

    private static final Layer[] NONE = {};

    private HitSounds() {
    }

    public static Layer[] hit(HitStyle st) {
        if (st == null) return NONE;
        return switch (st) {
            case KOPUK -> new Layer[]{l("block.bubble_column.bubble_pop", 0.9f, 1.25f), l("entity.slime.squish_small", 0.45f, 1.45f)};
            case KELEBEK -> new Layer[]{l("entity.parrot.fly", 0.55f, 1.55f), l("block.amethyst_block.chime", 0.55f, 1.7f)};
            case KALP -> new Layer[]{l("entity.experience_orb.pickup", 0.45f, 1.45f), l("block.note_block.chime", 0.35f, 1.6f)};
            case ISILTI -> new Layer[]{l("block.amethyst_block.hit", 0.75f, 1.6f), l("block.note_block.chime", 0.3f, 2.0f, 1)};
            case YILDIZ -> new Layer[]{l("entity.firework_rocket.twinkle", 0.35f, 1.7f), l("block.note_block.pling", 0.3f, 1.8f)};
            case ALEV -> new Layer[]{l("entity.blaze.shoot", 0.3f, 1.65f), l("item.firecharge.use", 0.35f, 1.55f)};
            case RUH -> new Layer[]{l("particle.soul_escape", 1.0f, 1.2f), l("block.soul_sand.hit", 0.6f, 1.3f)};
            case KAR -> new Layer[]{l("block.powder_snow.break", 0.7f, 1.3f), l("block.small_amethyst_bud.break", 0.4f, 1.8f)};
            case ELMAS -> new Layer[]{l("block.amethyst_block.break", 0.55f, 1.55f), l("block.medium_amethyst_bud.break", 0.45f, 1.7f)};
            case KAN -> new Layer[]{l("block.slime_block.hit", 0.6f, 0.75f), l("block.honey_block.break", 0.35f, 0.9f)};
            case SIMSEK -> new Layer[]{l("entity.lightning_bolt.impact", 0.22f, 1.85f), l("block.respawn_anchor.charge", 0.25f, 1.9f)};
            case SAKURA -> new Layer[]{l("block.cherry_leaves.break", 0.8f, 1.3f), l("block.note_block.chime", 0.28f, 1.8f, 1)};
            case YAPRAK -> new Layer[]{l("block.azalea_leaves.break", 0.8f, 1.2f), l("block.grass.hit", 0.45f, 1.3f)};
            case NOTA -> new Layer[]{l("block.note_block.harp", 0.7f, -1f), l("block.note_block.bell", 0.25f, -1f)};
            case GOKKUSAGI -> new Layer[]{l("block.amethyst_block.chime", 0.6f, 1.35f), l("entity.experience_orb.pickup", 0.3f, 1.8f)};
            case ENDER -> new Layer[]{l("entity.enderman.teleport", 0.3f, 1.65f), l("block.end_portal_frame.fill", 0.35f, 1.45f)};
            case PARA -> new Layer[]{l("entity.experience_orb.pickup", 0.45f, 1.25f), l("block.chain.hit", 0.6f, 1.65f)};
            case KAFATASI -> new Layer[]{l("block.bone_block.break", 0.6f, 1.1f), l("entity.wither_skeleton.hurt", 0.3f, 1.25f)};
            case SOK -> new Layer[]{l("entity.breeze.wind_burst", 0.4f, 1.6f), l("entity.player.attack.knockback", 0.5f, 1.4f)};
            case NEON -> new Layer[]{l("block.amethyst_block.hit", 0.5f, 1.85f), l("block.beacon.power_select", 0.22f, 1.95f)};
            case KONFETI -> new Layer[]{l("entity.firework_rocket.blast", 0.3f, 1.65f), l("entity.firework_rocket.twinkle", 0.25f, 1.9f, 2)};
            case TUY -> new Layer[]{l("entity.allay.ambient_without_item", 0.45f, 1.7f), l("block.amethyst_block.chime", 0.4f, 1.9f, 1)};
            case HILAL -> new Layer[]{l("block.amethyst_block.resonate", 0.55f, 1.6f), l("block.note_block.chime", 0.3f, 1.5f, 1)};
            case PAPATYA -> new Layer[]{l("block.flowering_azalea.break", 0.8f, 1.3f), l("block.note_block.flute", 0.3f, 1.7f, 1)};
            case BUZ -> new Layer[]{l("block.glass.break", 0.35f, 1.9f), l("block.powder_snow.hit", 0.7f, 1.4f)};
            case GIRDAP -> new Layer[]{l("entity.breeze.idle_ground", 0.5f, 1.6f), l("entity.illusioner.cast_spell", 0.25f, 1.8f)};
            case TAC -> new Layer[]{l("block.chain.place", 0.6f, 1.6f), l("entity.experience_orb.pickup", 0.4f, 1.4f)};
            case RUN -> new Layer[]{l("block.enchantment_table.use", 0.7f, 1.5f), l("block.amethyst_block.chime", 0.4f, 1.3f)};
            case YARASA -> new Layer[]{l("entity.bat.takeoff", 0.35f, 1.4f), l("entity.bat.hurt", 0.25f, 1.6f)};
            case PATI -> new Layer[]{l("entity.cat.ambient", 0.35f, 1.6f), l("entity.experience_orb.pickup", 0.3f, 1.7f)};
            case YONCA -> new Layer[]{l("block.cave_vines.pick_berries", 0.7f, 1.4f), l("entity.experience_orb.pickup", 0.35f, 1.6f)};
            case KESIK -> new Layer[]{l("entity.player.attack.sweep", 0.55f, 1.5f), l("item.trident.hit", 0.35f, 1.7f)};
            case VOLKAN -> new Layer[]{l("block.lava.pop", 0.9f, 1.1f), l("entity.magma_cube.squish_small", 0.5f, 1.2f)};
            case ZEHIR -> new Layer[]{l("entity.slime.squish", 0.5f, 1.4f), l("block.brewing_stand.brew", 0.3f, 1.7f)};
            case GALAKSI -> new Layer[]{l("entity.ender_eye.launch", 0.45f, 1.6f), l("block.amethyst_block.chime", 0.4f, 1.4f)};
            case KARADELIK -> new Layer[]{l("block.respawn_anchor.deplete", 0.4f, 1.6f), l("entity.enderman.teleport", 0.25f, 1.8f)};
            case BALON -> new Layer[]{l("block.bubble_column.upwards_inside", 0.8f, 1.5f), l("entity.item.pickup", 0.4f, 1.8f)};
            case ZUMRUT -> new Layer[]{l("block.amethyst_cluster.hit", 0.7f, 1.6f), l("block.note_block.chime", 0.3f, 1.8f, 1)};
            case EJDERATESI -> new Layer[]{l("entity.ender_dragon.shoot", 0.25f, 1.7f), l("entity.blaze.shoot", 0.25f, 1.9f)};
            case BUYU -> new Layer[]{l("entity.illusioner.cast_spell", 0.35f, 1.6f), l("block.enchantment_table.use", 0.5f, 1.8f)};
            case PATLAMA -> new Layer[]{l("entity.generic.explode", 0.22f, 1.7f), l("entity.firework_rocket.blast", 0.4f, 1.3f)};
            case MUREKKEP -> new Layer[]{l("entity.squid.squirt", 0.5f, 1.4f), l("block.slime_block.hit", 0.45f, 0.9f)};
            case FISEK -> new Layer[]{l("entity.firework_rocket.blast", 0.35f, 1.6f), l("entity.firework_rocket.twinkle", 0.3f, 1.7f, 2)};
            case PIKSEL -> new Layer[]{l("block.note_block.bit", 0.6f, 1.5f), l("block.note_block.bit", 0.4f, 2.0f, 1)};
            case OKYANUS -> new Layer[]{l("entity.player.splash", 0.3f, 1.6f), l("block.bubble_column.bubble_pop", 0.8f, 1.3f)};
            case KUM -> new Layer[]{l("block.sand.break", 0.8f, 1.3f), l("block.suspicious_sand.break", 0.5f, 1.5f)};
            case PLAZMA -> new Layer[]{l("block.beacon.power_select", 0.25f, 1.9f), l("entity.shulker.shoot", 0.35f, 1.7f)};
            case GOLGE -> new Layer[]{l("entity.phantom.flap", 0.6f, 1.2f), l("block.sculk.break", 0.6f, 1.0f)};
            case SEKER -> new Layer[]{l("entity.experience_orb.pickup", 0.4f, 1.7f), l("block.note_block.chime", 0.35f, 1.9f, 1)};
            case ATES_BOCEGI -> new Layer[]{l("block.amethyst_block.chime", 0.45f, 1.8f), l("entity.allay.item_given", 0.25f, 1.9f, 1)};
            default -> NONE;
        };
    }

    public static Layer[] crit(HitStyle st) {
        if (st == null) return NONE;
        return switch (st) {
            case KOPUK -> new Layer[]{l("entity.puffer_fish.blow_up", 0.6f, 1.3f), l("block.bubble_column.bubble_pop", 1.0f, 0.95f),
                    l("block.bubble_column.bubble_pop", 0.8f, 1.45f, 1), l("entity.slime.jump", 0.4f, 1.3f, 2)};
            case KELEBEK -> new Layer[]{l("entity.allay.ambient_without_item", 0.6f, 1.5f), l("block.amethyst_block.chime", 0.9f, 1.9f),
                    l("entity.phantom.flap", 0.4f, 1.8f, 1), l("block.amethyst_block.chime", 0.6f, 1.5f, 3)};
            case KALP -> new Layer[]{l("block.note_block.bell", 0.6f, 1.41f), l("entity.experience_orb.pickup", 0.6f, 1.1f),
                    l("block.note_block.bell", 0.45f, 1.78f, 2)};
            case ISILTI -> new Layer[]{l("block.amethyst_block.resonate", 0.7f, 1.4f), l("block.note_block.chime", 0.55f, 1.68f),
                    l("block.note_block.chime", 0.45f, 2.0f, 2)};
            case YILDIZ -> new Layer[]{l("entity.firework_rocket.blast", 0.55f, 1.4f), l("block.note_block.pling", 0.4f, 1.5f),
                    l("entity.firework_rocket.twinkle", 0.5f, 1.3f, 2)};
            case ALEV -> new Layer[]{l("entity.blaze.shoot", 0.55f, 1.0f), l("entity.generic.burn", 0.5f, 1.2f),
                    l("item.firecharge.use", 0.5f, 1.0f, 1)};
            case RUH -> new Layer[]{l("entity.vex.charge", 0.5f, 1.3f), l("particle.soul_escape", 1.3f, 0.85f),
                    l("block.respawn_anchor.charge", 0.35f, 1.6f, 1)};
            case KAR -> new Layer[]{l("block.glass.break", 0.4f, 1.8f), l("block.powder_snow.break", 0.9f, 1.0f),
                    l("entity.player.hurt_freeze", 0.35f, 1.4f, 1)};
            case ELMAS -> new Layer[]{l("block.glass.break", 0.55f, 1.35f), l("block.amethyst_cluster.break", 0.8f, 1.2f),
                    l("block.amethyst_block.chime", 0.7f, 1.8f, 2)};
            case KAN -> new Layer[]{l("block.honey_block.break", 0.7f, 0.8f), l("entity.warden.heartbeat", 0.7f, 1.2f),
                    l("block.slime_block.hit", 0.6f, 0.6f, 1)};
            case SIMSEK -> new Layer[]{l("entity.lightning_bolt.impact", 0.45f, 1.35f), l("entity.lightning_bolt.thunder", 0.18f, 1.7f),
                    l("block.respawn_anchor.charge", 0.4f, 1.5f, 1)};
            case SAKURA -> new Layer[]{l("block.cherry_leaves.break", 1.0f, 1.0f), l("block.amethyst_block.chime", 0.7f, 1.5f),
                    l("block.note_block.flute", 0.35f, 1.5f, 2)};
            case YAPRAK -> new Layer[]{l("block.azalea_leaves.break", 1.0f, 0.9f), l("entity.player.attack.sweep", 0.35f, 1.45f),
                    l("block.grass.hit", 0.6f, 1.1f, 1)};
            // nota blogu arpeji: kok, ucluk, besli, oktav
            case NOTA -> new Layer[]{l("block.note_block.harp", 0.8f, 1.0f), l("block.note_block.harp", 0.7f, 1.26f, 1),
                    l("block.note_block.harp", 0.7f, 1.498f, 2), l("block.note_block.bell", 0.55f, 2.0f, 3)};
            case GOKKUSAGI -> new Layer[]{l("block.amethyst_block.chime", 0.9f, 1.2f), l("entity.player.levelup", 0.22f, 1.9f),
                    l("block.amethyst_block.chime", 0.7f, 1.6f, 2)};
            case ENDER -> new Layer[]{l("entity.enderman.teleport", 0.55f, 1.15f), l("block.end_portal_frame.fill", 0.6f, 1.0f),
                    l("entity.ender_eye.death", 0.6f, 1.2f, 1)};
            case PARA -> new Layer[]{l("block.amethyst_block.chime", 0.6f, 1.9f), l("block.chain.fall", 0.6f, 1.5f),
                    l("entity.player.levelup", 0.2f, 2.0f, 1), l("entity.experience_orb.pickup", 0.5f, 1.0f, 3)};
            case KAFATASI -> new Layer[]{l("entity.wither.shoot", 0.3f, 1.3f), l("block.bone_block.break", 0.8f, 0.8f),
                    l("entity.skeleton.ambient", 0.5f, 0.8f, 2)};
            case SOK -> new Layer[]{l("entity.breeze.wind_burst", 0.8f, 1.0f), l("entity.generic.explode", 0.18f, 1.8f),
                    l("entity.player.attack.knockback", 0.6f, 1.1f, 1)};
            case NEON -> new Layer[]{l("block.beacon.activate", 0.3f, 2.0f), l("block.respawn_anchor.charge", 0.45f, 1.7f),
                    l("entity.firework_rocket.twinkle", 0.4f, 1.9f, 2)};
            case KONFETI -> new Layer[]{l("entity.firework_rocket.large_blast", 0.5f, 1.3f), l("entity.player.levelup", 0.2f, 1.8f, 1),
                    l("entity.firework_rocket.twinkle", 0.5f, 1.2f, 3)};
            case TUY -> new Layer[]{l("block.beacon.power_select", 0.3f, 1.9f), l("entity.allay.item_thrown", 0.6f, 1.4f),
                    l("block.amethyst_block.chime", 0.7f, 1.6f, 2)};
            case HILAL -> new Layer[]{l("block.respawn_anchor.set_spawn", 0.35f, 1.6f), l("entity.allay.ambient_with_item", 0.5f, 1.4f),
                    l("block.note_block.chime", 0.5f, 2.0f, 2)};
            case PAPATYA -> new Layer[]{l("block.spore_blossom.break", 0.9f, 1.2f), l("block.note_block.flute", 0.5f, 1.26f),
                    l("block.note_block.chime", 0.45f, 1.68f, 2)};
            case BUZ -> new Layer[]{l("block.glass.break", 0.6f, 1.3f), l("entity.player.hurt_freeze", 0.45f, 1.3f),
                    l("block.powder_snow.hit", 0.8f, 0.9f, 1)};
            case GIRDAP -> new Layer[]{l("entity.breeze.wind_burst", 0.5f, 1.4f), l("entity.evoker.cast_spell", 0.35f, 1.5f),
                    l("entity.breeze.idle_ground", 0.6f, 1.2f, 2)};
            case TAC -> new Layer[]{l("block.bell.use", 0.45f, 1.7f), l("entity.player.levelup", 0.25f, 1.6f),
                    l("entity.experience_orb.pickup", 0.5f, 1.1f, 2)};
            case RUN -> new Layer[]{l("entity.evoker.prepare_summon", 0.3f, 1.7f), l("block.beacon.activate", 0.35f, 1.8f),
                    l("block.enchantment_table.use", 0.8f, 1.2f, 1)};
            case YARASA -> new Layer[]{l("entity.bat.ambient", 0.5f, 1.2f), l("entity.phantom.bite", 0.35f, 1.5f),
                    l("entity.bat.takeoff", 0.5f, 1.0f, 1)};
            case PATI -> new Layer[]{l("entity.cat.purreow", 0.5f, 1.4f), l("entity.cat.beg_for_food", 0.35f, 1.3f, 2),
                    l("entity.experience_orb.pickup", 0.45f, 1.4f, 1)};
            case YONCA -> new Layer[]{l("entity.player.levelup", 0.25f, 1.9f), l("block.amethyst_block.chime", 0.7f, 1.5f),
                    l("block.cave_vines.pick_berries", 0.8f, 1.1f, 1)};
            case KESIK -> new Layer[]{l("item.trident.riptide_1", 0.35f, 1.6f), l("entity.player.attack.crit", 0.6f, 1.2f),
                    l("entity.player.attack.sweep", 0.7f, 1.2f, 1)};
            case VOLKAN -> new Layer[]{l("block.lava.extinguish", 0.4f, 1.2f), l("entity.generic.explode", 0.2f, 1.6f),
                    l("block.lava.pop", 1.0f, 0.8f, 1)};
            case ZEHIR -> new Layer[]{l("entity.witch.drink", 0.4f, 1.5f), l("entity.spider.hurt", 0.3f, 1.3f),
                    l("entity.slime.squish", 0.6f, 1.0f, 1)};
            case GALAKSI -> new Layer[]{l("entity.shulker.shoot", 0.45f, 1.5f), l("block.respawn_anchor.charge", 0.4f, 1.8f),
                    l("block.amethyst_block.chime", 0.7f, 1.9f, 2)};
            case KARADELIK -> new Layer[]{l("entity.warden.sonic_charge", 0.35f, 1.8f), l("block.respawn_anchor.deplete", 0.6f, 1.1f),
                    l("entity.enderman.teleport", 0.4f, 0.9f, 3)};
            case BALON -> new Layer[]{l("entity.puffer_fish.blow_out", 0.6f, 1.4f), l("block.bubble_column.bubble_pop", 1.0f, 1.2f),
                    l("block.bubble_column.bubble_pop", 0.8f, 1.6f, 2)};
            case ZUMRUT -> new Layer[]{l("entity.villager.celebrate", 0.3f, 1.5f), l("block.amethyst_block.chime", 0.8f, 1.7f),
                    l("block.amethyst_cluster.hit", 0.8f, 1.2f, 1)};
            case EJDERATESI -> new Layer[]{l("entity.ender_dragon.growl", 0.15f, 1.8f), l("item.firecharge.use", 0.5f, 1.2f),
                    l("entity.ender_dragon.shoot", 0.4f, 1.3f, 1)};
            case BUYU -> new Layer[]{l("entity.evoker.cast_spell", 0.4f, 1.6f), l("block.amethyst_block.chime", 0.8f, 1.8f),
                    l("entity.illusioner.cast_spell", 0.4f, 1.3f, 2)};
            case PATLAMA -> new Layer[]{l("entity.generic.explode", 0.35f, 1.2f), l("entity.dragon_fireball.explode", 0.3f, 1.5f),
                    l("entity.firework_rocket.blast", 0.5f, 0.9f, 1)};
            case MUREKKEP -> new Layer[]{l("entity.glow_squid.squirt", 0.6f, 1.0f), l("block.honey_block.break", 0.5f, 0.8f),
                    l("entity.squid.squirt", 0.4f, 0.8f, 2)};
            case FISEK -> new Layer[]{l("entity.firework_rocket.large_blast", 0.4f, 1.3f), l("entity.firework_rocket.twinkle_far", 0.5f, 1.4f, 2),
                    l("entity.firework_rocket.launch", 0.4f, 1.6f)};
            case PIKSEL -> new Layer[]{l("block.note_block.bit", 0.7f, 1.0f), l("block.note_block.bit", 0.6f, 1.5f, 1),
                    l("block.note_block.bit", 0.6f, 2.0f, 2)};
            case OKYANUS -> new Layer[]{l("entity.player.splash.high_speed", 0.3f, 1.4f), l("entity.dolphin.splash", 0.6f, 1.2f),
                    l("block.bubble_column.upwards_inside", 0.5f, 1.4f, 1)};
            case KUM -> new Layer[]{l("block.sand.break", 1.0f, 0.9f), l("entity.breeze.wind_burst", 0.3f, 1.4f),
                    l("block.suspicious_sand.break", 0.7f, 1.2f, 1)};
            case PLAZMA -> new Layer[]{l("block.beacon.activate", 0.35f, 1.8f), l("entity.shulker.shoot", 0.5f, 1.3f),
                    l("block.respawn_anchor.charge", 0.35f, 1.7f, 2)};
            case GOLGE -> new Layer[]{l("entity.phantom.bite", 0.5f, 1.0f), l("block.sculk.spread", 0.8f, 0.8f),
                    l("entity.enderman.teleport", 0.3f, 0.7f, 2)};
            case SEKER -> new Layer[]{l("entity.player.levelup", 0.25f, 1.9f), l("block.note_block.chime", 0.5f, 1.6f),
                    l("block.amethyst_block.chime", 0.6f, 1.9f, 2)};
            case ATES_BOCEGI -> new Layer[]{l("entity.allay.ambient_with_item", 0.4f, 1.8f), l("block.amethyst_block.resonate", 0.5f, 1.9f),
                    l("block.note_block.chime", 0.35f, 2.0f, 2)};
            default -> NONE;
        };
    }
}
