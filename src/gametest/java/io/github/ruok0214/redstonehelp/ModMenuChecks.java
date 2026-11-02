package io.github.ruok0214.redstonehelp;

import com.terraformersmc.modmenu.api.ModMenuApi;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.gui.widget.ModListWidget;
import com.terraformersmc.modmenu.util.mod.fabric.FabricIconHandler;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

/** Loaded only in the optional Mod Menu integration run. */
@SuppressWarnings("UnstableApiUsage")
final class ModMenuChecks {
    private static void check(boolean condition, String reason) {
        if (!condition) throw new AssertionError(reason);
    }

    static void run(ClientGameTestContext context, String language) {
        var parent = context.computeOnClient(mc -> mc.gui.screen());
        boolean translate = ModMenuConfig.TRANSLATE_DESCRIPTIONS.getValue();
        try {
            context.runOnClient(mc -> {
                ModMenuConfig.TRANSLATE_DESCRIPTIONS.setValue(true);
                mc.gui.setScreen(ModMenuApi.createModsScreen(parent));
            });
            context.waitForScreen(ModsScreen.class);
            context.runOnClient(mc -> {
                var screen = (ModsScreen) mc.gui.screen();
                screen.children().stream().filter(EditBox.class::isInstance).map(EditBox.class::cast)
                        .findFirst().orElseThrow().setValue("Redstone Block Description");
                var list = screen.children().stream().filter(ModListWidget.class::isInstance)
                        .map(ModListWidget.class::cast).findFirst().orElseThrow();
                list.select(list.children().stream().filter(e -> e.getMod().getId().equals("redstone_help"))
                        .findFirst().orElseThrow());
                var mod = screen.getSelectedEntry().getMod();
                check(mod.getVersion().equals("1.0.0"), "Mod Menu shows release version");
                check(mod.getTranslatedDescription().equals(Component.translatable("modmenu.descriptionTranslation.redstone_help").getString()),
                        "Mod Menu resolves the full localized description");
                check(mod.getTranslatedSummary().equals(Component.translatable("modmenu.summaryTranslation.redstone_help").getString()),
                        "Mod Menu resolves the localized list summary");
                var container = FabricLoader.getInstance().getModContainer("redstone_help").orElseThrow();
                check(container.getMetadata().getIconPath(64).orElseThrow().equals("assets/redstone_help/icon.png"),
                        "Loader resolves the packaged icon path");
                try (var icons = new FabricIconHandler()) {
                    check(icons.createIcon(container, container.getMetadata().getIconPath(64).orElseThrow()) != null,
                            "Mod Menu successfully decodes the actual icon without its missing-icon fallback");
                }
            });
            context.waitTicks(3);
            context.takeScreenshot("modmenu-" + language);
            context.getInput().setCursorPos(800, 500);
            context.getInput().scroll(-100);
            context.waitTicks(3);
            context.takeScreenshot("modmenu-details-" + language);
            context.runOnClient(mc -> {
                ModMenuConfig.TRANSLATE_DESCRIPTIONS.setValue(false);
                var mod = ((ModsScreen) mc.gui.screen()).getSelectedEntry().getMod();
                check(mod.getTranslatedDescription().equals(mod.getDescription()) && mod.getDescription().contains("/rbd on"),
                        "English metadata remains useful with Mod Menu description translation disabled");
                mc.gui.screen().onClose();
                check(mc.gui.screen() == parent, "Mod Menu returns to its original screen");
            });
        } finally {
            context.runOnClient(mc -> ModMenuConfig.TRANSLATE_DESCRIPTIONS.setValue(translate));
        }
    }
}
