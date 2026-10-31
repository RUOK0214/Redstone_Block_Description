package io.github.ruok0214.redstonehelp;

import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CopperGolemStatueBlock;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** Compare the displayed ranges and newly described outputs with Minecraft's implementation. */
@SuppressWarnings("UnstableApiUsage")
final class ExpandedMechanicsChecks {
    private static void check(boolean valid, String reason) { if (!valid) throw new AssertionError(reason); }
    static void run(ClientGameTestContext context, TestSingleplayerContext world) {
        world.getServer().runOnServer(server -> {
            for (var block : BuiltInRegistries.BLOCK) {
                if (block.defaultBlockState().hasAnalogOutputSignal() && block.asItem() != Items.AIR) {
                    check(HelpCatalog.find(BuiltInRegistries.ITEM.getKey(block.asItem())) != null,
                            "Missing comparator-readable block: " + BuiltInRegistries.BLOCK.getKey(block));
                }
            }
            for (int slots : new int[]{1, 3, 5, 9, 27, 54}) {
                for (var item : new net.minecraft.world.item.Item[]{Items.STONE, Items.ENDER_PEARL, Items.IRON_SWORD}) {
                    int size = new ItemStack(item).getMaxStackSize();
                    var inventory = new SimpleContainer(slots);
                    var ranges = CountTables.ranges(slots, size);
                    boolean[] reached = new boolean[16];
                    for (int count = 0; count <= slots * size; count++) {
                        if (count > 0) inventory.setItem((count - 1) / size, new ItemStack(item, (count - 1) % size + 1));
                        int actual = AbstractContainerMenu.getRedstoneSignalFromContainer(inventory);
                        check(ranges.get(actual).contains(count), "Count range mismatch: " + slots + "/" + size + "/" + count + " => " + actual);
                        reached[actual] = true;
                    }
                    for (int signal = 0; signal <= 15; signal++) check(reached[signal] == (ranges.get(signal).min() >= 0), "Unreachable signal must show a dash");
                }
            }
            var brewing = new SimpleContainer(5);
            for (var row : CountTables.forItem("brewing_stand").getFirst().rows()) {
                brewing.clearContent();
                for (int bottle = 0; bottle < Integer.parseInt(row.get(0)); bottle++) brewing.setItem(bottle, new ItemStack(Items.POTION));
                brewing.setItem(3, new ItemStack(Items.NETHER_WART, Integer.parseInt(row.get(1))));
                brewing.setItem(4, new ItemStack(Items.BLAZE_POWDER, Integer.parseInt(row.get(2))));
                check(AbstractContainerMenu.getRedstoneSignalFromContainer(brewing) == Integer.parseInt(row.get(3)), "Brewing example output");
            }
            var level = server.overworld();
            var pos = new BlockPos(0, 81, 20);
            level.setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
            level.setBlockAndUpdate(pos, Blocks.OAK_SHELF.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH));
            var shelf = (Container) level.getBlockEntity(pos);
            for (int mask = 0; mask < 8; mask++) {
                for (int slot = 0; slot < 3; slot++) shelf.setItem(slot, (mask & (1 << slot)) == 0 ? ItemStack.EMPTY : new ItemStack(Items.STONE));
                check(level.getBlockState(pos).getAnalogOutputSignal(level, pos, Direction.NORTH) == mask, "Shelf bitmask " + mask);
                check(level.getBlockState(pos).getAnalogOutputSignal(level, pos, Direction.SOUTH) == 0, "Shelf is read from the back");
            }
            level.setBlockAndUpdate(pos, Blocks.CHISELED_BOOKSHELF.defaultBlockState());
            var bookshelf = (ChiseledBookShelfBlockEntity) level.getBlockEntity(pos);
            check(level.getBlockState(pos).getAnalogOutputSignal(level, pos, Direction.NORTH) == 0, "Unused bookshelf");
            for (int slot = 0; slot < 6; slot++) {
                bookshelf.setItem(slot, new ItemStack(Items.BOOK));
                check(level.getBlockState(pos).getAnalogOutputSignal(level, pos, Direction.NORTH) == slot + 1, "Bookshelf last inserted slot");
                bookshelf.removeItem(slot, 1);
                check(level.getBlockState(pos).getAnalogOutputSignal(level, pos, Direction.NORTH) == slot + 1, "Empty bookshelf retains last slot");
            }
            for (int charge = 0; charge <= 4; charge++) check(Blocks.RESPAWN_ANCHOR.defaultBlockState()
                    .setValue(BlockStateProperties.RESPAWN_ANCHOR_CHARGES, charge).getAnalogOutputSignal(level, pos, Direction.NORTH)
                    == new int[]{0, 3, 7, 11, 15}[charge], "Respawn anchor output");
            for (int bites = 0; bites <= 6; bites++) check(Blocks.CAKE.defaultBlockState().setValue(BlockStateProperties.BITES, bites)
                    .getAnalogOutputSignal(level, pos, Direction.NORTH) == (7 - bites) * 2, "Cake output");
            for (int fill = 1; fill <= 3; fill++) for (var block : new net.minecraft.world.level.block.Block[]{Blocks.WATER_CAULDRON, Blocks.POWDER_SNOW_CAULDRON})
                check(block.defaultBlockState().setValue(BlockStateProperties.LEVEL_CAULDRON, fill).getAnalogOutputSignal(level, pos, Direction.NORTH) == fill, "Cauldron output");
            check(Blocks.CAULDRON.defaultBlockState().getAnalogOutputSignal(level, pos, Direction.NORTH) == 0, "Empty cauldron");
            check(Blocks.LAVA_CAULDRON.defaultBlockState().getAnalogOutputSignal(level, pos, Direction.NORTH) == 3, "Lava cauldron");
            for (var pose : CopperGolemStatueBlock.Pose.values()) check(BuiltInRegistries.BLOCK.getValue(net.minecraft.resources.Identifier.withDefaultNamespace("copper_golem_statue")).defaultBlockState()
                    .setValue(BlockStateProperties.COPPER_GOLEM_POSE, pose).getAnalogOutputSignal(level, pos, Direction.NORTH) == pose.ordinal() + 1, "Statue pose output");
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        });
        world.getServer().runCommand("fill 3 80 19 8 80 21 stone");
        world.getServer().runCommand("setblock 3 81 20 dispenser[facing=east]");
        world.getServer().runCommand("setblock 7 81 20 dropper[facing=east]");
        world.getServer().runCommand("item replace block 3 81 20 container.0 with water_bucket");
        world.getServer().runCommand("item replace block 7 81 20 container.0 with water_bucket");
        world.getServer().runCommand("setblock 2 81 20 redstone_block");
        world.getServer().runCommand("setblock 6 81 20 redstone_block");
        context.waitTicks(6);
        world.getServer().runOnServer(server -> {
            var level = server.overworld();
            check(level.getBlockState(new BlockPos(4, 81, 20)).is(Blocks.WATER), "Dispenser places bucket contents");
            check(level.getBlockState(new BlockPos(8, 81, 20)).isAir(), "Dropper does not place bucket contents");
            check(((Container) level.getBlockEntity(new BlockPos(3, 81, 20))).getItem(0).is(Items.BUCKET), "Dispenser keeps empty bucket");
            check(((Container) level.getBlockEntity(new BlockPos(7, 81, 20))).isEmpty(), "Dropper ejects the full bucket");
            check(level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                    new net.minecraft.world.phys.AABB(7, 80, 19, 10, 83, 22)).stream().anyMatch(item -> item.getItem().is(Items.WATER_BUCKET)), "Dropped bucket item exists");
        });
    }
}
