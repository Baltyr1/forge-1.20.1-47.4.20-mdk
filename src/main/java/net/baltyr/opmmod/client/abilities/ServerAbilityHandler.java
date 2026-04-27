package net.baltyr.opmmod.client.abilities;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Logique des capacités exécutée côté SERVEUR uniquement.
 * Appelée depuis UseAbilityPacket.handle().
 */
public class ServerAbilityHandler {

    // Cooldowns serveur par joueur UUID → on utilise un simple tableau global
    // pour l'instant (singleplayer). À améliorer avec une map UUID→cooldowns en multijoueur.
    private static final int[] cooldownsServer = new int[5];
    private static final float[] STAMINA_COST  = { 10f, 25f, 30f, 35f, 50f };
    private static final int[]   COOLDOWN_TICKS = { 10, 60, 100, 120, 200 };

    // Sauts latéraux tick-based côté serveur
    private static boolean sideHopsActive = false;
    private static int     sideHopsTicks  = 0;
    private static int     sideHopsDir    = 1;
    private static ServerPlayer sideHopsPlayer = null;
    private static final int SIDE_HOPS_DURATION = 30;
    private static final int SIDE_HOPS_INTERVAL = 5;

    public static void activate(int slot, ServerPlayer player) {
        if (slot < 0 || slot >= 5) return;

        switch (slot) {
            case 0 -> normalPunch(player);
            case 1 -> consecutivePunches(player);
            case 2 -> startSideHops(player);
            case 3 -> seriousTableFlip(player);
            case 4 -> seriousPunch(player);
        }
    }

    // ── Tick serveur appelé depuis un event ForgeEvent ────────────────────────
    public static void tick() {
        if (sideHopsActive && sideHopsPlayer != null) {
            tickSideHops();
        }
    }

    // ── 1. Coup de poing normal ───────────────────────────────────────────────
    private static void normalPunch(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 look   = player.getLookAngle();
        Vec3 center = player.position().add(look.scale(2.5));

        hurtEntities(player, center, 3.0, 6f, 0.6, 0.3);
        destroyBlocks(level, BlockPos.containing(center), 2);
    }

    // ── 2. Coups consécutifs ──────────────────────────────────────────────────
    private static void consecutivePunches(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 look = player.getLookAngle();

        for (int hit = 0; hit < 4; hit++) {
            final int h = hit;
            Thread t = new Thread(() -> {
                try { Thread.sleep(h * 150L); } catch (InterruptedException ignored) {}
                Vec3 center = player.position().add(look.scale(3.0));
                hurtEntities(player, center, 4.0, 4f, 0.4, 0.2);
                destroyBlocks(level, BlockPos.containing(center), 2);
            });
            t.setDaemon(true);
            t.start();
        }
    }

    // ── 3. Sauts latéraux ─────────────────────────────────────────────────────
    private static void startSideHops(ServerPlayer player) {
        sideHopsActive = true;
        sideHopsTicks  = SIDE_HOPS_DURATION;
        sideHopsDir    = 1;
        sideHopsPlayer = player;
    }

    private static void tickSideHops() {
        sideHopsTicks--;

        if (sideHopsTicks % SIDE_HOPS_INTERVAL == 0) {
            ServerPlayer player = sideHopsPlayer;
            Vec3 look  = player.getLookAngle();
            Vec3 right = new Vec3(-look.z, 0, look.x).normalize();
            Vec3 dash  = right.scale(sideHopsDir * 1.8);
            player.setDeltaMovement(dash.x, 0.3, dash.z);
            player.hurtMarked = true;

            Vec3 pos = player.position();
            hurtEntities(player, pos, 5.0, 8f, 1.0, 0.4);
            destroyBlocks(player.serverLevel(), BlockPos.containing(pos), 3);

            sideHopsDir = -sideHopsDir;
        }

        if (sideHopsTicks <= 0) {
            sideHopsActive = false;
            sideHopsPlayer = null;
        }
    }

    // ── 4. Retournement de Table ──────────────────────────────────────────────
    private static void seriousTableFlip(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 look   = player.getLookAngle();
        Vec3 center = player.position().add(look.scale(3.5));

        List<LivingEntity> targets = player.level().getEntitiesOfClass(
                LivingEntity.class,
                new AABB(center, center).inflate(6.0),
                e -> e != player
        );
        for (LivingEntity e : targets) {
            e.hurt(player.damageSources().playerAttack(player), 12f);
            Vec3 dir = e.position().subtract(player.position()).normalize();
            e.setDeltaMovement(dir.x * 1.2, 1.8, dir.z * 1.2);
            e.hurtMarked = true;
        }
        destroyBlocks(level, BlockPos.containing(center), 4);
    }

    // ── 5. Coup de Poing Sérieux ──────────────────────────────────────────────
    private static void seriousPunch(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 look   = player.getLookAngle();
        Vec3 center = player.position().add(look.scale(4.0));

        List<LivingEntity> targets = player.level().getEntitiesOfClass(
                LivingEntity.class,
                new AABB(center, center).inflate(7.0),
                e -> e != player
        );
        for (LivingEntity e : targets) {
            e.hurt(player.damageSources().playerAttack(player), 20f);
            Vec3 kb = look.scale(3.0);
            e.setDeltaMovement(kb.x, 0.8, kb.z);
            e.hurtMarked = true;
        }
        destroyBlocks(level, BlockPos.containing(center), 5);
    }

    // ── Utilitaires ───────────────────────────────────────────────────────────

    private static void hurtEntities(ServerPlayer player, Vec3 center,
                                     double radius, float damage,
                                     double kbHoriz, double kbVert) {
        List<LivingEntity> targets = player.level().getEntitiesOfClass(
                LivingEntity.class,
                new AABB(center, center).inflate(radius),
                e -> e != player
        );
        for (LivingEntity e : targets) {
            e.hurt(player.damageSources().playerAttack(player), damage);
            Vec3 kb = e.position().subtract(player.position()).normalize().scale(kbHoriz);
            e.setDeltaMovement(kb.x, kbVert, kb.z);
            e.hurtMarked = true;
        }
    }

    private static void destroyBlocks(ServerLevel level, BlockPos center, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x*x + y*y + z*z <= radius * radius) {
                        BlockPos pos = center.offset(x, y, z);
                        if (!level.getBlockState(pos).isAir() &&
                                !level.getBlockState(pos).liquid()) {
                            level.removeBlock(pos, false);
                        }
                    }
                }
            }
        }
    }
}