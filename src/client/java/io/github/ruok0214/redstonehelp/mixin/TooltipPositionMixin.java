package io.github.ruok0214.redstonehelp.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.ruok0214.redstonehelp.HelpImageRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import java.util.List;

@Mixin(GuiGraphicsExtractor.class)
public abstract class TooltipPositionMixin {
    @WrapOperation(method = "tooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;positionTooltip(IIIIII)Lorg/joml/Vector2ic;"))
    private Vector2ic redstoneHelp$keepPhotoVisible(ClientTooltipPositioner positioner, int screenWidth, int screenHeight,
            int mouseX, int mouseY, int width, int height, Operation<Vector2ic> original,
            @Local(argsOnly = true) List<ClientTooltipComponent> components) {
        var position = original.call(positioner, screenWidth, screenHeight, mouseX, mouseY, width, height);
        if (components.stream().noneMatch(HelpImageRenderer.class::isInstance)) return position;
        return new Vector2i(Math.max(6, Math.min(position.x(), screenWidth - width - 6)),
                Math.max(6, Math.min(position.y(), screenHeight - height - 6)));
    }
}
