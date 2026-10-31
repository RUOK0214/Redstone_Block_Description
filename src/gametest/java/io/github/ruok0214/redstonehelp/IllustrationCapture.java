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
        command("fill -12 81 -6 12 89 6 air", "fill -16 80 -16 16 80 16 light_gray_concrete");
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
        if (frame == 2 && (topic.equals("sculk_sensor") || topic.equals("calibrated_sculk_sensor"))) {
            property(1, 81, 0, "lit", "true"); return;
        }
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
            if (Boolean.getBoolean("redstone_help.captureDetailCloseups")) { captureDetailCloseups(); return; }
            if (Boolean.getBoolean("redstone_help.captureDetails")) { captureDetails(); return; }
            if (Boolean.getBoolean("redstone_help.captureVibrations")) { captureVibrations(); return; }
            if (Boolean.getBoolean("redstone_help.captureCauldrons")) { captureCauldrons(); return; }
            if (Boolean.getBoolean("redstone_help.captureExpanded")) { captureExpanded(); return; }
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
        captureExpanded();
        captureVibrations();
        captureDetails();
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

    private void captureExpanded() {
        floor(); block(0, 81, 0, "oak_shelf[facing=south]"); photo("shelf", 0, 0);
        command("item replace block 0 81 0 container.0 with diamond", "item replace block 0 81 0 container.1 with redstone", "item replace block 0 81 0 container.2 with book");
        photo("shelf", 1, 0);
        for (int x : new int[]{-1, 1}) block(x, 81, 0, "oak_shelf[facing=south]");
        for (int x = -1; x <= 1; x++) block(x, 80, 0, "redstone_block"); photo("shelf", 2, 4);
        property(0, 81, 0, "powered", "true");
        floor(); block(0, 81, 0, "chiseled_bookshelf[facing=south]"); photo("chiseled_bookshelf", 0, 0);
        for (int slot = 0; slot < 6; slot++) command("item replace block 0 81 0 container." + slot + " with book");
        photo("chiseled_bookshelf", 1, 0); property(0, 81, 0, "slot_5_occupied", "true");
        captureCauldrons();
        floor(); block(0, 80, 0, "clay"); block(0, 81, 0, "big_dripleaf[facing=south,tilt=none]"); photo("big_dripleaf", 0, 0);
        block(0, 81, 0, "big_dripleaf[facing=south,tilt=full]"); photo("big_dripleaf", 1, 0);
        block(-1, 81, 0, "redstone_block"); photo("big_dripleaf", 2, 0); property(0, 81, 0, "tilt", "none");
        floor(); camera(-4, 84, 5.5, -144, 32); block(-2, 81, 0, "dispenser[facing=east]"); block(1, 81, 0, "stone");
        block(0, 81, 0, "stone_button[face=wall,facing=west]"); block(1, 82, 0, "redstone_lamp");
        command("item replace block -2 81 0 container.0 with wind_charge"); photo("wind_charge", 0, 3);
        block(-3, 81, 0, "redstone_block"); photo("wind_charge", 1, 8);
        property(0, 81, 0, "powered", "true"); property(1, 82, 0, "lit", "true");
        floor(); block(0, 81, 0, "beehive[facing=south,honey_level=0]"); photo("beehive", 0, 0);
        block(0, 81, 0, "beehive[facing=south,honey_level=5]"); photo("beehive", 1, 0);
        floor(); for (int i = 0; i < 3; i++) { block(0, 81, 0, "cake[bites=" + (i * 3) + "]"); photo("cake", i, 0); }
        floor(); block(0, 81, 0, "respawn_anchor[charges=0]"); photo("respawn_anchor", 0, 0);
        block(0, 81, 0, "respawn_anchor[charges=4]"); photo("respawn_anchor", 1, 0);
        floor(); block(0, 81, 0, "decorated_pot"); photo("decorated_pot", 0, 0);
        floor(); input(false); wire(-1, 0); block(0, 81, 0, "bell[attachment=floor,facing=south]");
        photo("bell", 0, 0); input(true); photo("bell", 1, 2); property(0, 81, 0, "powered", "true");
        floor(); block(0, 81, 0, "stone");
        var frame = world.getServer().computeOnServer(server -> {
            var created = new net.minecraft.world.entity.decoration.ItemFrame(server.overworld(), new BlockPos(0, 81, 1), net.minecraft.core.Direction.SOUTH);
            server.overworld().addFreshEntity(created); return created;
        });
        photo("item_frame", 0, 0);
        world.getServer().runOnServer(server -> frame.setItem(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.COMPASS)));
        photo("item_frame", 1, 0);
        world.getServer().runOnServer(server -> frame.setRotation(4)); photo("item_frame", 2, 0);
        floor(); String[] poses = {"standing", "sitting", "running", "star"};
        for (int i = 0; i < poses.length; i++) { block(0, 81, 0, "copper_golem_statue[facing=south,copper_golem_pose=" + poses[i] + "]"); photo("copper_golem_statue", i, 0); }
        floor(); block(0, 80, 0, "pale_oak_log[axis=y]"); block(0, 82, 0, "pale_oak_log[axis=y]");
        block(0, 81, 0, "creaking_heart[axis=y,creaking_heart_state=dormant]"); photo("creaking_heart", 0, 0);
        block(0, 81, 0, "creaking_heart[axis=y,creaking_heart_state=awake]"); photo("creaking_heart", 1, 0);
        floor(); block(0, 81, 0, "end_portal_frame[eye=false]"); photo("end_portal_frame", 0, 0);
        block(0, 81, 0, "end_portal_frame[eye=true]"); photo("end_portal_frame", 1, 0);
        floor(); block(-1, 81, 0, "command_block"); block(0, 81, 0, "chain_command_block"); block(1, 81, 0, "repeating_command_block"); photo("command_block", 0, 0);
        floor(); block(0, 81, 0, "copper_chest[facing=south]"); photo("copper_chest", 0, 0);
        openContainer(); photo("copper_chest", 1, 0);
        world.getServer().runOnServer(server -> server.getPlayerList().getPlayers().getFirst().closeContainer());
        command("gamemode spectator @a");
    }

    private void captureVibrations() {
        floor(); camera(5, 85, 8, 149, 29);
        block(-1, 81, 0, "sculk_sensor"); block(0, 81, 0, "amethyst_block");
        block(3, 81, 0, "sculk_sensor"); block(4, 81, 0, "redstone_lamp");
        photo("amethyst_block", 0, 2);
        world.getServer().runOnServer(server -> {
            var level = server.overworld(); var pos = new BlockPos(-1, 81, 0);
            var sensor = (net.minecraft.world.level.block.entity.SculkSensorBlockEntity) level.getBlockEntity(pos);
            sensor.getVibrationUser().onReceiveVibration(level, pos, net.minecraft.world.level.gameevent.GameEvent.STEP, null, null, 2);
        });
        photo("amethyst_block", 1, 12);
        property(4, 81, 0, "lit", "true");
        world.getServer().runOnServer(server -> {
            var sensor = (net.minecraft.world.level.block.entity.SculkSensorBlockEntity) server.overworld().getBlockEntity(new BlockPos(3, 81, 0));
            if (sensor.getLastVibrationFrequency() != 1) throw new AssertionError("Resonance preserves frequency");
        });
        for (int variant = 0; variant < 3; variant++) {
            floor(); camera(5, 85, 8, 149, 29);
            block(-3, 81, 0, "note_block"); block(2, 81, 0, "sculk_sensor"); block(3, 81, 0, "redstone_lamp");
            if (variant == 1) block(0, 81, 0, "white_wool");
            if (variant == 2) block(0, 81, 0, "white_carpet");
            boolean accepted = world.getServer().computeOnServer(server -> {
                var level = server.overworld();
                var sensor = (net.minecraft.world.level.block.entity.SculkSensorBlockEntity) level.getBlockEntity(new BlockPos(2, 81, 0));
                return sensor.getListener().handleGameEvent(level, net.minecraft.world.level.gameevent.GameEvent.NOTE_BLOCK_PLAY,
                        net.minecraft.world.level.gameevent.GameEvent.Context.of(level.getBlockState(new BlockPos(-3, 81, 0))),
                        net.minecraft.world.phys.Vec3.atCenterOf(new BlockPos(-3, 81, 0)));
            });
            if (accepted != (variant != 1)) throw new AssertionError("Wool occlusion differs from carpet");
            photo(variant == 2 ? "wool_carpet" : "wool", variant == 2 ? 0 : variant, 12);
            property(3, 81, 0, "lit", Boolean.toString(variant != 1));
        }
    }

    private void captureCauldrons() {
        floor(); block(0, 81, 0, "cauldron"); photo("cauldron", 0, 0);
        block(0, 81, 0, "water_cauldron[level=1]"); photo("cauldron", 1, 0);
        block(0, 81, 0, "water_cauldron[level=3]"); photo("cauldron", 2, 0);
        block(0, 81, 0, "lava_cauldron"); photo("cauldron", 3, 0);
        block(0, 81, 0, "cauldron");
        command("fillbiome -16 72 -16 15 95 15 snowy_plains", "weather rain");
        world.getServer().runOnServer(server -> {
            var level = server.overworld(); var pos = new BlockPos(0, 81, 0);
            level.setRainLevel(1); level.getRandom().setSeed(2602);
            for (int i = 0; i < 2048; i++) {
                var state = level.getBlockState(pos);
                if (state.is(net.minecraft.world.level.block.Blocks.POWDER_SNOW_CAULDRON)
                        && state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LEVEL_CAULDRON) == 3) break;
                level.tickPrecipitation(pos);
            }
        });
        checkBlock(0, 81, 0, "powder_snow_cauldron"); property(0, 81, 0, "level", "3");
        world.getConnection().waitForClientboundPackets();
        context.runOnClient(mc -> { mc.level.setRainLevel(1); mc.level.setThunderLevel(0); });
        photo("cauldron", 4, 0);
        command("weather clear", "fillbiome -16 72 -16 15 95 15 plains");
        world.getServer().runOnServer(server -> server.overworld().setRainLevel(0));
        context.runOnClient(mc -> mc.level.setRainLevel(0));
    }

    private void captureDetails() {
        String[][] instruments = {
                {"dirt", "harp"}, {"sand", "snare"}, {"glass", "hat"}, {"clay", "flute"},
                {"gold_block", "bell"}, {"white_wool", "guitar"}, {"packed_ice", "chime"},
                {"bone_block", "xylophone"}, {"iron_block", "iron_xylophone"}, {"soul_sand", "cow_bell"},
                {"pumpkin", "didgeridoo"}, {"emerald_block", "bit"}, {"hay_block", "banjo"}, {"glowstone", "pling"},
                {"copper_block", "trumpet"}, {"exposed_copper", "trumpet_exposed"},
                {"weathered_copper", "trumpet_weathered"}, {"oxidized_copper", "trumpet_oxidized"}};
        for (int i = 0; i < instruments.length; i++) {
            floor(); block(0, 81, 0, instruments[i][0]); block(0, 82, 0, "note_block");
            step(1); property(0, 82, 0, "instrument", instruments[i][1]); photo("note_block", i + 2, 0);
        }
        for (int i = 0; i < 2; i++) {
            floor(); block(0, 81, 0, "gold_block"); block(0, 82, 0, "note_block");
            block(0, 83, 0, i == 0 ? "zombie_head[rotation=8]" : "creeper_head[rotation=8]");
            camera(4, 85.5, 6.5, 149, 30);
            step(1); property(0, 82, 0, "instrument", i == 0 ? "zombie" : "creeper"); photo("note_block", i + 20, 0);
        }
        for (String rail : new String[]{"powered_rail", "activator_rail"}) {
            floor(); camera(1, 92, 21, 180, 29);
            for (int x = -9; x <= 9; x++) block(x, 81, 0, rail + "[shape=east_west]");
            block(0, 80, 0, "redstone_block");
            step(4);
            for (int x = -9; x <= 9; x++) property(x, 81, 0, "powered", Boolean.toString(Math.abs(x) <= 8));
            photo(rail, 2, 0);
            if (rail.equals("powered_rail")) {
                block(4, 81, 0, "rail[shape=east_west]"); step(4);
                for (int x = 5; x <= 9; x++) property(x, 81, 0, "powered", "false");
                photo(rail, 3, 0);
            }
        }
        ordinaryCircuit("repeater[facing=west]"); input(true); step(5);
        block(0, 81, -1, "repeater[facing=north]"); block(0, 81, -2, "redstone_block");
        step(6); property(0, 81, 0, "locked", "true"); photo("repeater", 2, 0);
        input(false); step(6); property(0, 81, 0, "powered", "true"); photo("repeater", 3, 0);
        for (String sensor : new String[]{"sculk_sensor", "calibrated_sculk_sensor"}) {
            floor(); block(0, 81, 0, sensor + "[waterlogged=true]"); block(1, 81, 0, "redstone_lamp");
            world.getServer().runOnServer(server -> {
                var entity = (net.minecraft.world.level.block.entity.SculkSensorBlockEntity) server.overworld().getBlockEntity(new BlockPos(0, 81, 0));
                entity.getVibrationUser().onReceiveVibration(server.overworld(), new BlockPos(0, 81, 0), net.minecraft.world.level.gameevent.GameEvent.NOTE_BLOCK_PLAY, null, null, 2);
            });
            property(0, 81, 0, "waterlogged", "true"); property(1, 81, 0, "lit", "true"); photo(sensor, 2, 0);
        }
        captureDispenserDetails();
        floor(); camera(4, 85, 7, 149, 30);
        for (int x = -1; x <= 1; x++) {
            block(x, 82, 0, "oak_shelf[facing=south]");
            command("item replace block " + x + " 82 0 container.0 with diamond");
            block(x, 82, -1, "redstone_block");
        }
        step(4); photo("shelf", 3, 0);
        floor(); block(0, 81, 0, "cauldron"); command("fillbiome -16 72 -16 15 95 15 plains", "weather rain");
        world.getServer().runOnServer(server -> {
            var level = server.overworld(); level.setRainLevel(1); level.getRandom().setSeed(2602);
            for (int i = 0; i < 2048; i++) {
                var pos = new BlockPos(0, 81, 0); var state = level.getBlockState(pos);
                if (state.is(net.minecraft.world.level.block.Blocks.WATER_CAULDRON)
                        && state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LEVEL_CAULDRON) == 3) break;
                level.tickPrecipitation(pos);
            }
        });
        checkBlock(0, 81, 0, "water_cauldron"); property(0, 81, 0, "level", "3");
        context.runOnClient(mc -> mc.level.setRainLevel(1)); photo("cauldron", 5, 0);
        command("weather clear"); world.getServer().runOnServer(server -> server.overworld().setRainLevel(0));
        context.runOnClient(mc -> mc.level.setRainLevel(0));
        captureDetailCloseups();
    }

    private void captureDetailCloseups() {
        for (String rail : new String[]{"powered_rail", "activator_rail"}) {
            floor();
            for (int x = -9; x <= 9; x++) block(x, 81, 0, rail + "[shape=east_west]");
            block(0, 80, 0, "redstone_block"); step(4);
            property(8, 81, 0, "powered", "true"); property(9, 81, 0, "powered", "false");
            camera(12, 84, 5.5, 146, 32);
            photo(rail, rail.equals("powered_rail") ? 4 : 3, 0);
            if (rail.equals("powered_rail")) {
                block(4, 81, 0, "rail[shape=east_west]"); step(4); camera(8, 84, 5.5, 146, 32);
                property(5, 81, 0, "powered", "false"); photo(rail, 3, 0);
            }
        }
        floor(); camera(4, 86.5, 10, 158, 22);
        context.runOnClient(mc -> mc.options.fov().set(40));
        block(0, 81, 0, "cauldron"); block(0, 85, 0, "dripstone_block");
        block(0, 84, 0, "pointed_dripstone[vertical_direction=down,thickness=tip]");
        for (int[] p : new int[][]{{1,0},{-1,0},{0,1},{0,-1}}) block(p[0], 86, p[1], "glass");
        block(0, 86, 0, "lava");
        world.getServer().runOnServer(server -> {
            var level = server.overworld(); var pos = new BlockPos(0, 84, 0);
            net.minecraft.world.level.block.PointedDripstoneBlock.maybeTransferFluid(level.getBlockState(pos), level, pos, 0);
        });
        step(60); checkBlock(0, 81, 0, "lava_cauldron"); photo("cauldron", 6, 0);
        context.runOnClient(mc -> mc.options.fov().set(32));
    }

    private void dispenserSetup(String item) {
        floor(); block(0, 81, 0, "dispenser[facing=east]");
        command("item replace block 0 81 0 container.0 with " + item);
    }
    private void dispense() { block(-1, 81, 0, "redstone_block"); step(5); }
    private void captureDispenserDetails() {
        dispenserSetup("flint_and_steel"); dispense(); checkBlock(1, 81, 0, "fire"); photo("dispenser", 2, 0);
        dispenserSetup("shears"); command("summon sheep 1.5 81 0.5 {NoAI:1b,Rotation:[90f,0f]}");
        photo("dispenser", 3, 0); dispense();
        world.getServer().runOnServer(server -> {
            var sheep = server.overworld().getEntitiesOfClass(net.minecraft.world.entity.animal.sheep.Sheep.class, new net.minecraft.world.phys.AABB(0, 80, -1, 3, 84, 2)).getFirst();
            if (!sheep.isSheared()) throw new AssertionError("Dispenser shears sheep");
        });
        photo("dispenser", 4, 0);
        dispenserSetup("bone_meal 16"); block(1, 80, 0, "farmland[moisture=7]"); block(1, 81, 0, "wheat[age=5]");
        dispense(); property(1, 81, 0, "age", "7"); photo("dispenser", 5, 0);
        dispenserSetup("glass_bottle"); block(1, 81, 0, "beehive[facing=south,honey_level=5]"); dispense();
        property(1, 81, 0, "honey_level", "0");
        world.getServer().runOnServer(server -> {
            var container = (net.minecraft.world.Container) server.overworld().getBlockEntity(new BlockPos(0, 81, 0));
            if (!container.getItem(0).is(net.minecraft.world.item.Items.HONEY_BOTTLE)) throw new AssertionError("Dispenser collects honey");
        });
        photo("dispenser", 6, 0); photo("beehive", 2, 0);
        dispenserSetup("potion[potion_contents={potion:'minecraft:water'}]"); block(1, 81, 0, "dirt"); dispense();
        checkBlock(1, 81, 0, "mud"); photo("dispenser", 7, 0);
        dispenserSetup("glowstone"); block(1, 81, 0, "respawn_anchor"); dispense();
        property(1, 81, 0, "charges", "1"); photo("dispenser", 8, 0);
        dispenserSetup("honeycomb"); block(1, 81, 0, "copper_block"); dispense();
        checkBlock(1, 81, 0, "waxed_copper_block"); photo("dispenser", 9, 0);
        dispenserSetup("iron_chestplate"); command("summon armor_stand 1.5 81 0.5 {Rotation:[180f,0f]}"); dispense();
        world.getServer().runOnServer(server -> {
            var entity = server.overworld().getEntitiesOfClass(net.minecraft.world.entity.decoration.ArmorStand.class, new net.minecraft.world.phys.AABB(0, 80, -1, 3, 84, 2)).getFirst();
            if (!entity.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST).is(net.minecraft.world.item.Items.IRON_CHESTPLATE)) throw new AssertionError("Dispenser equips armor");
        });
        photo("dispenser", 10, 0);
        dispenserSetup("minecart"); block(1, 81, 0, "rail[shape=east_west]"); dispense(); photo("dispenser", 11, 0);
        dispenserSetup("shulker_box"); dispense(); checkBlock(1, 81, 0, "shulker_box"); photo("dispenser", 12, 0);
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
