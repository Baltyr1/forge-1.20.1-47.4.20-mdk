package net.baltyr.opmmod.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.baltyr.opmmod.OpmMod;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = OpmMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class KeyBindings {

    public static final KeyMapping OPEN_CLASS_SCREEN = new KeyMapping(
            "key.opmmod.class_screen",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_COMMA,
            "key.categories.opmmod"
    );

    public static final KeyMapping TOGGLE_COMBAT_MODE = new KeyMapping(
            "key.opmmod.combat_mode",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_C,      // touche C par défaut, changeable dans les options
            "key.categories.opmmod"
    );

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_CLASS_SCREEN);
        event.register(TOGGLE_COMBAT_MODE);
    }
}