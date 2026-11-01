package io.github.ruok0214.redstonehelp;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public final class HelpCommands {
    private HelpCommands() {}
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registry) -> {
            var root = dispatcher.register(literal("rbd")
                    .executes(context -> status(context.getSource(), true))
                    .then(literal("on").executes(context -> set(context.getSource(), true)))
                    .then(literal("off").executes(context -> set(context.getSource(), false)))
                    .then(literal("toggle").executes(context -> set(context.getSource(), !HelpSettings.enabled())))
                    .then(literal("status").executes(context -> status(context.getSource(), false))));
            dispatcher.register(literal("redstonedescription").executes(context -> status(context.getSource(), true)).redirect(root));
        });
    }
    private static int status(FabricClientCommandSource source, boolean usage) {
        source.sendFeedback(Component.translatable(HelpSettings.enabled() ? "redstone_help.command.on" : "redstone_help.command.off"));
        if (usage) source.sendFeedback(Component.translatable("redstone_help.command.usage"));
        return 1;
    }
    private static int set(FabricClientCommandSource source, boolean enabled) {
        boolean saved = HelpSettings.setEnabled(enabled);
        status(source, false);
        if (!saved) source.sendError(Component.translatable("redstone_help.command.save_error"));
        return saved ? 1 : 0;
    }
}
