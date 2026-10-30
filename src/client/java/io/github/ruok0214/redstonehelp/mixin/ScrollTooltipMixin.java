package io.github.ruok0214.redstonehelp.mixin;

import io.github.ruok0214.redstonehelp.ScrollTooltip;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.List;

@Mixin(GuiGraphicsExtractor.class)
public abstract class ScrollTooltipMixin {
    @Inject(method = "tooltip", at = @At("HEAD"), cancellable = true)
    private void redstoneHelp$scroll(Font font, List<ClientTooltipComponent> components, int mouseX, int mouseY,
            ClientTooltipPositioner positioner, Identifier style, CallbackInfo ci) {
        if (ScrollTooltip.render((GuiGraphicsExtractor) (Object) this, font, components, mouseX, mouseY, positioner, style)) ci.cancel();
    }
}
