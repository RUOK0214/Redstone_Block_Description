package io.github.ruok0214.redstonehelp;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.util.FormattedCharSequence;

public record GuideTextRenderer(FormattedCharSequence text, String kind) implements ClientTooltipComponent {
    public static GuideTextRenderer from(FormattedCharSequence text) {
        String[] kind = {null};
        text.accept((index, style, codepoint) -> {
            if (style.getInsertion() != null && style.getInsertion().startsWith(GuideText.PREFIX)) {
                kind[0] = style.getInsertion().substring(GuideText.PREFIX.length()); return false;
            }
            return true;
        });
        return kind[0] == null ? null : new GuideTextRenderer(text, kind[0]);
    }
    public String section() { return kind.startsWith("section/") ? kind.substring(8) : null; }
    private int indent() { return kind.equals("paragraph") || kind.equals("continuation") ? 10 : 0; }
    @Override public int getWidth(Font font) { return font.width(text) + indent(); }
    @Override public int getHeight(Font font) { return section() != null ? 26 : kind.equals("paragraph") ? 17 : kind.equals("brand") ? 16 : 12; }
    @Override public void extractText(GuiGraphicsExtractor graphics, Font font, int x, int y) {
        int pad = section() != null ? 12 : kind.equals("paragraph") ? 5 : 0;
        if (section() != null) {
            graphics.horizontalLine(x, x + GuideText.width(net.minecraft.client.Minecraft.getInstance()) - 1, y + 5, 0xff334452);
        } else if (kind.equals("paragraph")) {
            graphics.fill(x + 2, y + pad + 3, x + 4, y + pad + 5, 0xff78b6bd);
        }
        graphics.text(font, text, x + indent(), y + pad, 0xffdbe4ed);
    }
}
