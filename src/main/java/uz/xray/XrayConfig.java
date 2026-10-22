package uz.xray;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Set;

public class XrayConfig {
    public boolean xray = false;
    public boolean esp = true;
    public boolean auto = true;                 // true = faqat rudalar
    public Set<String> blocks = new HashSet<>(); // custom rejim uchun

    // Tez tekshiruv uchun tayyor to'plamlar (chizish oqimlari faqat o'qiydi)
    private static volatile Set<Block> visibleSet = Set.of();
    private static volatile Set<Block> espSet = Set.of();

    private static XrayConfig INSTANCE;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path file() { return FabricLoader.getInstance().getConfigDir().resolve("xraymod.json"); }

    public static synchronized XrayConfig get() {
        if (INSTANCE == null) {
            XrayConfig loaded = null;
            try { loaded = GSON.fromJson(Files.readString(file()), XrayConfig.class); } catch (Exception ignored) {}
            if (loaded == null) loaded = new XrayConfig();
            if (loaded.blocks == null) loaded.blocks = new HashSet<>();
            loaded.xray = false;
            INSTANCE = loaded;
            rebuildCache();
        }
        return INSTANCE;
    }

    public void save() {
        try { Files.writeString(file(), GSON.toJson(this)); } catch (Exception ignored) {}
    }

    public static String id(Block b) { return Registries.BLOCK.getId(b).getPath(); }

    public static boolean isOre(Block b) {
        String p = id(b);
        return p.endsWith("_ore") || p.equals("ancient_debris");
    }

    /** Sozlama o'zgargan har safar chaqiriladi. */
    public static void rebuildCache() {
        XrayConfig c = INSTANCE;
        if (c == null) return;
        Set<Block> vis = Collections.newSetFromMap(new IdentityHashMap<>());
        Set<Block> esp = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Block b : Registries.BLOCK) {
            boolean ore = isOre(b);
            boolean v = c.auto ? ore : c.blocks.contains(id(b));
            if (v) vis.add(b);
            if (v && ore) esp.add(b);
        }
        visibleSet = vis;
        espSet = esp;
    }

    public boolean visible(Block b) { return visibleSet.contains(b); }

    public static boolean isEsp(Block b) { return espSet.contains(b); }

    public void fillOres() {
        for (Block b : Registries.BLOCK) if (isOre(b)) blocks.add(id(b));
    }
}
