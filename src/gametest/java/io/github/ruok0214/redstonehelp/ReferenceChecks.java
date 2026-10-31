package io.github.ruok0214.redstonehelp;

import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.entity.SculkSensorBlockEntity;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.phys.Vec3;
import java.util.List;

/** Check reference tables against actual song items, game events and vibration listener behavior. */
final class ReferenceChecks {
    private static void check(boolean valid, String reason) { if (!valid) throw new AssertionError(reason); }
    static void run(TestSingleplayerContext world) {
        world.getServer().runOnServer(server -> {
            var level = server.overworld(); var pos = new BlockPos(0, 81, 24);
            long discs = BuiltInRegistries.ITEM.stream().filter(item -> new ItemStack(item).has(DataComponents.JUKEBOX_PLAYABLE)).count();
            check(discs == GuideTables.TRACKS.size(), "All vanilla discs must be listed");
            level.setBlockAndUpdate(pos, Blocks.JUKEBOX.defaultBlockState());
            var jukebox = (JukeboxBlockEntity) level.getBlockEntity(pos);
            for (var track : GuideTables.TRACKS) {
                var item = BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace("music_disc_" + track.id()));
                jukebox.setTheItem(new ItemStack(item));
                check(jukebox.getComparatorOutput() == track.signal(), "Disc comparator output: " + track.id());
                check(level.getBlockState(pos).getSignal(level, pos, Direction.DOWN) == 15, "Playback power: " + track.id());
                check(level.hasNeighborSignal(pos.below()), "Playback powers a hopper below");
                jukebox.getSongPlayer().stop(level, level.getBlockState(pos));
                check(level.getBlockState(pos).getSignal(level, pos, Direction.DOWN) == 0, "Stopped playback power");
                check(jukebox.getComparatorOutput() == track.signal(), "Stopped disc keeps comparator output");
                jukebox.setTheItem(ItemStack.EMPTY);
                check(jukebox.getComparatorOutput() == 0, "Empty jukebox output");
            }
            var events = List.of(GameEvent.STEP, GameEvent.PROJECTILE_LAND, GameEvent.PROJECTILE_SHOOT,
                    GameEvent.ELYTRA_GLIDE, GameEvent.EQUIP, GameEvent.SHEAR, GameEvent.ENTITY_DAMAGE,
                    GameEvent.DRINK, GameEvent.CONTAINER_CLOSE, GameEvent.NOTE_BLOCK_PLAY, GameEvent.BLOCK_CHANGE,
                    GameEvent.BLOCK_DESTROY, GameEvent.BLOCK_PLACE, GameEvent.TELEPORT, GameEvent.EXPLODE);
            for (int i = 0; i < events.size(); i++) check(VibrationSystem.getGameEventFrequency(events.get(i)) == i + 1,
                    "Reference event frequency " + (i + 1));
            check(Blocks.AMETHYST_BLOCK.defaultBlockState().is(BlockTags.VIBRATION_RESONATORS), "Amethyst resonates");
            check(!Blocks.BUDDING_AMETHYST.defaultBlockState().is(BlockTags.VIBRATION_RESONATORS), "Budding amethyst does not resonate");
            for (var entry : HelpCatalog.entries().entrySet()) {
                if (!List.of("wool", "wool_carpet").contains(entry.getValue().topic())) continue;
                var state = BuiltInRegistries.BLOCK.getValue(Identifier.withDefaultNamespace(entry.getKey())).defaultBlockState();
                check(state.is(BlockTags.DAMPENS_VIBRATIONS), "Wool/carpet dampens its own events");
                check(state.is(BlockTags.OCCLUDES_VIBRATION_SIGNALS) == entry.getValue().topic().equals("wool"), "Only full wool blocks occlude");
            }
            var source = pos.west(4);
            var wool = BuiltInRegistries.BLOCK.getValue(Identifier.withDefaultNamespace("white_wool"));
            var carpet = BuiltInRegistries.BLOCK.getValue(Identifier.withDefaultNamespace("white_carpet"));
            for (var barrier : List.of(Blocks.AIR, wool, carpet)) {
                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                level.setBlockAndUpdate(pos, Blocks.SCULK_SENSOR.defaultBlockState());
                level.setBlockAndUpdate(pos.west(2).below(), Blocks.STONE.defaultBlockState());
                level.setBlockAndUpdate(pos.west(2), barrier.defaultBlockState());
                var sensor = (SculkSensorBlockEntity) level.getBlockEntity(pos);
                boolean accepted = sensor.getListener().handleGameEvent(level, GameEvent.NOTE_BLOCK_PLAY,
                        GameEvent.Context.of(Blocks.NOTE_BLOCK.defaultBlockState()), Vec3.atCenterOf(source));
                check(accepted == (barrier != wool), "Actual vibration path through " + barrier);
                for (var dampener : List.of(wool, carpet))
                    check(!sensor.getVibrationUser().isValidVibration(GameEvent.BLOCK_PLACE, GameEvent.Context.of(dampener.defaultBlockState())), "Placement is dampened");
            }
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(pos.west(2), Blocks.AIR.defaultBlockState());
        });
    }
}
