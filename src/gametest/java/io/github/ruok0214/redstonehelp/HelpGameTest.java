package io.github.ruok0214.redstonehelp;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.TooltipDisplay;
import java.util.List;
import java.util.LinkedHashSet;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

@SuppressWarnings("UnstableApiUsage")
public final class HelpGameTest implements FabricClientGameTest {
    private static void check(boolean valid, String reason) { if (!valid) throw new AssertionError(reason); }
    private static List<Component> tooltip(Minecraft mc, ItemStack stack) { return Screen.getTooltipFromItem(mc, stack); }
    private static String text(List<Component> lines) { return String.join("", lines.stream().map(Component::getString).toList()); }

    private static void language(ClientGameTestContext context, String code) {
        context.runOnClient(mc -> {
            mc.options.languageCode = code;
            mc.getLanguageManager().setSelected(code);
            mc.reloadResourcePacks();
        });
        context.waitFor(mc -> mc.gui.overlay() == null && Language.getInstance().getOrDefault("redstone_help.heading")
                .equals(code.equals("ko_kr") ? "레드스톤 블록 설명" : "Redstone Block Description"));
    }

    private static void checkCatalog(ClientGameTestContext context) {
        context.runOnClient(mc -> {
            for (var pair : HelpCatalog.entries().entrySet()) {
                var id = Identifier.withDefaultNamespace(pair.getKey());
                check(BuiltInRegistries.ITEM.containsKey(id), "Unknown item: " + id);
                var entry = pair.getValue();
                check(Language.getInstance().has(entry.summaryKey()), "Missing summary: " + id);
                for (int i = 1; i <= entry.details(); i++) check(Language.getInstance().has(entry.detailKey(i)), "Missing detail: " + id);
                var lines = tooltip(mc, new ItemStack(BuiltInRegistries.ITEM.getValue(id)));
                check(text(lines).contains(Component.translatable("redstone_help.heading").getString()), "Callback not applied: " + id);
                check(lines.stream().noneMatch(line -> line.getString().contains("redstone_help.")), "Untranslated key: " + id);
                for (int i = 2; i < lines.size(); i++) check(mc.font.width(lines.get(i)) <= 250, "Help exceeds wrapping width: " + id);
                for (var key : ExtraDetails.keys(entry.topic())) check(Language.getInstance().has(key), "Missing extra detail: " + key);
                for (var table : GuideTables.forItem(id.toString())) {
                    check(Language.getInstance().has(table.title().getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents t ? t.getKey() : ""), "Missing table title");
                    var renderer = new CountTableRenderer(table);
                    for (int col = 0; col < table.headers().size(); col++) {
                        check(mc.font.width(table.headers().get(col)) < renderer.columnWidth(col), "Table header overflow: " + table.headers().get(col).getString());
                        for (var row : table.rows()) check(mc.font.width(row.get(col)) < renderer.columnWidth(col), "Table cell overflow: " + row.get(col));
                    }
                }
                var sequence = Illustrations.forItem(id, entry.topic());
                check(sequence != null, "Missing illustration topic: " + id);
                for (int i = 0; i < sequence.captions().size(); i++) {
                    check(mc.getResourceManager().getResource(sequence.texture(i)).isPresent(), "Missing screenshot: " + sequence.texture(i));
                    check(Language.getInstance().has(sequence.captionKey(i)), "Missing image caption: " + sequence.captionKey(i));
                }
                var image = new ItemStack(BuiltInRegistries.ITEM.getValue(id)).getTooltipImage();
                if (mc.hasShiftDown()) {
                    check(image.orElse(null) instanceof HelpImage, "Shift must include a screenshot: " + id);
                    var helpImage = (HelpImage) image.orElseThrow();
                    check(ClientTooltipComponent.create(helpImage) instanceof HelpImageRenderer, "Image renderer not registered");
                    check(helpImage.height() < mc.getWindow().getGuiScaledHeight(), "Photo exceeds screen: " + id);
                    var sections = GuideSections.forItem(id.toString(), entry);
                    for (var section : sections) {
                        check(Language.getInstance().has(section.titleKey()), "Missing section title: " + section.id());
                        check(text(lines).contains(Component.translatable(section.titleKey()).getString()), "Section title not shown: " + id);
                    }
                    var extras = GuideSections.extras(entry.topic()).stream().flatMap(s -> s.keys().stream()).toList();
                    check(extras.size() == ExtraDetails.keys(entry.topic()).size() && new java.util.HashSet<>(extras).equals(new java.util.HashSet<>(ExtraDetails.keys(entry.topic()))), "Grouped text lost or duplicated: " + id);
                    var navigation = new GuideNavigation(mc.font, GuideText.width(mc), sections.stream().map(GuideSections.Section::id).toList());
                    for (var label : navigation.labels()) check(label.x() + label.width() <= GuideText.width(mc), "Navigation overflow: " + id);
                    var styled = lines.stream().map(line -> GuideTextRenderer.from(line.getVisualOrderText())).filter(java.util.Objects::nonNull).toList();
                    check(styled.stream().filter(line -> line.section() != null).count() == sections.size(), "Section tags survive wrapping: " + id);
                    for (var line : styled) check(line.getWidth(mc.font) <= GuideText.width(mc), "Indented text overflow: " + id);
                    check(styled.stream().filter(line -> line.section() != null).allMatch(line -> line.getHeight(mc.font) >= 24), "Section separation");
                } else check(image.isEmpty(), "Image must be hidden without Shift: " + id);
            }
            check(HelpCatalog.find(Identifier.fromNamespaceAndPath("another_mod", "repeater")) == null, "Do not describe modded lookalikes");
            check(!text(tooltip(mc, new ItemStack(Items.STONE))).contains(Component.translatable("redstone_help.heading").getString()), "Unrelated item changed");
            var named = new ItemStack(Items.REPEATER);
            named.set(DataComponents.CUSTOM_NAME, Component.literal("Custom circuit part"));
            named.set(DataComponents.LORE, new ItemLore(List.of(Component.literal("Existing item lore"))));
            var namedLines = tooltip(mc, named);
            check(namedLines.getFirst().getString().equals("Custom circuit part") && text(namedLines).contains("Existing item lore"), "Original name/lore lost");
            var hidden = new ItemStack(Items.REPEATER);
            hidden.set(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(true, new LinkedHashSet<>()));
            check(tooltip(mc, hidden).isEmpty(), "Hidden tooltip must stay hidden");
            check(hidden.getTooltipImage().isEmpty(), "Hidden tooltip must not expose a screenshot");
            check(new ItemStack(Items.STONE).getTooltipImage().isEmpty(), "Unrelated item received screenshot");
        });
    }

    private static void hover(ClientGameTestContext context, int x, int y) {
        double[] pixel = context.computeOnClient(mc -> new double[]{
                x * (double) mc.getWindow().getScreenWidth() / mc.getWindow().getGuiScaledWidth(),
                y * (double) mc.getWindow().getScreenHeight() / mc.getWindow().getGuiScaledHeight()});
        context.getInput().setCursorPos(pixel[0], pixel[1]);
        context.waitTicks(4);
    }

    private static void expandedPreview(ClientGameTestContext context, net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext world,
                                        String item, String name, boolean middle) {
        context.getInput().releaseShift();
        world.getServer().runCommand("item replace entity @a hotbar.1 with minecraft:" + item);
        world.getConnection().waitForClientboundPackets();
        context.waitFor(mc -> BuiltInRegistries.ITEM.getKey(mc.player.getInventory().getItem(1).getItem()).getPath().equals(item));
        int[] point = context.computeOnClient(mc -> ((InventoryProbe) mc.gui.screen()).hotbar(1));
        hover(context, point[0], point[1]); context.getInput().holdShift(); context.waitTicks(3);
        context.runOnClient(mc -> mc.gui.toastManager().clear());
        context.takeScreenshot(name + "-top-ko");
        if (middle) { context.getInput().scroll(item.equals("chest") ? -17 : -10); context.waitTicks(3); context.takeScreenshot(name + "-middle-ko"); }
        context.getInput().scroll(-100); context.waitTicks(3);
        context.runOnClient(mc -> check(ScrollTooltip.scrollPosition() == ScrollTooltip.scrollLimit(), "Expanded details reach bottom: " + item));
        context.takeScreenshot(name + "-bottom-ko");
    }

    @Override public void runTest(ClientGameTestContext context) {
        if (Boolean.getBoolean("redstone_help.capture") || Boolean.getBoolean("redstone_help.audit")) return;
        try (var world = context.worldBuilder().create()) {
            world.getConnection().waitForChunksDownload();
            MechanicsChecks.run(context, world);
            ExpandedMechanicsChecks.run(context, world);
            ReferenceChecks.run(world);
            world.getServer().runCommand("gamemode survival @a");
            world.getServer().runCommand("item replace entity @a hotbar.0 with minecraft:comparator");
            world.getServer().runCommand("item replace entity @a hotbar.1 with minecraft:repeater");
            world.getServer().runCommand("item replace entity @a hotbar.2 with minecraft:observer");
            world.getServer().runCommand("item replace entity @a hotbar.3 with minecraft:hopper");
            world.getServer().runCommand("item replace entity @a hotbar.4 with minecraft:sticky_piston");
            world.getServer().runCommand("item replace entity @a hotbar.5 with minecraft:redstone");
            world.getServer().runCommand("item replace entity @a hotbar.6 with minecraft:lever");
            world.getServer().runCommand("item replace entity @a hotbar.7 with minecraft:note_block");
            world.getServer().runCommand("item replace entity @a hotbar.8 with minecraft:powered_rail");
            world.getConnection().waitForClientboundPackets();
            context.waitFor(mc -> !mc.player.getAbilities().instabuild && mc.player.getInventory().getItem(0).is(Items.COMPARATOR));
            context.getInput().resizeWindow(1000, 700);
            context.runOnClient(mc -> mc.options.guiScale().set(2));
            language(context, "ko_kr");
            context.runOnClient(mc -> {
                SlidePlayback.reset();
                for (long t = 0; t < 1800; t += 100) check(SlidePlayback.frame("piston", 3, t) == 0, "First slide duration");
                check(SlidePlayback.frame("piston", 3, 1800) == 1, "Slideshow advances");
                check(SlidePlayback.frame("lever", 2, 1801) == 0, "Switching subjects starts at first slide");
                check(SlidePlayback.frame("lever", 2, 2400) == 0, "Returning after hover gap restarts");
                SlidePlayback.reset();
            });
            checkCatalog(context);
            context.setScreen(() -> new InventoryProbe(Minecraft.getInstance()));
            context.waitForScreen(InventoryProbe.class);
            int[] point = context.computeOnClient(mc -> ((InventoryProbe) mc.gui.screen()).hotbar());
            hover(context, point[0], point[1]);
            context.runOnClient(mc -> {
                var lines = tooltip(mc, new ItemStack(Items.COMPARATOR));
                check(text(lines).contains("Shift") && !text(lines).contains("빼기 모드:"), "Default tooltip must be compact");
            });
            context.takeScreenshot("inventory-hover-ko");
            context.getInput().holdShift();
            context.waitTick();
            context.runOnClient(mc -> {
                var lines = tooltip(mc, new ItemStack(Items.COMPARATOR));
                check(text(lines).contains("빼기 모드:") && !text(lines).contains("Shift"), "Shift must show full detail");
            });
            checkCatalog(context);
            context.waitTicks(4);
            context.takeScreenshot("inventory-shift-ko");
            context.waitFor(mc -> new ItemStack(Items.COMPARATOR).getTooltipImage().filter(HelpImage.class::isInstance)
                    .map(HelpImage.class::cast).map(image -> image.frame() == 1).orElse(false));
            context.takeScreenshot("inventory-slideshow-second-ko");
            context.getInput().releaseShift();
            context.getInput().holdShift();
            context.waitTick();
            context.runOnClient(mc -> check(((HelpImage) new ItemStack(Items.COMPARATOR).getTooltipImage().orElseThrow()).frame() == 0,
                    "Quick Shift release and repress restarts the slideshow"));
            context.takeScreenshot("inventory-slideshow-restart-ko");
            point = context.computeOnClient(mc -> ((InventoryProbe) mc.gui.screen()).hotbar(5));
            hover(context, point[0], point[1]);
            context.takeScreenshot("redstone-dust-shift-ko");
            point = context.computeOnClient(mc -> ((InventoryProbe) mc.gui.screen()).hotbar(6));
            hover(context, point[0], point[1]);
            context.takeScreenshot("lever-shift-ko");
            point = context.computeOnClient(mc -> ((InventoryProbe) mc.gui.screen()).hotbar(7));
            hover(context, point[0], point[1]);
            context.takeScreenshot("note-block-shift-top-ko");
            context.runOnClient(mc -> check(ScrollTooltip.scrollLimit() > 0 && ScrollTooltip.scrollPosition() == 0, "Long Shift tooltip starts at top"));
            context.getInput().scroll(-6);
            context.waitTicks(3);
            context.runOnClient(mc -> check(ScrollTooltip.scrollPosition() > 0, "Real mouse-wheel input scrolls the tooltip"));
            context.takeScreenshot("note-block-shift-middle-ko");
            context.getInput().scroll(-100);
            context.waitTicks(3);
            context.runOnClient(mc -> check(ScrollTooltip.scrollPosition() == ScrollTooltip.scrollLimit(), "Bottom clamp"));
            context.takeScreenshot("note-block-shift-bottom-ko");
            context.runOnClient(mc -> check(!ScrollTooltip.activeSection().equals("basics"), "Index follows scrolled section"));
            point = context.computeOnClient(mc -> ((InventoryProbe) mc.gui.screen()).hotbar(8));
            hover(context, point[0], point[1]);
            context.runOnClient(mc -> check(ScrollTooltip.scrollPosition() == 0, "Different item resets scroll"));
            context.getInput().scroll(-100);
            context.waitTicks(3);
            context.takeScreenshot("powered-rail-details-ko");
            context.getInput().releaseShift(); context.waitTick();
            context.getInput().holdShift(); context.waitTicks(3);
            context.runOnClient(mc -> check(ScrollTooltip.scrollPosition() == 0, "Releasing Shift resets scroll"));
            expandedPreview(context, world, "chest", "chest-counts", true);
            expandedPreview(context, world, "barrel", "barrel-functions", true);
            expandedPreview(context, world, "shulker_box", "shulker-functions", false);
            expandedPreview(context, world, "furnace", "furnace-functions", false);
            expandedPreview(context, world, "hopper", "hopper-counts", false);
            expandedPreview(context, world, "brewing_stand", "brewing-counts", false);
            expandedPreview(context, world, "dispenser", "dispenser-actions", true);
            expandedPreview(context, world, "oak_shelf", "shelf-details", false);
            expandedPreview(context, world, "cauldron", "cauldron-details", true);
            expandedPreview(context, world, "jukebox", "jukebox-tracks", true);
            expandedPreview(context, world, "lectern", "lectern-pages", true);
            expandedPreview(context, world, "beehive", "beehive-function", false);
            expandedPreview(context, world, "amethyst_block", "amethyst-resonance", false);
            expandedPreview(context, world, "white_wool", "wool-vibrations", false);
            expandedPreview(context, world, "white_carpet", "carpet-vibrations", false);
            expandedPreview(context, world, "calibrated_sculk_sensor", "sculk-frequencies", true);
            context.getInput().resizeWindow(854, 480);
            context.waitTicks(3);
            expandedPreview(context, world, "chest", "chest-counts-small", true);
            context.runOnClient(mc -> {
                check(ScrollTooltip.activeSection().equals("counts"), "Index identifies the count tables");
                check(ScrollTooltip.tableHeaderPinned(), "Small-screen table keeps its headers visible");
            });
            expandedPreview(context, world, "jukebox", "jukebox-tracks-small", false);
            context.runOnClient(mc -> {
                check(ScrollTooltip.activeSection().equals("tracks"), "Index identifies the disc table");
                check(ScrollTooltip.tableHeaderPinned(), "Small-screen disc table keeps its headers visible");
            });
            point = context.computeOnClient(mc -> ((InventoryProbe) mc.gui.screen()).hotbar());
            hover(context, point[0], point[1]);
            checkCatalog(context);
            context.waitTicks(4);
            context.takeScreenshot("inventory-small-shift-ko");
            context.getInput().releaseShift();
            context.waitTick();
            context.runOnClient(mc -> check(text(tooltip(mc, new ItemStack(Items.COMPARATOR))).contains("Shift"), "Releasing Shift must restore compact help"));

            context.setScreen(() -> new ChestProbe(Minecraft.getInstance()));
            point = context.computeOnClient(mc -> ((ChestProbe) mc.gui.screen()).firstSlot());
            hover(context, point[0], point[1]);
            context.takeScreenshot("chest-hover-ko");
            context.setScreen(() -> null);
            world.getServer().runCommand("gamemode creative @a");
            world.getConnection().waitForClientboundPackets();
            context.waitFor(mc -> mc.player.getAbilities().instabuild);
            context.setScreen(() -> new CreativeProbe(Minecraft.getInstance()));
            point = context.computeOnClient(mc -> ((CreativeProbe) mc.gui.screen()).hotbar());
            hover(context, point[0], point[1]);
            context.takeScreenshot("creative-hover-ko");

            language(context, "en_us");
            checkCatalog(context);
            context.getInput().holdShift();
            checkCatalog(context);
            context.waitTicks(3);
            context.takeScreenshot("creative-shift-en");
            context.getInput().scroll(-100); context.waitTicks(3);
            context.takeScreenshot("creative-details-en");
            context.getInput().releaseShift();
            context.setScreen(() -> null);
        }
    }

    private static final class InventoryProbe extends InventoryScreen {
        InventoryProbe(Minecraft mc) { super(mc.player); }
        int[] hotbar() { return hotbar(0); }
        int[] hotbar(int index) { var slot = getMenu().slots.get(36 + index); return new int[]{leftPos + slot.x + 8, topPos + slot.y + 8}; }
    }
    private static final class ChestProbe extends ContainerScreen {
        ChestProbe(Minecraft mc) { super(ChestMenu.threeRows(0, mc.player.getInventory(), contents()), mc.player.getInventory(), Component.translatable("container.chest")); }
        static SimpleContainer contents() { var box = new SimpleContainer(27); box.setItem(0, new ItemStack(Items.HOPPER)); return box; }
        int[] firstSlot() { var slot = getMenu().slots.getFirst(); return new int[]{leftPos + slot.x + 8, topPos + slot.y + 8}; }
    }
    private static final class CreativeProbe extends CreativeModeInventoryScreen {
        CreativeProbe(Minecraft mc) { super(mc.player, mc.level.enabledFeatures(), false); }
        int[] hotbar() {
            var slot = getMenu().slots.stream().filter(s -> s.getItem().is(Items.COMPARATOR)).findFirst().orElseThrow();
            return new int[]{leftPos + slot.x + 8, topPos + slot.y + 8};
        }
    }
}
