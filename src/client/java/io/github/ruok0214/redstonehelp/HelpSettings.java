package io.github.ruok0214.redstonehelp;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.LoggerFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.io.IOException;

/** Client preferences only; no server permissions or world data are involved. */
public final class HelpSettings {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("redstone-block-description.json");
    private static boolean enabled = true;
    private HelpSettings() {}
    public static boolean enabled() { return enabled; }
    public static void load() {
        enabled = true;
        if (!Files.exists(FILE)) return;
        try (var reader = Files.newBufferedReader(FILE)) {
            var config = JsonParser.parseReader(reader).getAsJsonObject();
            if (config.has("enabled")) enabled = config.get("enabled").getAsBoolean();
        } catch (IOException | RuntimeException exception) {
            LoggerFactory.getLogger("redstone_help").warn("Could not read help preferences; using defaults", exception);
        }
        if (!enabled) ScrollTooltip.reset();
    }
    public static boolean setEnabled(boolean value) {
        enabled = value;
        ScrollTooltip.reset();
        var config = new JsonObject();
        config.addProperty("enabled", enabled);
        var temporary = FILE.resolveSibling(FILE.getFileName() + ".tmp");
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(temporary, config + "\n");
            try { Files.move(temporary, FILE, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (java.nio.file.AtomicMoveNotSupportedException exception) { Files.move(temporary, FILE, StandardCopyOption.REPLACE_EXISTING); }
            return true;
        } catch (IOException exception) {
            LoggerFactory.getLogger("redstone_help").warn("Could not save help preferences", exception);
            return false;
        }
    }
}
