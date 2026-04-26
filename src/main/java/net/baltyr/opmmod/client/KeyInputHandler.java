package net.baltyr.opmmod.client;

import net.baltyr.opmmod.OpmMod;
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
            if (mc.screen == null) {
                mc.setScreen(new ClassScreen());
            }
        }
    }
}