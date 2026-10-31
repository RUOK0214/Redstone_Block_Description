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
    public int columnWidth(int column) {
        return table.headers().size() == 2 ? (column == 0 ? width() - 48 : 48) : width() / table.headers().size();
    }
    public int headerHeight(Font font) { return font.split(table.title(), width()).size() * 10 + 17; }
    @Override public int getHeight(Font font) { return 10 + headerHeight(font) + table.rows().size() * 12 + 5; }
    public void drawHeader(Font font, int x, int y, GuiGraphicsExtractor graphics) {
        graphics.fill(x, y - 2, x + width(), y + headerHeight(font), 0xff101b29);
        for (var line : font.split(table.title(), width())) { graphics.text(font, line, x, y, 0xffffc857); y += 10; }
        y += 3;
        graphics.fill(x, y - 2, x + width(), y + 11, 0xff334154);
        int columnX = x;
        for (int col = 0; col < table.headers().size(); col++) {
            graphics.centeredText(font, table.headers().get(col), columnX + columnWidth(col) / 2, y, 0xffffffff);
            columnX += columnWidth(col);
        }
    }
    @Override public void extractImage(Font font, int x, int y, int width, int height, GuiGraphicsExtractor graphics) {
        y += 10;
        drawHeader(font, x, y, graphics);
        y += headerHeight(font);
        int columns = table.headers().size();
        for (int row = 0; row < table.rows().size(); row++) {
            if (row % 2 == 0) graphics.fill(x, y - 1, x + width(), y + 11, 0x66384759);
            int columnX = x;
            for (int col = 0; col < columns; col++) {
                graphics.centeredText(font, Component.literal(table.rows().get(row).get(col)), columnX + columnWidth(col) / 2, y, 0xffd9e5ef);
                columnX += columnWidth(col);
            }
            y += 12;
        }
    }
}
