package net.baltyr.opmmod.client.abilities;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ServerAbilityHandler {

    // ── Coups consécutifs ─────────────────────────────────────────────────────
    private static boolean consecutiveActive = false;
    private static int     consecutiveTicks  = 0;
    private static final int CONSECUTIVE_TOTAL    = 6;
    private static final int CONSECUTIVE_INTERVAL = 3;
    private static ServerPlayer consecutivePlayer = null;
    private static Vec3         consecutiveDir    = null;

    // ── Sauts latéraux ────────────────────────────────────────────────────────
    private static boolean sideHopsActive = false;
    private static int     sideHopsTicks  = 0;
    private static int     sideHopsDir    = 1;
    private static ServerPlayer sideHopsPlayer = null;
    private static final int SIDE_HOPS_DURATION = 30;
    private static final int SIDE_HOPS_INTERVAL = 5;

    // ── Coup de poing sérieux ─────────────────────────────────────────────────
    private static boolean seriousPunchActive = false;
    private static int     seriousPunchOffset = 0;
    private static Vec3    seriousPunchOrigin = null;
    private static Vec3    seriousPunchDir    = null;
    private static ServerPlayer seriousPunchPlayer = null;
    private static final int SERIOUS_PUNCH_RANGE  = 40;
    private static final int SERIOUS_PUNCH_STEP   = 2;
    private static final int SERIOUS_PUNCH_RADIUS = 3; // inchangé — portée du tunnel

    // ─────────────────────────────────────────────────────────────────────────

    public static void activate(int slot, ServerPlayer player) {
        switch (slot) {
            case 0 -> normalPunch(player);
            case 1 -> startConsecutive(player);
            case 2 -> startSideHops(player);
            case 3 -> seriousTableFlip(player);
            case 4 -> startSeriousPunch(player);
        }
    }

    public static void tick() {
        if (consecutiveActive  && consecutivePlayer  != null) tickConsecutive();
        if (sideHopsActive     && sideHopsPlayer     != null) tickSideHops();
        if (seriousPunchActive && seriousPunchPlayer != null) tickSeriousPunch();
    }

    // ── 1. Coup de Poing Normal ───────────────────────────────────────────────
    private static void normalPunch(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 origin = player.getEyePosition();
        Vec3 dir    = player.getLookAngle().normalize();

        for (int d = 0; d < 5; d++) {
            Vec3 point = origin.add(dir.scale(d + 1));
            destroyCylinder(level, point, dir, 1);
        }

        Vec3 center = origin.add(dir.scale(3));
        hurtEntities(player, center, 2.0, 10f, dir, 0.5, 0.2);
    }

    // ── 2. Coups Consécutifs ──────────────────────────────────────────────────
    // Destruction réduite : rayon 1 uniquement (plus de rayon 2)
    private static void startConsecutive(ServerPlayer player) {
        consecutiveActive = true;
        consecutiveTicks  = 0;
        consecutivePlayer = player;
        consecutiveDir    = player.getLookAngle().normalize();
    }

    private static void tickConsecutive() {
        consecutiveTicks++;

        if (consecutiveTicks % CONSECUTIVE_INTERVAL == 0) {
            ServerPlayer player = consecutivePlayer;
            Vec3 origin = player.getEyePosition();
            Vec3 dir    = consecutiveDir;

            Vec3 center = origin.add(dir.scale(2));
            List<LivingEntity> targets = player.level().getEntitiesOfClass(
                    LivingEntity.class,
                    new AABB(center, center).inflate(4.0),
                    e -> e != player
            );
            for (LivingEntity e : targets) {
                Vec3 toEntity = e.position().subtract(origin).normalize();
                if (toEntity.dot(dir) > 0.5) {
                    e.hurt(player.damageSources().playerAttack(player), 2f);
                    e.setDeltaMovement(dir.x * 0.3, 0.1, dir.z * 0.3);
                    e.hurtMarked = true;
                }
            }

            // Destruction réduite : rayon 1 sur toute la portée (plus de cône élargi)
            for (int d = 0; d < 4; d++) {
                Vec3 point = origin.add(dir.scale(d + 1));
                destroyCylinder(player.serverLevel(), point, dir, 1);
            }
        }

        if (consecutiveTicks >= CONSECUTIVE_TOTAL * CONSECUTIVE_INTERVAL) {
            consecutiveActive = false;
            consecutivePlayer = null;
        }
    }

    // ── 3. Sauts Latéraux Sérieux ─────────────────────────────────────────────
    // Déplacement encore réduit (0.5), zone de dégâts élargie (8.0)
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

            // Déplacement encore plus doux : 0.5
            Vec3 dash = right.scale(sideHopsDir * 0.5);
            player.setDeltaMovement(dash.x, 0.15, dash.z);
            player.hurtMarked = true;

            // Zone de dégâts élargie : rayon 8.0
            Vec3 pos = player.position();
            hurtEntities(player, pos, 8.0, 6f, Vec3.ZERO, 0.6, 0.4);

            // Destruction au sol rayon 2
            BlockPos ground = BlockPos.containing(pos.x, pos.y - 0.5, pos.z);
            destroySphere(player.serverLevel(), ground, 2);

            sideHopsDir = -sideHopsDir;
        }

        if (sideHopsTicks <= 0) {
            sideHopsActive = false;
            sideHopsPlayer = null;
        }
    }

    // ── 4. Retournement de Table Sérieux ──────────────────────────────────────
    // Zone circulaire de rayon 4 (au lieu du carré 5x5)
    private static void seriousTableFlip(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 look    = player.getLookAngle();
        Vec3 flatDir = new Vec3(look.x, 0, look.z).normalize();
        Vec3 center  = player.position().add(flatDir.scale(4));
        BlockPos cPos = BlockPos.containing(center);

        int radius = 4;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                // Cercle : x² + z² <= r²
                if (x * x + z * z <= radius * radius) {
                    for (int y = -1; y <= 1; y++) {
                        removeBlock(level, cPos.offset(x, y, z));
                    }
                }
            }
        }

        // Entités dans la zone circulaire projetées vers le haut
        List<LivingEntity> targets = level.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(center, center).inflate(radius),
                e -> e != player
        );
        for (LivingEntity e : targets) {
            e.hurt(player.damageSources().playerAttack(player), 15f);
            Vec3 dir = e.position().subtract(player.position()).normalize();
            e.setDeltaMovement(dir.x * 1.0, 1.8, dir.z * 1.0);
            e.hurtMarked = true;
        }
    }

    // ── 5. Coup de Poing Sérieux ──────────────────────────────────────────────
    // Boulet de canon rayon 3, avance de 2 blocs/tick sur 20 blocs
    private static void startSeriousPunch(ServerPlayer player) {
        seriousPunchActive = true;
        seriousPunchOffset = 0;
        seriousPunchOrigin = player.getEyePosition();
        seriousPunchDir    = player.getLookAngle().normalize();
        seriousPunchPlayer = player;

        Vec3 center = seriousPunchOrigin.add(seriousPunchDir.scale(10));
        hurtEntities(player, center, 5.0, 40f, seriousPunchDir, 3.0, 1.0);
    }

    private static void tickSeriousPunch() {
        ServerLevel level = seriousPunchPlayer.serverLevel();
        int end = Math.min(seriousPunchOffset + SERIOUS_PUNCH_STEP, SERIOUS_PUNCH_RANGE);

        for (int d = seriousPunchOffset; d < end; d++) {
            Vec3 sphereCenter = seriousPunchOrigin.add(seriousPunchDir.scale(d + 1));
            destroySphere(level, BlockPos.containing(sphereCenter), SERIOUS_PUNCH_RADIUS);
        }

        seriousPunchOffset = end;

        if (seriousPunchOffset >= SERIOUS_PUNCH_RANGE) {
            seriousPunchActive = false;
            seriousPunchPlayer = null;
        }
    }

    // ── Utilitaires ───────────────────────────────────────────────────────────

    private static void destroyCylinder(ServerLevel level, Vec3 point, Vec3 dir, int radius) {
        Vec3 up     = Math.abs(dir.y) < 0.9 ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
        Vec3 right  = dir.cross(up).normalize();
        Vec3 upPerp = dir.cross(right).normalize();

        for (int r1 = -radius; r1 <= radius; r1++) {
            for (int r2 = -radius; r2 <= radius; r2++) {
                if (r1 * r1 + r2 * r2 <= radius * radius) {
                    BlockPos pos = BlockPos.containing(
                            point.x + right.x * r1 + upPerp.x * r2,
                            point.y + right.y * r1 + upPerp.y * r2,
                            point.z + right.z * r1 + upPerp.z * r2
                    );
                    removeBlock(level, pos);
                }
            }
        }
    }

    private static void destroySphere(ServerLevel level, BlockPos center, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x*x + y*y + z*z <= radius * radius) {
                        removeBlock(level, center.offset(x, y, z));
                    }
                }
            }
        }
    }

    private static void removeBlock(ServerLevel level, BlockPos pos) {
        if (!level.getBlockState(pos).isAir() && !level.getBlockState(pos).liquid()) {
            level.removeBlock(pos, false);
        }
    }

    private static void hurtEntities(ServerPlayer player, Vec3 center,
                                     double radius, float damage,
                                     Vec3 kbDir, double kbHoriz, double kbVert) {
        List<LivingEntity> targets = player.level().getEntitiesOfClass(
                LivingEntity.class,
                new AABB(center, center).inflate(radius),
                e -> e != player
        );
        for (LivingEntity e : targets) {
            e.hurt(player.damageSources().playerAttack(player), damage);
            Vec3 kb = kbDir.lengthSqr() > 0
                    ? kbDir.scale(kbHoriz)
                    : e.position().subtract(player.position()).normalize().scale(kbHoriz);
            e.setDeltaMovement(kb.x, kbVert, kb.z);
            e.hurtMarked = true;
        }
    }
}