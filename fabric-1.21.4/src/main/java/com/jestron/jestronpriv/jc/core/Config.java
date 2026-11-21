package com.jestron.jestronpriv.jc.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jestron.jestronpriv.jc.JestronClient;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * config/jestron-client.json. Yazma: geçici dosya + atomik taşıma, önceki sağlam dosya .bak olarak
 * saklanır. Bozuk dosya silinmez, .bozuk adıyla kenara alınır ve .bak'tan devam edilir.
 */
public final class Config {
    private Config() {
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int VERSION = 1;
    /** Oyun içi test sırasında oyuncunun ayarları ezilmesin. */
    public static boolean readOnly;
    /** Bu açılışta mevcut bir ayar dosyası yüklendi mi (ilk açılış ayarları için). */
    public static boolean loadedExisting;
    private static String lastWritten = "";

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("jestron-client.json");
    }

    public static void load() {
        Path f = file();
        JsonObject root = read(f);
        if (root == null) {
            Path bak = f.resolveSibling(f.getFileName() + ".bak");
            root = read(bak);
            if (root != null) JestronClient.LOG.warn("Ayar dosyası okunamadı, yedekten (.bak) yüklendi");
        }
        if (root == null) return;
        loadedExisting = true;
        JsonElement mods = root.get("modules");
        if (mods == null || !mods.isJsonObject()) return;
        JsonObject mo = mods.getAsJsonObject();
        for (Module m : Modules.ALL) {
            JsonElement e = mo.get(m.id);
            if (e != null && e.isJsonObject()) m.load(e.getAsJsonObject());
        }
    }

    private static JsonObject read(Path f) {
        if (!Files.isRegularFile(f)) return null;
        try {
            String s = Files.readString(f, StandardCharsets.UTF_8);
            JsonElement e = JsonParser.parseString(s);
            return e != null && e.isJsonObject() ? e.getAsJsonObject() : null;
        } catch (Exception ex) {
            JestronClient.LOG.warn("Ayar dosyası bozuk: {}", f, ex);
            try {
                Files.move(f, f.resolveSibling(f.getFileName() + ".bozuk"), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ignored) {
            }
            return null;
        }
    }

    public static synchronized void save() {
        if (readOnly) return;
        JsonObject root = new JsonObject();
        root.addProperty("version", VERSION);
        JsonObject mods = new JsonObject();
        for (Module m : Modules.ALL) mods.add(m.id, m.save());
        root.add("modules", mods);
        String json = GSON.toJson(root);
        if (json.equals(lastWritten)) return;
        Path f = file();
        try {
            Files.createDirectories(f.getParent());
            Path tmp = f.resolveSibling(f.getFileName() + ".tmp");
            Files.writeString(tmp, json, StandardCharsets.UTF_8);
            if (Files.isRegularFile(f)) {
                Files.copy(f, f.resolveSibling(f.getFileName() + ".bak"), StandardCopyOption.REPLACE_EXISTING);
            }
            try {
                Files.move(tmp, f, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException atomicFail) {
                Files.move(tmp, f, StandardCopyOption.REPLACE_EXISTING);
            }
            lastWritten = json;
        } catch (IOException ex) {
            JestronClient.LOG.warn("Ayarlar kaydedilemedi", ex);
        }
    }
}
