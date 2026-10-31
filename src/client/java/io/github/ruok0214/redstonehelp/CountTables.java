package io.github.ruok0214.redstonehelp;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Item-count ranges for uniform maximum stack sizes; mixed inventories need per-slot fullness. */
public final class CountTables {
    public record Range(int min, int max) {
        public boolean contains(int count) { return min >= 0 && count >= min && count <= max; }
        public String text() { return min < 0 ? "—" : min == max ? Integer.toString(min) : min + "–" + max; }
    }
    public record Table(Component title, List<Component> headers, List<List<String>> rows) {}
    private static final Map<String, List<Range>> CACHE = new ConcurrentHashMap<>();
    private static final Map<String, List<Table>> TABLES = new ConcurrentHashMap<>();
    private CountTables() {}
    public static int signal(int slots, int stackSize, int count) {
        float fullness = 0;
        for (int slot = 0; slot < slots; slot++) fullness += (float) Math.max(0, Math.min(stackSize, count - slot * stackSize)) / stackSize;
        return Mth.lerpDiscrete(fullness / slots, 0, 15);
    }
    public static List<Range> ranges(int slots, int stackSize) {
        return CACHE.computeIfAbsent(slots + ":" + stackSize, key -> {
            int[] min = new int[16], max = new int[16];
            java.util.Arrays.fill(min, -1); java.util.Arrays.fill(max, -1);
            for (int count = 0; count <= slots * stackSize; count++) {
                int signal = signal(slots, stackSize, count);
                if (min[signal] < 0) min[signal] = count;
                max[signal] = count;
            }
            var rows = new ArrayList<Range>();
            for (int signal = 0; signal <= 15; signal++) rows.add(new Range(min[signal], max[signal]));
            return List.copyOf(rows);
        });
    }
    private static Table inventory(int slots, boolean allStackSizes, boolean large) {
        int[] sizes = allStackSizes ? new int[]{64, 16, 1} : new int[]{64};
        var headers = new ArrayList<Component>();
        for (int size : sizes) headers.add(Component.translatable("redstone_help.table.stack", size));
        headers.add(Component.translatable("redstone_help.table.signal"));
        var rows = new ArrayList<List<String>>();
        for (int signal = 0; signal <= 15; signal++) {
            var row = new ArrayList<String>();
            for (int size : sizes) row.add(ranges(slots, size).get(signal).text());
            row.add(Integer.toString(signal));
            rows.add(List.copyOf(row));
        }
        return new Table(Component.translatable(large ? "redstone_help.table.double" : "redstone_help.table.slots", slots), List.copyOf(headers), List.copyOf(rows));
    }
    public static List<Table> forItem(String itemId) {
        String id = itemId.startsWith("minecraft:") ? itemId.substring(10) : itemId;
        return TABLES.computeIfAbsent(id, CountTables::createTables);
    }
    private static List<Table> createTables(String id) {
        if (id.equals("chest") || id.equals("trapped_chest") || id.endsWith("copper_chest")) return List.of(inventory(27, true, false), inventory(54, true, true));
        if (id.equals("barrel") || id.endsWith("shulker_box")) return List.of(inventory(27, true, false));
        if (id.equals("hopper")) return List.of(inventory(5, true, false));
        if (id.equals("dispenser") || id.equals("dropper")) return List.of(inventory(9, true, false));
        if (id.equals("decorated_pot")) return List.of(inventory(1, true, false));
        if (List.of("furnace", "blast_furnace", "smoker").contains(id)) return List.of(inventory(3, false, false));
        if (id.equals("brewing_stand")) return List.of(new Table(Component.translatable("redstone_help.table.brewing"),
                List.of(Component.translatable("redstone_help.table.bottles"), Component.translatable("redstone_help.table.ingredient"),
                        Component.translatable("redstone_help.table.fuel"), Component.translatable("redstone_help.table.signal")),
                List.of(List.of("0", "0", "0", "0"), List.of("1", "0", "0", "3"), List.of("2", "0", "0", "6"),
                        List.of("3", "0", "0", "9"), List.of("0", "64", "0", "3"), List.of("0", "0", "64", "3"),
                        List.of("3", "64", "0", "12"), List.of("3", "0", "64", "12"), List.of("3", "64", "64", "15"))));
        return List.of();
    }
}
