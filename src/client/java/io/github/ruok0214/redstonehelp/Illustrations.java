package io.github.ruok0214.redstonehelp;

import net.minecraft.resources.Identifier;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Illustrations {
    public record Sequence(String id, List<String> captions) {
        public Identifier texture(int frame) {
            return Identifier.fromNamespaceAndPath("redstone_help", "textures/guide/" + id + "/" + frame + ".png");
        }
        public String captionKey(int frame) { return "redstone_help.image." + captions.get(frame); }
    }
    private static final Map<String, Sequence> SEQUENCES = create();
    private Illustrations() {}
    public static Sequence forTopic(String topic) { return SEQUENCES.get(topic); }
    public static Sequence forItem(Identifier item, String topic) {
        if (topic.equals("chest")) return forTopic("container");
        if (!topic.equals("container")) return forTopic(topic);
        if (item.getPath().endsWith("copper_chest")) return forTopic("copper_chest");
        String id = item.getPath().endsWith("shulker_box") ? "shulker_box" : item.getPath();
        return SEQUENCES.getOrDefault(id, forTopic(topic));
    }
    public static Map<String, Sequence> sequences() { return SEQUENCES; }

    private static Map<String, Sequence> create() {
        var map = new LinkedHashMap<String, Sequence>();
        add(map, "amethyst_block", "amethyst_idle", "amethyst_resonating");
        add(map, "wool", "wool_clear", "wool_blocked");
        add(map, "wool_carpet", "carpet_passes");
        for (String topic : new String[]{"redstone", "lever", "redstone_lamp", "powered_rail", "activator_rail"}) {
            add(map, topic, "power_off", "power_on");
        }
        add(map, "redstone_block", "constant_power");
        add(map, "redstone_torch", "torch_on", "torch_off");
        add(map, "repeater", "delay_one", "delay_four");
        add(map, "comparator", "compare", "subtract");
        add(map, "observer", "before_change", "after_change");
        add(map, "stone_button", "button_up", "button_down");
        add(map, "wood_button", "oak_button_up", "oak_button_down");
        add(map, "stone_pressure_plate", "empty_plate", "entity_plate");
        add(map, "wood_pressure_plate", "oak_plate_empty", "oak_plate_entity");
        add(map, "light_weighted_pressure_plate", "empty_plate", "entity_plate");
        add(map, "heavy_weighted_pressure_plate", "empty_plate", "entity_plate");
        add(map, "daylight_detector", "day", "night");
        add(map, "target", "target_idle", "target_active");
        add(map, "tripwire_hook", "tripwire_idle", "tripwire_active");
        map.put("string", map.get("tripwire_hook"));
        add(map, "sculk_sensor", "sensor_idle", "sensor_active");
        add(map, "calibrated_sculk_sensor", "sensor_idle", "sensor_active");
        add(map, "piston", "piston_retracted", "piston_extended", "piston_left");
        add(map, "sticky_piston", "piston_retracted", "piston_extended", "piston_pulled");
        add(map, "slime_block", "before_move", "after_move");
        add(map, "honey_block", "before_move", "after_move");
        add(map, "dispenser", "dispenser_idle", "dispenser_water");
        add(map, "dropper", "dropper_idle", "dropper_item");
        add(map, "hopper", "hopper_unlocked", "hopper_locked");
        add(map, "crafter", "crafter_idle", "crafter_active");
        add(map, "copper_bulb", "bulb_off", "bulb_on", "bulb_memory");
        add(map, "note_block", "wood_below", "stone_below");
        add(map, "tnt", "tnt_idle", "tnt_primed");
        add(map, "rail", "rail");
        add(map, "detector_rail", "rail_empty", "rail_cart");
        add(map, "door", "oak_door_closed", "oak_door_open");
        add(map, "iron_door", "door_closed", "door_open");
        add(map, "trapdoor", "oak_trapdoor_closed", "oak_trapdoor_open");
        add(map, "iron_trapdoor", "trapdoor_closed", "trapdoor_open");
        add(map, "fence_gate", "oak_gate_closed", "oak_gate_open");
        add(map, "trapped_chest", "trapped_closed", "trapped_open");
        add(map, "container", "chest_closed", "chest_open");
        add(map, "barrel", "barrel_closed", "barrel_open");
        add(map, "furnace", "furnace_off", "furnace_on");
        add(map, "blast_furnace", "blast_off", "blast_on");
        add(map, "smoker", "smoker_off", "smoker_on");
        add(map, "brewing_stand", "brewing_empty", "brewing_bottles");
        add(map, "shulker_box", "shulker_closed", "shulker_open");
        add(map, "lectern", "lectern_empty", "lectern_book");
        add(map, "composter", "composter_empty", "composter_ready");
        add(map, "lightning_rod", "lightning_rod");
        add(map, "jukebox", "jukebox_idle", "jukebox_playing");
        add(map, "shelf", "shelf_empty", "shelf_items", "shelf_powered");
        add(map, "chiseled_bookshelf", "bookshelf_empty", "bookshelf_full");
        add(map, "cauldron", "cauldron_empty", "cauldron_water_one", "cauldron_water_full", "cauldron_lava", "cauldron_snow");
        add(map, "big_dripleaf", "dripleaf_flat", "dripleaf_tilt", "dripleaf_powered");
        add(map, "wind_charge", "wind_before", "wind_after");
        add(map, "beehive", "hive_empty", "hive_full");
        add(map, "cake", "cake_whole", "cake_half", "cake_last");
        add(map, "respawn_anchor", "anchor_empty", "anchor_full");
        add(map, "decorated_pot", "pot");
        add(map, "bell", "bell_off", "bell_on");
        add(map, "item_frame", "frame_empty", "frame_zero", "frame_rotated");
        add(map, "copper_golem_statue", "statue_standing", "statue_sitting", "statue_running", "statue_star");
        add(map, "creaking_heart", "heart_dormant", "heart_awake");
        add(map, "end_portal_frame", "portal_no_eye", "portal_eye");
        add(map, "command_block", "commands");
        add(map, "copper_chest", "copper_chest_closed", "copper_chest_open");
        return Map.copyOf(map);
    }
    private static void add(Map<String, Sequence> map, String topic, String... captions) {
        map.put(topic, new Sequence(topic, List.of(captions)));
    }
}
