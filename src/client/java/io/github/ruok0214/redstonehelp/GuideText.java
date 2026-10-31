package io.github.ruok0214.redstonehelp;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/** Semantic styles survive vanilla wrapping; no visible control characters are inserted. */
public final class GuideText {
    public static final String PREFIX = "redstone_help/";
    private static final Pattern NUMBERS = Pattern.compile("[0-9]+(?:[.,][0-9]+)?(?:[~–×·][0-9]+(?:[.,][0-9]+)?)*");
    private GuideText() {}
    public static int width(Minecraft mc) { return Math.max(80, Math.min(250, mc.getWindow().getGuiScaledWidth() - 48)); }
    public static void append(Minecraft mc, List<Component> lines, String key, String kind) {
        boolean paragraph = kind.equals("paragraph");
        var text = Component.empty();
        String value = Component.translatable(key).getString();
        int labelEnd = paragraph ? value.indexOf(':') : -1;
        if (labelEnd < 0 && paragraph) labelEnd = value.indexOf('→');
        if (labelEnd >= 0) {
            text.append(Component.literal(value.substring(0, labelEnd + 1)).withStyle(s -> s.withColor(0x83ddd3).withBold(true)));
            value = value.substring(labelEnd + 1);
        }
        var matcher = NUMBERS.matcher(value);
        int last = 0;
        while (matcher.find()) {
            text.append(Component.literal(value.substring(last, matcher.start())));
            text.append(Component.literal(matcher.group()).withStyle(s -> s.withColor(0xffe4a1).withBold(true)));
            last = matcher.end();
        }
        text.append(Component.literal(value.substring(last)));
        text.withStyle(s -> s.withColor(kind.startsWith("section/") ? 0xffd27d : kind.equals("brand") ? 0xaebfd0 : 0xdbe4ed)
                .withBold(kind.startsWith("section/")));
        var wrapped = mc.font.getSplitter().splitLines(text, width(mc) - (paragraph ? 10 : 0), Style.EMPTY);
        for (int i = 0; i < wrapped.size(); i++) {
            String tag = PREFIX + (paragraph && i > 0 ? "continuation" : kind);
            var output = Component.empty();
            wrapped.get(i).visit((style, part) -> {
                output.append(Component.literal(part).setStyle(style.withInsertion(tag)));
                return Optional.empty();
            }, Style.EMPTY);
            lines.add(output);
        }
    }
}
