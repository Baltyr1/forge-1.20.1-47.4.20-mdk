package net.baltyr.opmmod.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class SaitamaAttackGoal extends Goal {

    private final SaitamaMob saitama;
    private LivingEntity target;
    private static final double DETECTION_RANGE = 10.0;

    public SaitamaAttackGoal(SaitamaMob saitama) {
        this.saitama = saitama;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        target = findNearestMonster();
        if (target == null) target = saitama.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        return target != null && target.isAlive()
                && target.distanceTo(saitama) <= DETECTION_RANGE + 2;
    }

    @Override
    public void start() {
        saitama.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (target == null || !target.isAlive()) return;

        double dist = saitama.distanceTo(target);

        if (dist > 2.5) {
            saitama.getNavigation().moveTo(target, 1.2);
        } else {
            saitama.getNavigation().stop();
        }

        saitama.getLookControl().setLookAt(target, 30f, 30f);

        // Attaque si assez proche et qu'une attaque est disponible
        if (dist <= 3.5 && saitama.canAttack()) {
            saitama.performSaitamaAttack(target);
        }
    }

    @Override
    public void stop() {
        target = null;
        saitama.getNavigation().stop();
    }

    private LivingEntity findNearestMonster() {
        List<Monster> monsters = saitama.level().getEntitiesOfClass(
                Monster.class,
                new AABB(saitama.position(), saitama.position()).inflate(DETECTION_RANGE),
                e -> !e.equals(saitama) && e.isAlive()
        );
        if (monsters.isEmpty()) return null;
        return monsters.stream()
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(saitama)))
                .orElse(null);
    }
}