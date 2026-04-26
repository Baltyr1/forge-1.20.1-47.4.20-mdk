package net.baltyr.opmmod.client;

import net.baltyr.opmmod.OpmMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = OpmMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class CombatModeHandler {

    private static boolean combatMode = false;

    public static boolean isInCombatMode() {
        return combatMode;
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (KeyBindings.TOGGLE_COMBAT_MODE.consumeClick()) {
            combatMode = !combatMode;
        }
    }
}