package net.baltyr.opmmod.event;

import net.baltyr.opmmod.OpmMod;
import net.baltyr.opmmod.classes.ClassCapabilityProvider;
import net.baltyr.opmmod.classes.ModCapabilities;
import net.baltyr.opmmod.classes.OpmClass;
import net.baltyr.opmmod.command.ClassCommand;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.baltyr.opmmod.network.PacketHandler;
import net.baltyr.opmmod.network.SyncClassPacket;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.event.entity.player.PlayerEvent;

@Mod.EventBusSubscriber(modid = OpmMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClassEventHandler {

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(
                    new ResourceLocation(OpmMod.MOD_ID, "player_class"),
                    new ClassCapabilityProvider());
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();
        event.getOriginal().getCapability(ModCapabilities.PLAYER_CLASS).ifPresent(oldCap -> {
            event.getEntity().getCapability(ModCapabilities.PLAYER_CLASS).ifPresent(newCap -> {
                newCap.setPlayerClass(oldCap.getPlayerClass());
            });
        });
        event.getOriginal().invalidateCaps();
    }

    @SubscribeEvent
    public static void onNameTag(PlayerEvent.NameFormat event) {
        event.getEntity().getCapability(ModCapabilities.PLAYER_CLASS).ifPresent(cap -> {
            OpmClass cls = cap.getPlayerClass();
            if (cls != OpmClass.NONE) {
                event.setDisplayname(
                        Component.literal(cls.getFormattedName() + " §r" + event.getUsername()));
            }
        });
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer sp) {
            sp.getCapability(ModCapabilities.PLAYER_CLASS).ifPresent(cap -> {
                PacketHandler.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> sp),
                        new SyncClassPacket(cap.getPlayerClass())
                );
            });
        }
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        ClassCommand.register(event.getDispatcher());
    }
}