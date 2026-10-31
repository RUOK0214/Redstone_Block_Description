package io.github.ruok0214.redstonehelp;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Additional information after a small gap in the scrollable Shift tooltip. */
public final class ExtraDetails {
    private static final Map<String, Integer> COUNTS = Map.ofEntries(
            Map.entry("powered_rail", 4), Map.entry("activator_rail", 2),
            Map.entry("note_block", 25), Map.entry("repeater", 2),
            Map.entry("comparator", 3), Map.entry("hopper", 3),
            Map.entry("piston", 2), Map.entry("sticky_piston", 3),
            Map.entry("sculk_sensor", 7), Map.entry("calibrated_sculk_sensor", 7), Map.entry("jukebox", 4),
            Map.entry("crafter", 2), Map.entry("copper_bulb", 2),
            Map.entry("dispenser", 24), Map.entry("dropper", 2), Map.entry("detector_rail", 2),
            Map.entry("shelf", 3), Map.entry("chiseled_bookshelf", 3), Map.entry("cauldron", 8),
            Map.entry("big_dripleaf", 3), Map.entry("wind_charge", 3), Map.entry("beehive", 3),
            Map.entry("cake", 2), Map.entry("respawn_anchor", 2), Map.entry("decorated_pot", 2),
            Map.entry("bell", 1), Map.entry("item_frame", 2), Map.entry("copper_golem_statue", 2),
            Map.entry("creaking_heart", 2), Map.entry("end_portal_frame", 1), Map.entry("command_block", 2),
            Map.entry("lightning_rod", 2), Map.entry("lectern", 2), Map.entry("composter", 2), Map.entry("target", 2));
    private ExtraDetails() {}
    public static boolean has(String topic) { return COUNTS.containsKey(topic); }
    public static List<String> keys(String topic) {
        return java.util.stream.IntStream.rangeClosed(1, COUNTS.getOrDefault(topic, 0))
                .mapToObj(i -> "redstone_help.extra." + topic + "." + i).toList();
    }
    /** Representative base blocks, checked against Minecraft's own instrument states in the game test. */
    public static Map<String, String> instruments() {
        var map = new LinkedHashMap<String, String>();
        String[][] pairs = {{"dirt", "harp"}, {"stone", "basedrum"}, {"sand", "snare"},
                {"glass", "hat"}, {"oak_planks", "bass"}, {"clay", "flute"}, {"gold_block", "bell"},
                {"white_wool", "guitar"}, {"packed_ice", "chime"}, {"bone_block", "xylophone"},
                {"iron_block", "iron_xylophone"}, {"soul_sand", "cow_bell"}, {"pumpkin", "didgeridoo"},
                {"emerald_block", "bit"}, {"hay_block", "banjo"}, {"glowstone", "pling"},
                {"copper_block", "trumpet"}, {"exposed_copper", "trumpet_exposed"},
                {"weathered_copper", "trumpet_weathered"}, {"oxidized_copper", "trumpet_oxidized"}};
        for (var pair : pairs) map.put(pair[0], pair[1]);
        return java.util.Collections.unmodifiableMap(map);
    }
}
