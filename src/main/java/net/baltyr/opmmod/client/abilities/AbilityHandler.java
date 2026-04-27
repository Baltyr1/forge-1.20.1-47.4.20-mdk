package net.baltyr.opmmod.client.abilities;

import net.baltyr.opmmod.OpmMod;
import net.baltyr.opmmod.client.CombatHud;
import net.baltyr.opmmod.client.CombatModeHandler;
import net.baltyr.opmmod.network.PacketHandler;
import net.baltyr.opmmod.network.UseAbilityPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = OpmMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class AbilityHandler {

    public static final int SLOT_NORMAL_PUNCH  = 0;
    public static final int SLOT_CONSECUTIVE   = 1;
    public static final int SLOT_SIDE_HOPS     = 2;
    public static final int SLOT_TABLE_FLIP    = 3;
    public static final int SLOT_SERIOUS_PUNCH = 4;

    private static final float[] STAMINA_COST   = { 10f, 20f, 25f, 30f, 60f };
    //                                              0.8s  4s   6s   8s   20s
    private static final int[]   COOLDOWN_TICKS = { 16,  80,  120, 160, 400 };

    public static final int[] cooldowns = new int[5];

    // Sauts latéraux mouvement côté client
    private static boolean sideHopsActive = false;
    private static int     sideHopsTicks  = 0;
    private static int     sideHopsDir    = 1;
    private static final int SIDE_HOPS_DURATION = 30;
    private static final int SIDE_HOPS_INTERVAL = 5;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        Player player = event.player;

        // Décrémente cooldowns côté client
        for (int i = 0; i < cooldowns.length; i++) {
            if (cooldowns[i] > 0) cooldowns[i]--;
        }

        // Régénération stamina
        if (CombatModeHandler.isInCombatMode()) {
            CombatHud.stamina = Math.min(CombatHud.maxStamina, CombatHud.stamina + 0.5f);
        }

        // Mouvement dash latéral côté client
        if (sideHopsActive && player.level().isClientSide) {
            sideHopsTicks--;
            if (sideHopsTicks >= 0 && sideHopsTicks % SIDE_HOPS_INTERVAL == 0) {
                Vec3 look  = player.getLookAngle();
                Vec3 right = new Vec3(-look.z, 0, look.x).normalize();
                Vec3 dash  = right.scale(sideHopsDir * 2.0);
                player.setDeltaMovement(dash.x, 0.3, dash.z);
                sideHopsDir = -sideHopsDir;
            }
            if (sideHopsTicks <= 0) sideHopsActive = false;
        }

        // Tick serveur
        if (!player.level().isClientSide) {
            ServerAbilityHandler.tick();
        }

    }

    public static boolean tryActivate(int slot, Player player) {
        if (slot < 0 || slot >= 5) return false;
        if (cooldowns[slot] > 0) return false;
        if (CombatHud.stamina < STAMINA_COST[slot]) return false;

        CombatHud.stamina -= STAMINA_COST[slot];
        cooldowns[slot] = COOLDOWN_TICKS[slot];

        if (slot == SLOT_SIDE_HOPS) {
            sideHopsActive = true;
            sideHopsTicks  = SIDE_HOPS_DURATION;
            sideHopsDir    = 1;
        }

        PacketHandler.CHANNEL.send(
                PacketDistributor.SERVER.noArg(),
                new UseAbilityPacket(slot)
        );
        return true;
    }

    public static float getCooldownProgress(int slot) {
        if (slot < 0 || slot >= 5) return 0f;
        return (float) cooldowns[slot] / COOLDOWN_TICKS[slot];
    }

    public static int getCooldownSeconds(int slot) {
        if (slot < 0 || slot >= 5) return 0;
        return (cooldowns[slot] + 19) / 20;
    }

    public static boolean isOnCooldown(int slot) {
        return slot < 5 && cooldowns[slot] > 0;
    }
}