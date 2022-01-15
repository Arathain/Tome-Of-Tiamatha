package net.arathain.tot.common.entity.goal;

import net.arathain.tot.common.entity.DriderEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.util.Hand;
import net.minecraft.util.UseAction;

public class DriderAttackGoal extends MeleeAttackGoal {
    private final DriderEntity drider;

    public DriderAttackGoal(DriderEntity drider, double speed, boolean pauseWhenMobIdle) {
        super(drider, speed, pauseWhenMobIdle);
        this.drider = drider;
    }

    @Override
    public boolean canStart() {
        return this.drider.getTarget() != null && super.canStart();
    }

    @Override
    public boolean shouldContinue() {
        return this.drider.getTarget() != null && super.shouldContinue();
    }


    @Override
    public void stop() {
        super.stop();
        this.drider.setAttacking(false);
    }

    @Override
    public void tick() {
        LivingEntity target = drider.getTarget();
        if (target != null) {
            if (target.distanceTo(drider) <= 3.0D && !drider.isBlocking()) {
                drider.getMoveControl().strafeTo(-2.0F, 0.0F);
                drider.lookAtEntity(target, 30.0F, 30.0F);
            }
//            if (path != null && target.distanceTo(drider) <= 2.0D)
//                drider.getNavigation().stop();
            super.tick();
        }
    }
    @Override
    protected double getSquaredMaxAttackDistance(LivingEntity attackTarget) {
        return super.getSquaredMaxAttackDistance(attackTarget) * 3.55D;
    }

    @Override
    protected void attack(LivingEntity target, double squaredDistance) {
        double d0 = this.getSquaredMaxAttackDistance(target);
        if (squaredDistance <= d0 && this.getCooldown() <= 0 && (this.drider.attackedCooldown > 0 || drider.getOffHandStack().getUseAction() != UseAction.BLOCK || drider.hurtTime > 100 || (drider.getAttacker() == null))) {
            this.resetCooldown();
            this.drider.stopUsingItem();
            if (drider.shieldCooldown == 0)
                this.drider.shieldCooldown = 6;
            this.drider.swingHand(Hand.MAIN_HAND);
            this.drider.tryAttack(target);
        }
    }
}
