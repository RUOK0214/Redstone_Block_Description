package io.github.ruok0214.redstonehelp;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;

/** A real, aligned table rather than spacing characters in proportional-font text. */
public record CountTableRenderer(CountTables.Table table) implements ClientTooltipComponent {
    private int width() { return Math.max(160, Math.min(250, Minecraft.getInstance().getWindow().getGuiScaledWidth() - 48)); }
    @Override public int getWidth(Font font) { return width(); }
    @Override public int getHeight(Font font) { return 10 + font.split(table.title(), width()).size() * 10 + 14 + table.rows().size() * 12 + 8; }
    @Override public void extractImage(Font font, int x, int y, int width, int height, GuiGraphicsExtractor graphics) {
        y += 10;
        for (var line : font.split(table.title(), width())) { graphics.text(font, line, x, y, 0xffffc857); y += 10; }
        y += 3;
        int columns = table.headers().size(), cellWidth = width() / columns;
        graphics.fill(x, y - 2, x + cellWidth * columns, y + 11, 0xff334154);
        for (int col = 0; col < columns; col++) graphics.centeredText(font, table.headers().get(col), x + cellWidth * col + cellWidth / 2, y, 0xffffffff);
        y += 14;
        for (int row = 0; row < table.rows().size(); row++) {
            if (row % 2 == 0) graphics.fill(x, y - 1, x + cellWidth * columns, y + 11, 0x66384759);
            for (int col = 0; col < columns; col++) graphics.centeredText(font, Component.literal(table.rows().get(row).get(col)), x + cellWidth * col + cellWidth / 2, y, 0xffd9e5ef);
            y += 12;
        }
    }
}
