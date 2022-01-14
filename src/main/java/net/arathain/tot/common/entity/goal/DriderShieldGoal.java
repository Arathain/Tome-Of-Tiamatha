package net.arathain.tot.common.entity.goal;

import net.arathain.tot.common.entity.DriderEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.RavagerEntity;
import net.minecraft.item.ShieldItem;
import net.minecraft.util.Hand;

public class DriderShieldGoal extends Goal {
    public final DriderEntity drider;
    public DriderShieldGoal(DriderEntity driderEntity) {
        drider = driderEntity;
    }
    @Override
    public boolean canStart() {
        return (drider.getOffHandStack().getItem() instanceof ShieldItem && raiseShield() && drider.shieldCooldown == 0);
    }

    @Override
    public void stop() {
            drider.stopUsingItem();
    }
    @Override
    public void start() {
        if (drider.getOffHandStack().getItem() instanceof ShieldItem)
            drider.setCurrentHand(Hand.OFF_HAND);
    }

    @Override
    public boolean shouldContinue() {
        return canStart();
    }

    protected boolean raiseShield() {
        LivingEntity target = drider.getTarget();
        if (target != null && drider.shieldCooldown == 0) {
            return drider.distanceTo(target) <= 4.0D || target instanceof CreeperEntity || target instanceof RangedAttackMob && target.distanceTo(drider) >= 5.0D && target instanceof RavagerEntity;
        }
        return false;
    }
}
