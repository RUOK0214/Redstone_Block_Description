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
    public static Map<String, Sequence> sequences() { return SEQUENCES; }

    private static Map<String, Sequence> create() {
        var map = new LinkedHashMap<String, Sequence>();
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
        add(map, "trapped_chest", "trapped_chest");
        add(map, "container", "chest_empty", "chest_full");
        add(map, "lectern", "lectern");
        add(map, "composter", "composter_empty", "composter_ready");
        add(map, "lightning_rod", "lightning_rod");
        add(map, "jukebox", "jukebox");
        return Map.copyOf(map);
    }
    private static void add(Map<String, Sequence> map, String topic, String... captions) {
        map.put(topic, new Sequence(topic, List.of(captions)));
    }
}
