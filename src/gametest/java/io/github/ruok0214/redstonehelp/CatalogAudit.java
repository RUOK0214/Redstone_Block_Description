package io.github.ruok0214.redstonehelp;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.DispenserBlock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.TreeMap;

/** Opt-in inventory of 26.2's own comparator blocks and registered dispenser actions. */
@SuppressWarnings("UnstableApiUsage")
public final class CatalogAudit implements FabricClientGameTest {
    @Override public void runTest(ClientGameTestContext context) {
        if (!Boolean.getBoolean("redstone_help.audit")) return;
        var data = context.computeOnClient(mc -> {
            var root = new TreeMap<String, Object>();
            var analog = new TreeMap<String, String>();
            var powered = new TreeMap<String, String>();
            var actions = new TreeMap<String, String>();
            for (var block : BuiltInRegistries.BLOCK) {
                String id = BuiltInRegistries.BLOCK.getKey(block).getPath();
                var state = block.defaultBlockState();
                if (state.hasAnalogOutputSignal()) analog.put(id, block.getClass().getSimpleName());
                if (state.getProperties().stream().anyMatch(p -> p.getName().equals("powered"))) powered.put(id, block.getClass().getSimpleName());
            }
            DispenserBlock.DISPENSER_REGISTRY.forEach((item, action) -> actions.put(BuiltInRegistries.ITEM.getKey(item).getPath(), action.getClass().getName()));
            root.put("comparator_blocks", analog); root.put("powered_blocks", powered); root.put("dispenser_actions", actions);
            return root;
        });
        try { Files.writeString(Path.of("catalog-audit.json"), new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(data)); }
        catch (java.io.IOException error) { throw new RuntimeException(error); }
    }
}
