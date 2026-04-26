package net.baltyr.opmmod.event;

import net.baltyr.opmmod.OpmMod;
import net.baltyr.opmmod.classes.ClassCapabilityProvider;
import net.baltyr.opmmod.classes.ModCapabilities;
import net.baltyr.opmmod.classes.OpmClass;
import net.baltyr.opmmod.command.ClassCommand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = OpmMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClassEventHandler {

    // ── Attache la capability à chaque joueur ──────────────────────────────
    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(
                    new ResourceLocation(OpmMod.MOD_ID, "player_class"),
                    new ClassCapabilityProvider());
        }
    }

    // ── Copie la capability à la mort / respawn ────────────────────────────
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

    // ── Affiche le nom de classe au-dessus du pseudo (nametag) ────────────
    @SubscribeEvent
    public static void onNameTag(net.minecraftforge.event.entity.player.PlayerEvent.NameFormat event) {
        event.getEntity().getCapability(ModCapabilities.PLAYER_CLASS).ifPresent(cap -> {
            OpmClass cls = cap.getPlayerClass();
            if (cls != OpmClass.NONE) {
                // Format : [Classe] Pseudo
                event.setDisplayname(
                        net.minecraft.network.chat.Component.literal(
                                cls.getFormattedName() + " §r" + event.getUsername()));
            }
        });
    }

    // ── Enregistre les commandes ───────────────────────────────────────────
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        ClassCommand.register(event.getDispatcher());
    }
}