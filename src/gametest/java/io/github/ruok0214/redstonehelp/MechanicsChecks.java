package io.github.ruok0214.redstonehelp;

import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.SculkSensorBlock;
import net.minecraft.world.level.block.CopperBulbBlock;
import net.minecraft.world.level.block.entity.SculkSensorBlockEntity;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import java.util.Arrays;

/** Check newly documented numbers and instrument mappings against the actual 26.2 world. */
@SuppressWarnings("UnstableApiUsage")
final class MechanicsChecks {
    private static void check(boolean valid, String reason) { if (!valid) throw new AssertionError(reason); }
    static void run(ClientGameTestContext context, TestSingleplayerContext world) {
        world.getServer().runCommand("forceload add -16 0 16 16");
        world.getServer().runCommand("fill -10 80 8 10 80 14 stone");
        for (String rail : new String[]{"powered_rail", "activator_rail"}) {
            int z = rail.equals("powered_rail") ? 10 : 14;
            for (int x = -9; x <= 9; x++) world.getServer().runCommand("setblock " + x + " 81 " + z + " " + rail + "[shape=east_west]");
            world.getServer().runCommand("setblock 0 80 " + z + " redstone_block");
            context.waitTicks(5);
            world.getServer().runOnServer(server -> {
                for (int x = -9; x <= 9; x++) check(server.overworld().getBlockState(new BlockPos(x, 81, z))
                        .getValue(PoweredRailBlock.POWERED) == (Math.abs(x) <= 8), rail + " propagation at " + x);
            });
            world.getServer().runCommand("setblock 1 81 " + z + " rail[shape=east_west]");
            context.waitTicks(5);
            world.getServer().runOnServer(server -> check(!server.overworld().getBlockState(new BlockPos(2, 81, z))
                    .getValue(PoweredRailBlock.POWERED), "A normal rail must interrupt " + rail + " propagation"));
        }
        world.getServer().runOnServer(server -> {
            var level = server.overworld();
            var pos = new BlockPos(0, 83, 5);
            check(Arrays.stream(NoteBlockInstrument.values()).filter(NoteBlockInstrument::isTunable).count()
                    == ExtraDetails.instruments().size(), "All tunable instruments must be documented");
            for (var pair : ExtraDetails.instruments().entrySet()) {
                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                level.setBlockAndUpdate(pos.below(), Blocks.AIR.defaultBlockState());
                level.setBlockAndUpdate(pos, Blocks.NOTE_BLOCK.defaultBlockState());
                var base = BuiltInRegistries.BLOCK.getValue(Identifier.withDefaultNamespace(pair.getKey()));
                level.setBlockAndUpdate(pos.below(), base.defaultBlockState());
                check(level.getBlockState(pos).getValue(NoteBlock.INSTRUMENT).getSerializedName().equals(pair.getValue()),
                        "Instrument mismatch: " + pair.getKey());
            }
            String[][] more = {{"grass_block", "harp"}, {"cobblestone", "basedrum"}, {"gravel", "snare"},
                    {"white_concrete_powder", "snare"}, {"sea_lantern", "hat"}, {"oak_log", "bass"},
                    {"waxed_copper_block", "trumpet"}, {"waxed_exposed_copper", "trumpet_exposed"},
                    {"waxed_weathered_copper", "trumpet_weathered"}, {"waxed_oxidized_copper", "trumpet_oxidized"}};
            for (var pair : more) check(BuiltInRegistries.BLOCK.getValue(Identifier.withDefaultNamespace(pair[0]))
                    .defaultBlockState().instrument().getSerializedName().equals(pair[1]), "Additional instrument: " + pair[0]);
            check(NoteBlock.NOTE.getPossibleValues().size() == 25, "25 note pitches");
            for (int i = 0; i < 2; i++) {
                var block = i == 0 ? Blocks.SCULK_SENSOR : Blocks.CALIBRATED_SCULK_SENSOR;
                level.setBlockAndUpdate(pos, block.defaultBlockState());
                var entity = (SculkSensorBlockEntity) level.getBlockEntity(pos);
                check(entity.getVibrationUser().getListenerRadius() == (i == 0 ? 8 : 16), "Sensor radius");
                check(((SculkSensorBlock) block).getActiveTicks() == (i == 0 ? 30 : 10), "Sensor active duration");
            }
            String[] bulbs = {"copper_bulb", "exposed_copper_bulb", "weathered_copper_bulb", "oxidized_copper_bulb"};
            int[] light = {15, 12, 8, 4};
            for (int i = 0; i < bulbs.length; i++) check(BuiltInRegistries.BLOCK.getValue(Identifier.withDefaultNamespace(bulbs[i]))
                    .defaultBlockState().setValue(CopperBulbBlock.LIT, true)
                    .getLightEmission() == light[i], "Bulb light level");
            var chest = new SimpleContainer(27);
            check(AbstractContainerMenu.getRedstoneSignalFromContainer(chest) == 0, "Empty chest signal");
            chest.setItem(0, new ItemStack(Items.STONE, 64));
            check(AbstractContainerMenu.getRedstoneSignalFromContainer(chest) == 1, "One stack signal");
            for (int slot = 1; slot < 27; slot++) chest.setItem(slot, new ItemStack(Items.STONE, 64));
            check(AbstractContainerMenu.getRedstoneSignalFromContainer(chest) == 15, "Full chest signal");
        });
    }
}
