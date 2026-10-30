package io.github.ruok0214.redstonehelp;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import java.util.List;

public final class RedstoneHelpClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        ClientTooltipComponentCallback.EVENT.register(component -> component instanceof HelpImage image ? new HelpImageRenderer(image) : null);
        ItemTooltipCallback.EVENT.register((stack, context, flags, lines) -> {
            if (stack.isEmpty() || lines.isEmpty()) return;
            var entry = HelpCatalog.find(BuiltInRegistries.ITEM.getKey(stack.getItem()));
            if (entry == null) return;
            var minecraft = Minecraft.getInstance();
            lines.add(Component.empty());
            appendWrapped(minecraft, lines, "redstone_help.heading", ChatFormatting.GOLD);
            appendWrapped(minecraft, lines, entry.summaryKey(), ChatFormatting.GRAY);
            if (minecraft.hasShiftDown()) {
                for (int i = 1; i <= entry.details(); i++) {
                    appendWrapped(minecraft, lines, entry.detailKey(i), ChatFormatting.AQUA);
                }
            } else {
                appendWrapped(minecraft, lines, "redstone_help.hold_shift", ChatFormatting.YELLOW);
            }
        });
    }

    private static void appendWrapped(Minecraft minecraft, List<Component> lines, String key, ChatFormatting color) {
        int width = Math.max(80, Math.min(250, minecraft.getWindow().getGuiScaledWidth() - 32));
        var text = Component.translatable(key);
        for (var line : minecraft.font.getSplitter().splitLines(text, width, Style.EMPTY)) {
            lines.add(Component.literal(line.getString()).withStyle(color));
        }
    }
}
