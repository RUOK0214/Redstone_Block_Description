package io.github.ruok0214.redstonehelp;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.List;

/** A persistent reading-order index; the active section is underlined, not a clickable tab. */
public final class GuideNavigation {
    public record Label(String id, Component text, int x, int y, int width) {}
    private final List<Label> labels;
    private final int height;
    public GuideNavigation(Font font, int width, List<String> sections) {
        var labels = new ArrayList<Label>();
        int x = 0, y = 0;
        for (int i = 0; i < sections.size(); i++) {
            var title = Component.literal((i + 1) + " ").append(Component.translatable("redstone_help.guide.section." + sections.get(i)));
            int size = font.width(title) + 10;
            if (x > 0 && x + size > width) { x = 0; y += 14; }
            labels.add(new Label(sections.get(i), title, x, y, size));
            x += size;
        }
        this.labels = List.copyOf(labels);
        this.height = labels.isEmpty() ? 0 : y + 20;
    }
    public int height() { return height; }
    public List<Label> labels() { return labels; }
    public void draw(GuiGraphicsExtractor graphics, Font font, int x, int y, int width, String active) {
        for (var label : labels) {
            boolean selected = label.id().equals(active);
            graphics.text(font, label.text(), x + label.x() + 3, y + label.y(), selected ? 0xffffd27d : 0xff96a9ba);
            if (selected) graphics.horizontalLine(x + label.x() + 2, x + label.x() + label.width() - 5, y + label.y() + 10, 0xffffd27d);
        }
        if (height > 0) graphics.horizontalLine(x, x + width - 1, y + height - 5, 0xff334452);
    }
}
