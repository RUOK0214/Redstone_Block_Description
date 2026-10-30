package io.github.ruok0214.redstonehelp;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;

public record HelpImageRenderer(HelpImage image) implements ClientTooltipComponent {
    @Override public int getWidth(Font font) { return image.panelWidth(); }
    @Override public int getHeight(Font font) { return image.height(); }
    @Override public void extractImage(Font font, int x, int y, int width, int height, GuiGraphicsExtractor graphics) {
        int left = x + (width - image.imageWidth()) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, image.sequence().texture(image.frame()), left, y,
                0, 0, image.imageWidth(), image.imageHeight(), 640, 360, 640, 360);
        graphics.outline(left - 1, y - 1, image.imageWidth() + 2, image.imageHeight() + 2, 0xff687583);
        int captionY = y + image.imageHeight() + 3;
        for (var line : image.captions()) {
            graphics.text(font, line, x, captionY, 0xffc7d3df);
            captionY += 10;
        }
    }
}
