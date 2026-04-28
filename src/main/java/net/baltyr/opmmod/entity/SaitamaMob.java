package net.baltyr.opmmod.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class SaitamaMob extends PathfinderMob implements GeoEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // Synchronisation client/serveur de l'animation
    private static final EntityDataAccessor<String> CURRENT_ANIM =
            SynchedEntityData.defineId(SaitamaMob.class, EntityDataSerializers.STRING);

    // ── Cooldowns individuels ─────────────────────────────────────────────────
    private static final int CD_NORMAL_PUNCH  = 30;
    private static final int CD_CONSECUTIVE   = 120;
    private static final int CD_SIDE_HOPS     = 180;
    private static final int CD_TABLE_FLIP    = 240;
    private static final int CD_SERIOUS_PUNCH = 600;

    private int cdNormalPunch  = 0;
    private int cdConsecutive  = 0;
    private int cdSideHops     = 0;
    private int cdTableFlip    = 0;
    private int cdSeriousPunch = 0;

    // Timer animation
    private int animTicksLeft = 0;

    // Durées des animations en ticks
    private static final int ANIM_NORMAL   = 7;
    private static final int ANIM_CONSEC   = 17;
    private static final int ANIM_SIDEHOPS = 9;
    private static final int ANIM_TABLE    = 17;
    private static final int ANIM_SERIOUS  = 15;

    // ── Coup Sérieux progressif ───────────────────────────────────────────────
    private boolean seriousPunchActive = false;
    private int     seriousPunchOffset = 0;
    private Vec3    seriousPunchOrigin = null;
    private Vec3    seriousPunchDir    = null;
    private static final int SERIOUS_PUNCH_RANGE  = 40;
    private static final int SERIOUS_PUNCH_STEP   = 2;
    private static final int SERIOUS_PUNCH_RADIUS = 3;

    public SaitamaMob(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 200.0)
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.ATTACK_DAMAGE, 20.0)
                .add(Attributes.FOLLOW_RANGE, 16.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(CURRENT_ANIM, "idle");
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SaitamaAttackGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Monster.class, true));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
    }

    // ── GeckoLib ──────────────────────────────────────────────────────────────

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 2, state -> {
            String anim = this.entityData.get(CURRENT_ANIM);

            if (!anim.equals("idle")) {
                state.getController().setAnimation(
                        RawAnimation.begin().then(
                                "animation.saitama_mob." + anim,
                                Animation.LoopType.PLAY_ONCE
                        )
                );
                return PlayState.CONTINUE;
            }

            boolean moving = this.getDeltaMovement().horizontalDistanceSqr() > 0.01;
            state.getController().setAnimation(
                    moving
                            ? RawAnimation.begin().thenLoop("animation.saitama_mob.walk")
                            : RawAnimation.begin().thenLoop("animation.saitama_mob.idle")
            );
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public void playAnimation(String animName, int durationTicks) {
        this.entityData.set(CURRENT_ANIM, animName);
        this.animTicksLeft = durationTicks;
    }

    // ── Tick ──────────────────────────────────────────────────────────────────

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            if (cdNormalPunch  > 0) cdNormalPunch--;
            if (cdConsecutive  > 0) cdConsecutive--;
            if (cdSideHops     > 0) cdSideHops--;
            if (cdTableFlip    > 0) cdTableFlip--;
            if (cdSeriousPunch > 0) cdSeriousPunch--;

            if (seriousPunchActive) tickSeriousPunch();

            // Timer animation
            if (!this.entityData.get(CURRENT_ANIM).equals("idle")) {
                animTicksLeft--;
                if (animTicksLeft <= 0) {
                    this.entityData.set(CURRENT_ANIM, "idle");
                    animTicksLeft = 0;
                }
            }
        }
    }

    // ── Sélection d'attaque ───────────────────────────────────────────────────

    public void performSaitamaAttack(LivingEntity target) {
        Vec3 dir = target.position().subtract(this.position()).normalize();

        if (cdSeriousPunch == 0) {
            startSeriousPunch(target, dir);
            playAnimation("attack_serious", ANIM_SERIOUS);
            cdSeriousPunch = CD_SERIOUS_PUNCH;
        } else if (cdTableFlip == 0) {
            tableFlip(target, dir);
            playAnimation("attack_table", ANIM_TABLE);
            cdTableFlip = CD_TABLE_FLIP;
        } else if (cdSideHops == 0) {
            sideHops();
            playAnimation("attack_sidehops", ANIM_SIDEHOPS);
            cdSideHops = CD_SIDE_HOPS;
        } else if (cdConsecutive == 0) {
            consecutivePunches(target, dir);
            playAnimation("attack_consecutive", ANIM_CONSEC);
            cdConsecutive = CD_CONSECUTIVE;
        } else if (cdNormalPunch == 0) {
            normalPunch(target, dir);
            playAnimation("attack_normal", ANIM_NORMAL);
            cdNormalPunch = CD_NORMAL_PUNCH;
        }
    }

    public boolean canAttack() {
        return cdNormalPunch == 0 || cdConsecutive == 0 || cdSideHops == 0
                || cdTableFlip == 0 || cdSeriousPunch == 0;
    }

    // ── Techniques ────────────────────────────────────────────────────────────

    private void normalPunch(LivingEntity target, Vec3 dir) {
        target.hurt(this.damageSources().mobAttack(this), 10f);
        target.setDeltaMovement(dir.x * 0.6, 0.3, dir.z * 0.6);
        target.hurtMarked = true;
        if (this.level() instanceof ServerLevel sl)
            destroyCylinder(sl, BlockPos.containing(this.getEyePosition().add(dir.scale(3))), dir, 1, 5);
    }

    private void consecutivePunches(LivingEntity target, Vec3 dir) {
        for (int i = 0; i < 4; i++)
            target.hurt(this.damageSources().mobAttack(this), 4f);
        target.setDeltaMovement(dir.x * 0.4, 0.2, dir.z * 0.4);
        target.hurtMarked = true;
    }

    private void sideHops() {
        List<LivingEntity> nearby = this.level().getEntitiesOfClass(
                LivingEntity.class,
                new AABB(this.position(), this.position()).inflate(6.0),
                e -> !e.equals(this) && e instanceof Monster
        );
        for (LivingEntity e : nearby) {
            e.hurt(this.damageSources().mobAttack(this), 6f);
            Vec3 kb = e.position().subtract(this.position()).normalize().scale(0.6);
            e.setDeltaMovement(kb.x, 0.4, kb.z);
            e.hurtMarked = true;
        }
    }

    private void tableFlip(LivingEntity target, Vec3 dir) {
        Vec3 center = this.position().add(dir.scale(4));
        List<LivingEntity> nearby = this.level().getEntitiesOfClass(
                LivingEntity.class,
                new AABB(center, center).inflate(4.0),
                e -> !e.equals(this) && e instanceof Monster
        );
        for (LivingEntity e : nearby) {
            e.hurt(this.damageSources().mobAttack(this), 12f);
            Vec3 kb = e.position().subtract(this.position()).normalize();
            e.setDeltaMovement(kb.x, 1.8, kb.z);
            e.hurtMarked = true;
        }
        if (!(this.level() instanceof ServerLevel)) return;
        BlockPos cPos = BlockPos.containing(center);
        int radius = 4;
        for (int x = -radius; x <= radius; x++)
            for (int z = -radius; z <= radius; z++)
                if (x * x + z * z <= radius * radius)
                    for (int y = -1; y <= 1; y++)
                        removeBlock(cPos.offset(x, y, z));
    }

    private void startSeriousPunch(LivingEntity target, Vec3 dir) {
        Vec3 center = this.getEyePosition().add(dir.scale(10));
        List<LivingEntity> nearby = this.level().getEntitiesOfClass(
                LivingEntity.class,
                new AABB(center, center).inflate(5.0),
                e -> !e.equals(this)
        );
        for (LivingEntity e : nearby) {
            e.hurt(this.damageSources().mobAttack(this), 40f);
            e.setDeltaMovement(dir.x * 3.0, 1.0, dir.z * 3.0);
            e.hurtMarked = true;
        }
        seriousPunchActive = true;
        seriousPunchOffset = 0;
        seriousPunchOrigin = this.getEyePosition();
        seriousPunchDir    = dir;
    }

    private void tickSeriousPunch() {
        if (!(this.level() instanceof ServerLevel level)) return;
        int end = Math.min(seriousPunchOffset + SERIOUS_PUNCH_STEP, SERIOUS_PUNCH_RANGE);
        for (int d = seriousPunchOffset; d < end; d++) {
            Vec3 sphereCenter = seriousPunchOrigin.add(seriousPunchDir.scale(d + 1));
            destroySphere(level, BlockPos.containing(sphereCenter), SERIOUS_PUNCH_RADIUS);
        }
        seriousPunchOffset = end;
        if (seriousPunchOffset >= SERIOUS_PUNCH_RANGE) seriousPunchActive = false;
    }

    // ── Utilitaires blocs ─────────────────────────────────────────────────────

    private void destroySphere(ServerLevel level, BlockPos center, int radius) {
        for (int x = -radius; x <= radius; x++)
            for (int y = -radius; y <= radius; y++)
                for (int z = -radius; z <= radius; z++)
                    if (x*x + y*y + z*z <= radius * radius) {
                        BlockPos pos = center.offset(x, y, z);
                        if (!level.getBlockState(pos).isAir() && !level.getBlockState(pos).liquid())
                            level.removeBlock(pos, false);
                    }
    }

    private void destroyCylinder(ServerLevel level, BlockPos origin, Vec3 dir, int radius, int length) {
        Vec3 up     = Math.abs(dir.y) < 0.9 ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
        Vec3 right  = dir.cross(up).normalize();
        Vec3 upPerp = dir.cross(right).normalize();
        for (int d = 0; d < length; d++) {
            Vec3 point = new Vec3(origin.getX(), origin.getY(), origin.getZ()).add(dir.scale(d + 1));
            for (int r1 = -radius; r1 <= radius; r1++)
                for (int r2 = -radius; r2 <= radius; r2++)
                    if (r1 * r1 + r2 * r2 <= radius * radius) {
                        BlockPos pos = BlockPos.containing(
                                point.x + right.x * r1 + upPerp.x * r2,
                                point.y + right.y * r1 + upPerp.y * r2,
                                point.z + right.z * r1 + upPerp.z * r2
                        );
                        if (!level.getBlockState(pos).isAir() && !level.getBlockState(pos).liquid())
                            level.removeBlock(pos, false);
                    }
        }
    }

    private void removeBlock(BlockPos pos) {
        if (!(this.level() instanceof ServerLevel level)) return;
        if (!level.getBlockState(pos).isAir() && !level.getBlockState(pos).liquid())
            level.removeBlock(pos, false);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return super.hurt(source, amount * 0.1f);
    }
}