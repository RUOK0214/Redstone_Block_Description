package io.github.ruok0214.redstonehelp;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class HelpKeyMappings {
    public static final KeyMapping PIN = new KeyMapping("key.redstone_help.pin", InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H, KeyMapping.Category.register(Identifier.fromNamespaceAndPath("redstone_help", "general")));
    private HelpKeyMappings() {}
    public static void register() { KeyMappingHelper.registerKeyMapping(PIN); }
    public static Component pinHint() {
        return PIN.isUnbound() ? Component.translatable("redstone_help.pin_unbound")
                : Component.translatable("redstone_help.pin_hint", PIN.getTranslatedKeyMessage());
    }
}
