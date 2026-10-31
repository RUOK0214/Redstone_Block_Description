package io.github.ruok0214.redstonehelp;

import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.List;

/** Vanilla 26.2 reference values; localized rows are resolved again after language/resource changes. */
public final class GuideTables {
    public record Track(String id, String title, int signal) {}
    public static final List<Track> TRACKS = List.of(
            new Track("13", "13", 1), new Track("cat", "cat", 2), new Track("blocks", "blocks", 3),
            new Track("chirp", "chirp", 4), new Track("far", "far", 5), new Track("mall", "mall", 6),
            new Track("mellohi", "mellohi", 7), new Track("stal", "stal", 8), new Track("bounce", "Bounce", 8),
            new Track("strad", "strad", 9), new Track("lava_chicken", "Lava Chicken", 9),
            new Track("ward", "ward", 10), new Track("tears", "Tears", 10),
            new Track("11", "11", 11), new Track("creator_music_box", "Creator (Music Box)", 11),
            new Track("wait", "wait", 12), new Track("creator", "Creator", 12),
            new Track("pigstep", "Pigstep", 13), new Track("precipice", "Precipice", 13),
            new Track("otherside", "otherside", 14), new Track("relic", "Relic", 14), new Track("5", "5", 15));
    private GuideTables() {}
    public static List<CountTables.Table> forItem(String itemId) {
        String id = itemId.startsWith("minecraft:") ? itemId.substring(10) : itemId;
        if (id.equals("jukebox")) {
            var rows = new ArrayList<List<String>>();
            rows.add(List.of(Component.translatable("redstone_help.table.no_disc").getString(), "0"));
            for (var track : TRACKS) rows.add(List.of(track.title(), Integer.toString(track.signal())));
            return List.of(new CountTables.Table(Component.translatable("redstone_help.table.tracks"),
                    List.of(Component.translatable("redstone_help.table.track"), Component.translatable("redstone_help.table.signal")), List.copyOf(rows)));
        }
        if (id.equals("sculk_sensor") || id.equals("calibrated_sculk_sensor")) {
            var rows = new ArrayList<List<String>>();
            for (int frequency = 1; frequency <= 15; frequency++) rows.add(List.of(
                    Component.translatable("redstone_help.frequency." + frequency).getString(), Integer.toString(frequency)));
            return List.of(new CountTables.Table(Component.translatable("redstone_help.table.frequencies"),
                    List.of(Component.translatable("redstone_help.table.event"), Component.translatable("redstone_help.table.frequency")), List.copyOf(rows)));
        }
        return CountTables.forItem(id);
    }
}
