package io.github.ruok0214.redstonehelp;

import java.util.ArrayList;
import java.util.List;

/** Named groups keep long guides searchable without changing the factual sentences. */
public final class GuideSections {
    public record Section(String id, List<String> keys) {
        public String titleKey() { return "redstone_help.guide.section." + id; }
    }
    private GuideSections() {}
    private static Section group(String topic, String id, int... indices) {
        return new Section(id, java.util.Arrays.stream(indices).mapToObj(i -> "redstone_help.extra." + topic + "." + i).toList());
    }
    private static Section range(String topic, String id, int start, int end) {
        return group(topic, id, java.util.stream.IntStream.rangeClosed(start, end).toArray());
    }
    public static List<Section> extras(String topic) {
        return switch (topic) {
            case "dispenser" -> List.of(group(topic, "selection", 1), range(topic, "projectiles", 2, 4),
                    range(topic, "buckets", 5, 6), range(topic, "tools", 7, 14), group(topic, "equipment", 18, 19),
                    group(topic, "placing", 15, 16, 17, 20, 21, 22, 23), group(topic, "exceptions", 24));
            case "note_block" -> List.of(group(topic, "tuning", 1, 2), range(topic, "instruments", 3, 23), group(topic, "heads", 24, 25));
            case "powered_rail" -> List.of(group(topic, "distance", 1, 2), group(topic, "connections", 3, 4));
            case "activator_rail" -> List.of(group(topic, "distance", 1), group(topic, "connections", 2));
            case "comparator" -> List.of(group(topic, "examples", 1, 2), group(topic, "reading", 3));
            case "repeater" -> List.of(group(topic, "signal", 1), group(topic, "locking", 2));
            case "hopper" -> List.of(group(topic, "transfer", 1, 2), group(topic, "pickup", 3));
            case "piston", "sticky_piston" -> List.of(new Section("movement", ExtraDetails.keys(topic)));
            case "sculk_sensor", "calibrated_sculk_sensor" -> List.of(group(topic, "range_time", 1, 2), group(topic, "signal", 3));
            case "shelf" -> List.of(group(topic, "swapping", 1, 3), group(topic, "comparator_output", 2));
            case "chiseled_bookshelf" -> List.of(group(topic, "comparator_output", 1), group(topic, "storage", 2, 3));
            case "cauldron" -> List.of(group(topic, "weather", 4, 5, 6), group(topic, "contents", 2, 3, 7), group(topic, "comparator_output", 1, 8));
            case "big_dripleaf" -> List.of(group(topic, "timing", 1, 2), group(topic, "power", 3));
            case "wind_charge" -> List.of(group(topic, "exceptions", 1), group(topic, "signal", 2), group(topic, "launching", 3));
            case "copper_bulb" -> List.of(group(topic, "brightness", 1), group(topic, "comparator_output", 2));
            case "beehive", "cake", "respawn_anchor", "item_frame", "copper_golem_statue", "creaking_heart",
                 "end_portal_frame", "composter" -> List.of(new Section("comparator_output", ExtraDetails.keys(topic)));
            case "lectern", "lightning_rod", "target", "detector_rail" -> List.of(new Section("signal", ExtraDetails.keys(topic)));
            default -> ExtraDetails.has(topic) ? List.of(new Section("details", ExtraDetails.keys(topic))) : List.of();
        };
    }
    public static List<Section> forItem(String itemId, HelpCatalog.Entry entry) {
        var sections = new ArrayList<Section>();
        if (entry.topic().equals("trapped_chest")) {
            sections.add(new Section("basics", List.of(entry.detailKey(3), entry.detailKey(4))));
            sections.add(new Section("signal", List.of(entry.detailKey(1))));
            sections.add(new Section("comparator_output", List.of(entry.detailKey(2))));
        } else sections.add(new Section("basics", java.util.stream.IntStream.rangeClosed(1, entry.details()).mapToObj(entry::detailKey).toList()));
        sections.addAll(extras(entry.topic()));
        if (List.of("chest", "barrel", "furnace", "blast_furnace", "smoker", "brewing_stand", "shulker_box", "copper_chest").contains(entry.topic())) {
            sections.add(new Section("comparator_output", List.of("redstone_help.container.summary", "redstone_help.container.detail1", "redstone_help.container.detail2")));
        }
        if (!CountTables.forItem(itemId).isEmpty()) {
            sections.add(new Section("formula", List.of("redstone_help.count.formula", "redstone_help.count.assumptions",
                    itemId.equals("minecraft:brewing_stand") ? "redstone_help.count.brewing" : "redstone_help.count.ranges")));
            sections.add(new Section("counts", List.of()));
        }
        return List.copyOf(sections);
    }
}
