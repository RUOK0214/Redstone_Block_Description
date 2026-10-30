package io.github.ruok0214.redstonehelp;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import org.lwjgl.glfw.GLFW;
import java.nio.file.Path;
import java.nio.file.Files;

/** Reproducible, HUD-free photographs of a disposable Minecraft world. */
@SuppressWarnings("UnstableApiUsage")
public final class IllustrationCapture implements FabricClientGameTest {
    private ClientGameTestContext context;
    private TestSingleplayerContext world;
    private final Path output = Path.of("illustration-captures").toAbsolutePath();

    private void command(String... commands) { for (String command : commands) world.getServer().runCommand(command); }
    private void block(int x, int y, int z, String block) {
        command("setblock " + x + " " + y + " " + z + " minecraft:" + block);
        checkBlock(x, y, z, block.split("\\[")[0]);
    }
    private void floor() {
        command("fill -8 81 -6 8 89 6 air", "fill -16 80 -16 16 80 16 light_gray_concrete");
        world.getServer().runOnServer(server -> {
            var entities = new java.util.ArrayList<net.minecraft.world.entity.Entity>();
            server.overworld().getAllEntities().forEach(entities::add);
            for (var entity : entities) if (!(entity instanceof net.minecraft.world.entity.player.Player)) entity.discard();
        });
        camera(4, 84, 5.5, 144, 32);
    }
    private void camera(double x, double y, double z, float yaw, float pitch) {
        command("tp @a " + x + " " + y + " " + z + " " + yaw + " " + pitch);
        world.getConnection().waitForClientboundPackets();
        context.runOnClient(mc -> { mc.player.snapTo(x, y, z, yaw, pitch); mc.player.setOldPosAndRot(); });
    }
    private void step(int ticks) {
        if (ticks > 0) {
            command("tick step " + ticks);
            context.waitTicks(ticks + 4);
        }
        world.getConnection().waitForClientboundPackets();
        context.waitTicks(4);
    }
    private void photo(String topic, int frame, int ticks) {
        world.getServer().runOnServer(server -> {
            var level = server.overworld();
            for (var pos : BlockPos.betweenClosed(-5, 81, -2, 5, 84, 2)) level.updateNeighborsAt(pos.immutable(), level.getBlockState(pos).getBlock());
        });
        step(ticks);
        verifyState(topic, frame);
        context.runOnClient(mc -> { mc.gui.toastManager().clear(); mc.particleEngine.clearParticles(); });
        try { Files.createDirectories(output.resolve(topic)); } catch (java.io.IOException exception) { throw new RuntimeException(exception); }
        context.takeScreenshot(TestScreenshotOptions.of(topic + "/" + frame).disableCounterPrefix().withDestinationDir(output));
    }
    private void input(boolean powered) { block(-2, 81, 0, "lever[face=floor,facing=east,powered=" + powered + "]"); }
    private void wire(int x, int z) { block(x, 81, z, "redstone_wire"); }
    private void ordinaryCircuit(String center) {
        floor(); input(false); wire(-1, 0); block(0, 81, 0, center); wire(1, 0); block(2, 81, 0, "redstone_lamp");
    }
    private void toggled(String topic, String center) {
        ordinaryCircuit(center);
        photo(topic, 0, 5); input(true); photo(topic, 1, 5);
    }
    private void checkBlock(int x, int y, int z, String id) {
        boolean correct = world.getServer().computeOnServer(server -> BuiltInRegistries.BLOCK.getKey(
                server.overworld().getBlockState(new BlockPos(x, y, z)).getBlock()).getPath().equals(id));
        if (!correct) throw new AssertionError("Capture block missing: " + id);
    }
    private void property(int x, int y, int z, String name, String expected) {
        String actual = world.getServer().computeOnServer(server -> server.overworld().getBlockState(new BlockPos(x, y, z)).getValues()
                .filter(pair -> pair.property().getName().equals(name)).map(pair -> pair.valueName()).findFirst().orElse("missing"));
        if (!actual.equals(expected)) throw new AssertionError("Capture state at " + x + "," + y + "," + z + " " + name + " expected " + expected + ", got " + actual);
    }
    private void verifyState(String topic, int frame) {
        switch (topic) {
            case "redstone", "lever", "stone_button", "wood_button", "stone_pressure_plate", "wood_pressure_plate", "light_weighted_pressure_plate", "heavy_weighted_pressure_plate", "observer", "target", "trapped_chest", "jukebox", "sculk_sensor", "calibrated_sculk_sensor" -> property(2, 81, 0, "lit", Boolean.toString(frame == 1));
            case "detector_rail" -> { property(0, 81, 0, "powered", Boolean.toString(frame == 1)); property(0, 81, 2, "lit", Boolean.toString(frame == 1)); }
            case "barrel" -> property(0, 81, 0, "open", Boolean.toString(frame == 1));
            case "furnace", "blast_furnace", "smoker" -> property(0, 81, 0, "lit", Boolean.toString(frame == 1));
            case "redstone_block" -> property(1, 81, 0, "lit", "true");
            case "repeater" -> property(2, 81, 0, "lit", "true");
            case "comparator" -> property(2, 81, 0, "lit", Boolean.toString(frame == 0));
            case "redstone_torch" -> property(2, 82, 0, "lit", Boolean.toString(frame == 0));
            case "redstone_lamp" -> property(0, 81, 0, "lit", Boolean.toString(frame == 1));
            case "copper_bulb" -> property(0, 81, 0, "lit", Boolean.toString(frame > 0));
            case "hopper" -> property(0, 81, 0, "enabled", Boolean.toString(frame == 0));
            case "door", "iron_door", "trapdoor", "iron_trapdoor", "fence_gate" -> property(0, 81, 0, "open", Boolean.toString(frame == 1));
            case "tripwire_hook" -> { property(-3, 81, 0, "attached", "true"); property(-3, 81, 0, "powered", Boolean.toString(frame == 1)); }
            default -> { }
        }
    }
    @Override public void runTest(ClientGameTestContext context) {
        if (!Boolean.getBoolean("redstone_help.capture")) return;
        this.context = context;
        try (var world = context.worldBuilder().create()) {
            this.world = world;
            Files.createDirectories(output);
            world.getConnection().waitForChunksDownload();
            command("gamemode spectator @a", "time set noon", "weather clear", "tick freeze");
            context.getInput().resizeWindow(640, 360);
            context.runOnClient(mc -> { mc.options.fov().set(32); mc.options.bobView().set(false); mc.options.gamma().set(1.0); });
            context.getInput().pressKey(GLFW.GLFW_KEY_F1);
            floor();
            if (Boolean.getBoolean("redstone_help.captureRevised")) { captureRevised(); return; }
            input(false); wire(-1, 0); wire(0, 0); wire(1, 0); block(2, 81, 0, "redstone_lamp");
            photo("redstone", 0, 5); input(true); photo("redstone", 1, 5);
            toggled("lever", "redstone_wire");
            floor(); input(false); wire(-1, 0); block(0, 81, 0, "piston[facing=east]"); block(1, 81, 0, "stone");
            photo("piston", 0, 5); input(true); photo("piston", 1, 5); input(false); photo("piston", 2, 5);
            checkBlock(2, 81, 0, "stone");
            if (Boolean.getBoolean("redstone_help.capturePreview")) return;
            captureRemaining();
        } catch (Exception exception) { throw new RuntimeException(exception); }
        finally { context.getInput().pressKey(GLFW.GLFW_KEY_F1); }
    }

    private void captureRemaining() {
        floor(); block(-2, 81, 0, "redstone_block"); wire(-1, 0); wire(0, 0); block(1, 81, 0, "redstone_lamp"); photo("redstone_block", 0, 5);
        floor(); input(false); wire(-1, 0); block(0, 81, 0, "stone"); block(0, 82, 0, "redstone_torch");
        block(1, 81, 0, "stone"); block(1, 82, 0, "redstone_wire"); block(2, 82, 0, "redstone_lamp");
        photo("redstone_torch", 0, 6); input(true); photo("redstone_torch", 1, 6);
        ordinaryCircuit("repeater[facing=west,delay=1]"); input(true); photo("repeater", 0, 6);
        block(0, 81, 0, "repeater[facing=west,delay=4]"); photo("repeater", 1, 10);
        floor(); block(-1, 81, 0, "redstone_block"); block(0, 81, 0, "comparator[facing=west,mode=compare]");
        block(0, 81, -2, "redstone_block"); wire(0, -1); wire(1, 0); block(2, 81, 0, "redstone_lamp");
        photo("comparator", 0, 6); block(0, 81, 0, "comparator[facing=west,mode=subtract]"); photo("comparator", 1, 6);
        floor(); block(-1, 81, 0, "stone"); block(0, 81, 0, "observer[facing=west]"); wire(1, 0); block(2, 81, 0, "redstone_lamp");
        photo("observer", 0, 8); block(-1, 81, 0, "gold_block"); photo("observer", 1, 2);
        for (String material : new String[]{"stone", "oak"}) {
            floor(); block(0, 81, 0, material + "_button[face=floor,powered=false]"); wire(1, 0); block(2, 81, 0, "redstone_lamp");
            String topic = material.equals("oak") ? "wood_button" : "stone_button";
            photo(topic, 0, 4);
            world.getServer().runOnServer(server -> {
                var pos = new BlockPos(0, 81, 0); var level = server.overworld(); var state = level.getBlockState(pos);
                ((net.minecraft.world.level.block.ButtonBlock) state.getBlock()).press(state, level, pos, server.getPlayerList().getPlayers().getFirst());
            });
            photo(topic, 1, 2);
        }
        for (String material : new String[]{"stone", "oak"}) {
            floor(); block(0, 81, 0, material + "_pressure_plate"); wire(1, 0); block(2, 81, 0, "redstone_lamp");
            String topic = material.equals("oak") ? "wood_pressure_plate" : "stone_pressure_plate";
            photo(topic, 0, 4); command("summon pig 0.5 81 0.5 {NoAI:1b}"); photo(topic, 1, 3);
        }
        for (String weight : new String[]{"light", "heavy"}) {
            floor(); block(0, 81, 0, weight + "_weighted_pressure_plate"); wire(1, 0); block(2, 81, 0, "redstone_lamp");
            photo(weight + "_weighted_pressure_plate", 0, 3);
            command("summon pig 0.5 81 0.5 {NoAI:1b}"); photo(weight + "_weighted_pressure_plate", 1, 3);
        }
        floor(); block(0, 81, 0, "daylight_detector"); wire(1, 0); block(2, 81, 0, "redstone_lamp");
        command("time set noon"); photo("daylight_detector", 0, 25); command("time set midnight"); photo("daylight_detector", 1, 25); command("time set noon");
        floor(); block(0, 81, 0, "target"); wire(1, 0); block(2, 81, 0, "redstone_lamp"); photo("target", 0, 5);
        block(0, 81, 0, "target[power=15]"); photo("target", 1, 0);
        floor(); camera(0.5, 83, 9, 180, 20);
        block(-4, 81, 0, "stone"); block(4, 81, 0, "stone"); block(-3, 81, 0, "tripwire_hook[facing=east]"); block(3, 81, 0, "tripwire_hook[facing=west]");
        for (int x = -2; x <= 2; x++) block(x, 81, 0, "tripwire");
        photo("tripwire_hook", 0, 6); command("summon pig 0.5 81 0.5 {NoAI:1b}"); photo("tripwire_hook", 1, 3);
        for (String sensor : new String[]{"sculk_sensor", "calibrated_sculk_sensor"}) {
            floor(); block(0, 81, 0, sensor); wire(1, 0); block(2, 81, 0, "redstone_lamp");
            photo(sensor, 0, 0);
            world.getServer().runOnServer(server -> {
                var pos = new BlockPos(0, 81, 0); var level = server.overworld(); var state = level.getBlockState(pos);
                ((net.minecraft.world.level.block.SculkSensorBlock) state.getBlock()).activate(null, level, pos, state, 15, 1);
            });
            photo(sensor, 1, 0);
        }
        floor(); input(false); wire(-1, 0); block(0, 81, 0, "sticky_piston[facing=east]"); block(1, 81, 0, "stone");
        photo("sticky_piston", 0, 5); input(true); photo("sticky_piston", 1, 5); input(false); photo("sticky_piston", 2, 5); checkBlock(1, 81, 0, "stone");
        for (String sticky : new String[]{"slime_block", "honey_block"}) {
            floor(); camera(4, 85.5, 5.5, 144, 31);
            block(0, 83, 0, "sticky_piston[facing=east]"); block(1, 83, 0, sticky); block(1, 83, 1, "stone"); block(1, 83, -1, "stone");
            photo(sticky, 0, 5); block(-1, 83, 0, "redstone_block"); photo(sticky, 1, 5);
        }
        floor(); block(0, 81, 0, "dispenser[facing=east]"); command("item replace block 0 81 0 container.0 with water_bucket");
        photo("dispenser", 0, 3); block(-1, 81, 0, "redstone_block"); photo("dispenser", 1, 5);
        floor(); block(0, 81, 0, "dropper[facing=east]"); command("item replace block 0 81 0 container.0 with diamond");
        photo("dropper", 0, 3); block(-1, 81, 0, "redstone_block"); photo("dropper", 1, 5);
        floor(); block(0, 81, 0, "hopper[facing=east]"); block(1, 81, 0, "chest"); block(0, 82, 0, "chest"); block(-1, 81, 0, "lever[face=floor,powered=false]");
        photo("hopper", 0, 3); block(-1, 81, 0, "lever[face=floor,powered=true]"); photo("hopper", 1, 3);
        floor(); block(0, 81, 0, "crafter[orientation=east_up]"); command("item replace block 0 81 0 container.0 with oak_log");
        photo("crafter", 0, 3); block(-1, 81, 0, "redstone_block"); photo("crafter", 1, 5);
        floor(); input(false); wire(-1, 0); block(0, 81, 0, "redstone_lamp"); photo("redstone_lamp", 0, 5); input(true); photo("redstone_lamp", 1, 5);
        floor(); input(false); wire(-1, 0); block(0, 81, 0, "copper_bulb"); photo("copper_bulb", 0, 3); input(true); photo("copper_bulb", 1, 3); input(false); photo("copper_bulb", 2, 3);
        floor(); block(0, 81, 0, "oak_planks"); block(0, 82, 0, "note_block"); photo("note_block", 0, 3);
        block(0, 81, 0, "stone"); photo("note_block", 1, 3);
        floor(); block(0, 81, 0, "tnt"); photo("tnt", 0, 3); block(0, 81, 0, "air"); command("summon tnt 0.5 81 0.5"); photo("tnt", 1, 0);
        for (String rail : new String[]{"rail", "powered_rail", "activator_rail"}) {
            floor(); for (int x = -2; x <= 2; x++) block(x, 81, 0, rail + "[shape=east_west]");
            photo(rail, 0, 3);
            if (rail.equals("detector_rail")) { command("summon minecart 0.5 81.1 0.5"); photo(rail, 1, 2); }
            else if (!rail.equals("rail")) { block(0, 81, -1, "redstone_block"); photo(rail, 1, 3); }
        }
        for (String material : new String[]{"oak", "iron"}) {
            floor(); input(false); wire(-1, 0); block(0, 81, 0, material + "_door[half=lower,facing=west]"); block(0, 82, 0, material + "_door[half=upper,facing=west]");
            String topic = material.equals("oak") ? "door" : "iron_door";
            photo(topic, 0, 3); input(true); photo(topic, 1, 3);
            floor(); input(false); wire(-1, 0); block(0, 81, 0, material + "_trapdoor[facing=west]");
            topic = material.equals("oak") ? "trapdoor" : "iron_trapdoor";
            photo(topic, 0, 3); input(true); photo(topic, 1, 3);
        }
        floor(); input(false); wire(-1, 0); block(0, 81, 0, "oak_fence_gate[facing=west]"); photo("fence_gate", 0, 3); input(true); photo("fence_gate", 1, 3);
        floor(); block(0, 81, 0, "lightning_rod[facing=up]"); wire(1, 0); block(2, 81, 0, "redstone_lamp"); photo("lightning_rod", 0, 3);
        captureRevised();
    }

    private void openContainer() {
        world.getServer().runOnServer(server -> {
            var level = server.overworld();
            var pos = new BlockPos(0, 81, 0);
            var container = (net.minecraft.world.MenuProvider) level.getBlockEntity(pos);
            // A real player opens the menu, producing the same state and block events as ordinary use.
            var player = server.getPlayerList().getPlayers().getFirst();
            player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
            player.openMenu(container);
        });
        step(10);
        context.setScreen(() -> null);
        context.waitTicks(4);
    }

    private void captureRevised() {
        floor(); camera(4, 85, 7, 146, 35);
        for (int x = -2; x <= 2; x++) block(x, 81, 0, "detector_rail[shape=east_west]");
        wire(0, 1); block(0, 81, 2, "redstone_lamp");
        photo("detector_rail", 0, 3);
        command("summon minecart 0.5 81.1 0.5"); photo("detector_rail", 1, 3);
        for (String name : new String[]{"chest", "barrel", "shulker_box", "trapped_chest"}) {
            floor(); block(0, 81, 0, name + (name.equals("shulker_box") ? "[facing=up]" : "[facing=south]"));
            String topic = name.equals("chest") ? "container" : name;
            if (name.equals("trapped_chest")) { wire(1, 0); block(2, 81, 0, "redstone_lamp"); }
            photo(topic, 0, 3);
            openContainer(); photo(topic, 1, 0);
            world.getServer().runOnServer(server -> server.getPlayerList().getPlayers().getFirst().closeContainer());
            command("gamemode spectator @a");
        }
        for (String name : new String[]{"furnace", "blast_furnace", "smoker"}) {
            floor(); block(0, 81, 0, name + "[facing=south,lit=false]"); photo(name, 0, 0);
            block(0, 81, 0, name + "[facing=south,lit=true]"); photo(name, 1, 0);
        }
        floor(); block(0, 81, 0, "brewing_stand"); photo("brewing_stand", 0, 0);
        for (int slot = 0; slot < 3; slot++) command("item replace block 0 81 0 container." + slot + " with potion[potion_contents={potion:'minecraft:water'}]");
        photo("brewing_stand", 1, 3);
        floor(); block(0, 81, 0, "lectern[facing=south]"); photo("lectern", 0, 0);
        block(0, 81, 0, "lectern[facing=south,has_book=true]");
        command("data merge block 0 81 0 {Book:{id:'minecraft:writable_book',count:1,components:{'minecraft:writable_book_content':{pages:['Redstone Block Description']}}},Page:0}");
        photo("lectern", 1, 0);
        floor(); block(0, 81, 0, "composter[level=0]"); photo("composter", 0, 0);
        block(0, 81, 0, "composter[level=8]"); photo("composter", 1, 0);
        floor(); block(0, 81, 0, "jukebox"); wire(1, 0); block(2, 81, 0, "redstone_lamp"); photo("jukebox", 0, 0);
        command("item replace block 0 81 0 container.0 with music_disc_13"); photo("jukebox", 1, 4);
    }
}
