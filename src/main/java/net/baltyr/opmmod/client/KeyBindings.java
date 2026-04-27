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
            GLFW.GLFW_KEY_C,
            "key.categories.opmmod"
    );

    public static final KeyMapping ABILITY_1 = new KeyMapping(
            "key.opmmod.ability_1",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_1,
            "key.categories.opmmod"
    );

    public static final KeyMapping ABILITY_2 = new KeyMapping(
            "key.opmmod.ability_2",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_2,
            "key.categories.opmmod"
    );

    public static final KeyMapping ABILITY_3 = new KeyMapping(
            "key.opmmod.ability_3",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_3,
            "key.categories.opmmod"
    );

    public static final KeyMapping ABILITY_4 = new KeyMapping(
            "key.opmmod.ability_4",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_4,
            "key.categories.opmmod"
    );

    public static final KeyMapping ABILITY_5 = new KeyMapping(
            "key.opmmod.ability_5",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_5,
            "key.categories.opmmod"
    );

    public static final KeyMapping ABILITY_6 = new KeyMapping(
            "key.opmmod.ability_6",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_6,
            "key.categories.opmmod"
    );

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_CLASS_SCREEN);
        event.register(TOGGLE_COMBAT_MODE);
        event.register(ABILITY_1);
        event.register(ABILITY_2);
        event.register(ABILITY_3);
        event.register(ABILITY_4);
        event.register(ABILITY_5);
        event.register(ABILITY_6);
    }
}