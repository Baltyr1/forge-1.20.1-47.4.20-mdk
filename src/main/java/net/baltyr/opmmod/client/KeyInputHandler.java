package net.baltyr.opmmod.client;

import net.baltyr.opmmod.OpmMod;
import net.baltyr.opmmod.client.abilities.AbilityHandler;
import net.baltyr.opmmod.client.screen.ClassScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = OpmMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class KeyInputHandler {

    // Touches AZERTY correspondant aux slots de la hotbar de combat
    // & = 49 (1), é = 50 (2), " = 51 (3), ' = 52 (4), ( = 53 (5), - = 54 (6)
    private static final int[] ABILITY_KEYS = {
            GLFW.GLFW_KEY_1,  // & → slot 0 : Coup de poing normal
            GLFW.GLFW_KEY_2,  // é → slot 1 : Coups consécutifs
            GLFW.GLFW_KEY_3,  // " → slot 2 : Sauts latéraux sérieux
            GLFW.GLFW_KEY_4,  // ' → slot 3 : Retournement de Table
            GLFW.GLFW_KEY_5,  // ( → slot 4 : Coup de Poing Sérieux
    };

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();

        if (KeyBindings.OPEN_CLASS_SCREEN.consumeClick()) {
            if (mc.screen == null) mc.setScreen(new ClassScreen());
        }

        if (!CombatModeHandler.isInCombatMode()) return;
        if (mc.screen != null || mc.player == null) return;
        if (event.getAction() != GLFW.GLFW_PRESS) return;

        // Get the key code from the event
        int key = event.getKey();

        for (int i = 0; i < ABILITY_KEYS.length; i++) {
            if (key == ABILITY_KEYS[i]) {
                AbilityHandler.tryActivate(i, mc.player);

                // Note: Since you're using event.getKey(), the vanilla hotbar
                // switch might still trigger. You may need to cancel the event
                // if you want to completely block the slot change.

                break;
            }
        }
    }
}