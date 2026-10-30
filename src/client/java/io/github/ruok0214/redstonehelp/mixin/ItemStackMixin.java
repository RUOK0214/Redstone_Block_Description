package io.github.ruok0214.redstonehelp.mixin;

import io.github.ruok0214.redstonehelp.HelpImage;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Optional;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Inject(method = "getTooltipImage", at = @At("RETURN"), cancellable = true)
    private void redstoneHelp$image(CallbackInfoReturnable<Optional<TooltipComponent>> callback) {
        // Keep vanilla or other mods' existing tooltip images.
        if (callback.getReturnValue().isEmpty()) {
            HelpImage.forStack((ItemStack) (Object) this).ifPresent(image -> callback.setReturnValue(Optional.of(image)));
        }
    }
}
