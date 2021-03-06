package net.arathain.tot.common.entity.living.goal;

import net.arathain.tot.common.entity.living.drider.DriderEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.RavagerEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.UseAction;

import java.util.List;

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
        List<Entity> projectiles = drider.world.getOtherEntities(drider, drider.getBoundingBox().expand(5), entity -> entity instanceof ProjectileEntity);
        if (target != null && drider.shieldCooldown == 0) {
            return !projectiles.isEmpty() || drider.distanceTo(target) <= 2.0D || (target instanceof PlayerEntity player && drider.distanceTo(target) <= 4.0D && !player.isBlocking()) || target instanceof CreeperEntity || (target instanceof RangedAttackMob && target.distanceTo(drider) >= 5.0D && drider.attackedCooldown == 0) || target instanceof RavagerEntity || (target instanceof IronGolemEntity && drider.distanceTo(target) <= 3.5D);
        }
        return false;
    }
}
