package net.baltyr.opmmod.client;

import net.baltyr.opmmod.OpmMod;
import net.baltyr.opmmod.classes.ModCapabilities;
import net.baltyr.opmmod.classes.OpmClass;
import net.baltyr.opmmod.client.abilities.AbilityHandler;
import net.baltyr.opmmod.client.screen.ClassScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = OpmMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class KeyInputHandler {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();

        if (KeyBindings.OPEN_CLASS_SCREEN.consumeClick()) {
            if (mc.screen == null) mc.setScreen(new ClassScreen());
        }

        if (!CombatModeHandler.isInCombatMode()) return;
        if (mc.screen != null || mc.player == null) return;

        // Vérifie que le joueur est bien Saitama
        boolean isSaitama = mc.player.getCapability(ModCapabilities.PLAYER_CLASS)
                .map(cap -> cap.getPlayerClass() == OpmClass.SAITAMA)
                .orElse(false);
        if (!isSaitama) return;

        if (KeyBindings.ABILITY_1.consumeClick()) AbilityHandler.tryActivate(0, mc.player);
        if (KeyBindings.ABILITY_2.consumeClick()) AbilityHandler.tryActivate(1, mc.player);
        if (KeyBindings.ABILITY_3.consumeClick()) AbilityHandler.tryActivate(2, mc.player);
        if (KeyBindings.ABILITY_4.consumeClick()) AbilityHandler.tryActivate(3, mc.player);
        if (KeyBindings.ABILITY_5.consumeClick()) AbilityHandler.tryActivate(4, mc.player);
        if (KeyBindings.ABILITY_6.consumeClick()) AbilityHandler.tryActivate(5, mc.player);
    }
}