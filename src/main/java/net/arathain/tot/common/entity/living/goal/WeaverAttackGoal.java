package net.arathain.tot.common.entity.living.goal;

import net.arathain.tot.common.entity.living.drider.weaver.WeaverEntity;
import net.arathain.tot.common.init.ToTEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.BowItem;
import net.minecraft.item.Items;

import java.util.EnumSet;

public class WeaverAttackGoal extends Goal {
    private final WeaverEntity obj;
    private final double speed;
    private int attackInterval;
    private final float squaredRange;
    private int cooldown = -1;
    private int targetSeeingTicker;
    private boolean movingToLeft;
    private boolean backward;
    private int combatTicks = -1;

    public WeaverAttackGoal(WeaverEntity obj, double speed, int attackInterval, float range) {
        this.obj = obj;
        this.speed = speed;
        this.attackInterval = attackInterval;
        this.squaredRange = range * range;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {
        return obj.getTarget() != null && !obj.getTarget().hasStatusEffect(ToTEffects.BROODS_CURSE) || (obj.getTarget().hasStatusEffect(ToTEffects.BROODS_CURSE)  && !(obj.getTarget().getStatusEffect(ToTEffects.BROODS_CURSE).getDuration() < 400));
    }
    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }

    public void setAttackInterval(int attackInterval) {
        this.attackInterval = attackInterval;
    }

    @Override
    public boolean shouldContinue() {
        return (this.canStart() || !((MobEntity)this.obj).getNavigation().isIdle());
    }

    @Override
    public void start() {
        super.start();
        ((MobEntity)this.obj).setAttacking(true);
    }

    @Override
    public void stop() {
        super.stop();
        this.obj.setAttacking(false);
        this.targetSeeingTicker = 0;
        this.cooldown = -1;
        this.obj.clearActiveItem();
    }

    @Override
    public void tick() {
        boolean bl2;
        LivingEntity livingEntity = this.obj.getTarget();
        if (livingEntity == null) {
            return;
        }
        double d = this.obj.squaredDistanceTo(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
        boolean bl = this.obj.getVisibilityCache().canSee(livingEntity);
        boolean bl3 = bl2 = this.targetSeeingTicker > 0;
        if (bl != bl2) {
            this.targetSeeingTicker = 0;
        }
        if (bl) {
            ++this.targetSeeingTicker;
        } else {
            --this.targetSeeingTicker;
        }
        if (d > (double)this.squaredRange || this.targetSeeingTicker < 20) {
            this.obj.getNavigation().startMovingTo(livingEntity, this.speed);
            this.combatTicks = -1;
        } else {
            this.obj.getNavigation().stop();
            ++this.combatTicks;
        }
        if (this.combatTicks >= 20) {
            if ((double) this.obj.getRandom().nextFloat() < 0.3) {
                boolean bl4 = this.movingToLeft = !this.movingToLeft;
            }
            if ((double) this.obj.getRandom().nextFloat() < 0.3) {
                this.backward = !this.backward;
            }
            this.combatTicks = 0;
        }
        if (this.combatTicks > -1) {
            if (d > (double)(this.squaredRange * 0.75f)) {
                this.backward = false;
            } else if (d < (double)(this.squaredRange * 0.25f)) {
                this.backward = true;
            }
            this.obj.getMoveControl().strafeTo(this.backward ? -0.5f : 0.5f, this.movingToLeft ? 0.5f : -0.5f);
            this.obj.lookAtEntity(livingEntity, 30.0f, 30.0f);
        } else {
            this.obj.getLookControl().lookAt(livingEntity, 30.0f, 30.0f);
        }
        if (this.obj.isUsingItem()) {
            int i;
            if (!bl && this.targetSeeingTicker < -60) {
                this.obj.clearActiveItem();
            } else if (bl && (i = this.obj.getItemUseTime()) >= 20) {
                this.obj.clearActiveItem();
                //((RangedAttackMob)this.obj).attack(livingEntity, BowItem.getPullProgress(i));
                this.cooldown = this.attackInterval;
            }
        } else if (--this.cooldown <= 0 && this.targetSeeingTicker >= -60) {
            ((LivingEntity)this.obj).setCurrentHand(ProjectileUtil.getHandPossiblyHolding(this.obj, Items.BOW));
        }
    }
}
