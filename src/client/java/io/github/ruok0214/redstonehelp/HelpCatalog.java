package io.github.ruok0214.redstonehelp;

import net.minecraft.resources.Identifier;
import java.util.LinkedHashMap;
import java.util.Map;

/** Explicit vanilla IDs keep similarly named modded items from receiving incorrect descriptions. */
public final class HelpCatalog {
    public record Entry(String topic, int details) {
        public String summaryKey() { return "redstone_help." + topic + ".summary"; }
        public String detailKey(int index) { return "redstone_help." + topic + ".detail" + index; }
    }
    private static final Map<String, Entry> ENTRIES = create();
    private HelpCatalog() {}

    public static Entry find(Identifier item) {
        return item.getNamespace().equals("minecraft") ? ENTRIES.get(item.getPath()) : null;
    }
    public static Map<String, Entry> entries() { return ENTRIES; }

    private static Map<String, Entry> create() {
        var map = new LinkedHashMap<String, Entry>();
        for (String id : new String[]{"redstone", "redstone_block", "redstone_lamp", "detector_rail", "lightning_rod"}) {
            add(map, id, 1, id);
        }
        for (String id : new String[]{"redstone_torch", "repeater",
                "observer", "lever", "daylight_detector", "target", "tripwire_hook", "string",
                "sculk_sensor", "calibrated_sculk_sensor", "piston", "sticky_piston", "slime_block",
                "honey_block", "dispenser", "dropper", "hopper", "crafter",
                "note_block", "tnt", "rail", "powered_rail", "activator_rail",
                "iron_door", "iron_trapdoor", "trapped_chest", "lectern", "composter", "jukebox"}) {
            add(map, id, 2, id);
        }
        add(map, "comparator", 3, "comparator");
        for (String id : new String[]{"chiseled_bookshelf", "cauldron", "big_dripleaf", "wind_charge", "cake",
                "respawn_anchor", "decorated_pot", "bell", "creaking_heart", "end_portal_frame"}) add(map, id, 2, id);
        add(map, "beehive", 2, "beehive", "bee_nest");
        add(map, "item_frame", 2, "item_frame", "glow_item_frame");
        add(map, "command_block", 2, "command_block", "chain_command_block", "repeating_command_block");
        add(map, "stone_button", 2, "stone_button", "polished_blackstone_button");
        add(map, "stone_pressure_plate", 1, "stone_pressure_plate", "polished_blackstone_pressure_plate");
        add(map, "light_weighted_pressure_plate", 2, "light_weighted_pressure_plate");
        add(map, "heavy_weighted_pressure_plate", 2, "heavy_weighted_pressure_plate");
        add(map, "container", 2, "chest", "barrel", "furnace", "blast_furnace", "smoker", "brewing_stand", "shulker_box");
        for (String color : new String[]{"white", "orange", "magenta", "light_blue", "yellow", "lime",
                "pink", "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"}) {
            add(map, "container", 2, color + "_shulker_box");
        }
        for (String wood : new String[]{"oak", "spruce", "birch", "jungle", "acacia", "dark_oak",
                "mangrove", "cherry", "pale_oak", "bamboo", "crimson", "warped"}) {
            add(map, "wood_button", 2, wood + "_button");
            add(map, "wood_pressure_plate", 2, wood + "_pressure_plate");
            add(map, "door", 1, wood + "_door");
            add(map, "trapdoor", 1, wood + "_trapdoor");
            add(map, "fence_gate", 2, wood + "_fence_gate");
            add(map, "shelf", 2, wood + "_shelf");
        }
        for (String wax : new String[]{"", "waxed_"}) {
            for (String age : new String[]{"", "exposed_", "weathered_", "oxidized_"}) {
                add(map, "copper_bulb", 2, wax + age + "copper_bulb");
                add(map, "door", 1, wax + age + "copper_door");
                add(map, "trapdoor", 1, wax + age + "copper_trapdoor");
                add(map, "container", 2, wax + age + "copper_chest");
                add(map, "copper_golem_statue", 2, wax + age + "copper_golem_statue");
                if (!(wax + age).isEmpty()) add(map, "lightning_rod", 1, wax + age + "lightning_rod");
            }
        }
        return Map.copyOf(map);
    }

    private static void add(Map<String, Entry> map, String topic, int details, String... ids) {
        var entry = new Entry(topic, details);
        for (String id : ids) {
            if (map.putIfAbsent(id, entry) != null) throw new IllegalStateException("Duplicate help: " + id);
        }
    }
}
