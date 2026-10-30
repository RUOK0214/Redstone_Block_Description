package io.github.ruok0214.redstonehelp;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.TooltipDisplay;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

public record HelpImage(Illustrations.Sequence sequence, int frame, int panelWidth,
                        int imageWidth, int imageHeight, List<FormattedCharSequence> captions) implements TooltipComponent {
    public static Optional<TooltipComponent> forStack(ItemStack stack) {
        var mc = Minecraft.getInstance();
        if (!mc.hasShiftDown() || stack.isEmpty() || stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT).hideTooltip()) {
            return Optional.empty();
        }
        var entry = HelpCatalog.find(BuiltInRegistries.ITEM.getKey(stack.getItem()));
        if (entry == null) return Optional.empty();
        var sequence = Illustrations.forTopic(entry.topic());
        if (sequence == null) return Optional.empty();
        int frame = SlidePlayback.frame(sequence.id(), sequence.captions().size(), System.nanoTime() / 1_000_000L);
        if (mc.getResourceManager().getResource(sequence.texture(frame)).isEmpty()) return Optional.empty();
        int width = Math.min(216, mc.getWindow().getGuiScaledWidth() - 24);
        var caption = Component.translatable(sequence.captionKey(frame));
        if (sequence.captions().size() > 1) caption.append(" · ").append(Component.translatable("redstone_help.image.frame", frame + 1, sequence.captions().size()));
        var captions = new ArrayList<>(mc.font.split(caption, width));
        int captionRows = captions.size();
        for (int i = 0; i < sequence.captions().size(); i++) {
            var other = Component.translatable(sequence.captionKey(i));
            if (sequence.captions().size() > 1) other.append(" · ").append(Component.translatable("redstone_help.image.frame", i + 1, sequence.captions().size()));
            captionRows = Math.max(captionRows, mc.font.split(other, width).size());
        }
        while (captions.size() < captionRows) captions.add(Component.empty().getVisualOrderText());
        // Leave room for the complete original tooltip, captions and vanilla margins on small windows.
        int textHeight = Screen.getTooltipFromItem(mc, stack).size() * 10 + 6;
        int available = mc.getWindow().getGuiScaledHeight() - textHeight - captions.size() * 10 - 24;
        if (available < 24 || width < 48) return Optional.empty();
        int imageHeight = Math.min(width * 9 / 16, available);
        int imageWidth = imageHeight * 16 / 9;
        return Optional.of(new HelpImage(sequence, frame, width, imageWidth, imageHeight, List.copyOf(captions)));
    }
    public int height() { return imageHeight + captions.size() * 10 + 6; }
}
