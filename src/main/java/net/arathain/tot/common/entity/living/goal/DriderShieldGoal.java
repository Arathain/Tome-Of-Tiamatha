package net.arathain.tot.common.entity.living.goal;

import net.arathain.tot.common.entity.living.drider.DriderEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.RavagerEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.UseAction;

public class DriderShieldGoal extends Goal {
    public final DriderEntity drider;
    public DriderShieldGoal(DriderEntity driderEntity) {
        drider = driderEntity;
    }
    @Override
    public boolean canStart() {
        return (drider.getOffHandStack().getUseAction() == UseAction.BLOCK && raiseShield() && drider.shieldCooldown == 0);
    }

    @Override
    public void stop() {
            drider.stopUsingItem();
    }
    @Override
    public void start() {
        if (drider.getOffHandStack().getUseAction() == UseAction.BLOCK)
            drider.setCurrentHand(Hand.OFF_HAND);
    }

    @Override
    public boolean shouldContinue() {
        return canStart();
    }

    protected boolean raiseShield() {
        LivingEntity target = drider.getTarget();
        if (target != null && drider.shieldCooldown == 0) {
            return drider.distanceTo(target) <= 3.0D || target instanceof CreeperEntity || target instanceof RangedAttackMob && target.distanceTo(drider) >= 5.0D || target instanceof RavagerEntity || ( target instanceof IronGolemEntity && drider.distanceTo(target) <= 10.0D);
        }
        return false;
    }
}
