package uz.xray;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class XrayConfig {
    public boolean xray = false;
    public boolean esp = true;
    public boolean auto = true;                 // true = faqat rudalar
    public Set<String> blocks = new HashSet<>(); // custom rejim uchun

    private static XrayConfig INSTANCE;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path file() { return FabricLoader.getInstance().getConfigDir().resolve("xraymod.json"); }

    public static XrayConfig get() {
        if (INSTANCE == null) {
            try {
                INSTANCE = GSON.fromJson(Files.readString(file()), XrayConfig.class);
            } catch (Exception e) { INSTANCE = null; }
            if (INSTANCE == null) INSTANCE = new XrayConfig();
            if (INSTANCE.blocks == null) INSTANCE.blocks = new HashSet<>();
            INSTANCE.xray = false;
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

    public boolean visible(Block b) {
        return auto ? isOre(b) : blocks.contains(id(b));
    }

    public void fillOres() {
        for (Block b : Registries.BLOCK) if (isOre(b)) blocks.add(id(b));
    }
}
