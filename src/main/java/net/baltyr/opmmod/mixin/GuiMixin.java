package net.baltyr.opmmod.mixin;

import net.baltyr.opmmod.client.CombatModeHandler;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {

    @Inject(
            method = "renderHotbar(FLnet/minecraft/client/gui/GuiGraphics;)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = true
    )
    private void cancelHotbar(float pPartialTick, GuiGraphics pGuiGraphics, CallbackInfo ci) {
        if (CombatModeHandler.isInCombatMode()) {
            ci.cancel();
        }
    }
}