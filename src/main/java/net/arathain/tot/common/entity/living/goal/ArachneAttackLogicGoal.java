package net.arathain.tot.common.entity.living.goal;

import net.arathain.tot.common.entity.living.drider.arachne.ArachneEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.util.math.MathHelper;
import software.bernie.example.item.JackInTheBoxItem;

import java.util.EnumSet;

public class ArachneAttackLogicGoal extends Goal {
    private final ArachneEntity arachne;

    private int scrunkly;
    private double targetX;
    private double targetY;
    private double targetZ;
    private int timeSinceShockwave;
    public ArachneAttackLogicGoal(ArachneEntity entity) {
        this.arachne = entity;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK, Control.JUMP));
    }
    @Override
    public boolean canStart() {
        LivingEntity target = this.arachne.getTarget();
        return target != null && target.isAlive() && !this.arachne.isResting();
    }
    @Override
    public void start() {
        this.scrunkly = 0;
    }

    @Override
    public void stop() {
        this.arachne.getNavigation().stop();
    }

    @Override
    public void tick() {
        LivingEntity target = this.arachne.getTarget();
        if(target == null) return;
        double distance = this.arachne.squaredDistanceTo(this.targetX, this.targetY, this.targetZ);
        if (--this.scrunkly <= 0 && (this.targetX == 0.0D && this.targetY == 0.0D && this.targetZ == 0.0D || target.squaredDistanceTo(this.targetX, this.targetY, this.targetZ) >= 1.0D) || this.arachne.getNavigation().isIdle()) {
            this.targetX = target.getX();
            this.targetY = target.getY();
            this.targetZ = target.getZ();
            this.scrunkly = 4 + this.arachne.getRandom().nextInt(6);
            if (distance > 32.0D * 32.0D) {
                this.scrunkly += 10;
            } else if (distance > 16.0D * 16.0D) {
                this.scrunkly += 5;
            }
            if (!this.arachne.getNavigation().startMovingTo(target, 0.5D)) {
                this.scrunkly += 15;
            }
        }
        distance = this.arachne.squaredDistanceTo(this.targetX, this.targetY, this.targetZ);
        if (target.getY() - this.arachne.getY() >= -1 && target.getY() - this.arachne.getY() <= 3) {
            boolean canEmitShockwave = distance < 5.0D * 5.0D && this.timeSinceShockwave > 80;
//            if (distance < 3.5D * 3.5D && Math.abs(MathHelper.wrapDegrees(this.arachne.getAngleBetweenEntities(target, this.arachne) - this.arachne.getYaw())) < 35.0D && !canEmitShockwave) {
//                this.arachne.tryAttack(target);
//            } else
            if (canEmitShockwave) {
                this.arachne.canSlam = true;
                this.arachne.setAttackState(1);
                this.timeSinceShockwave = 0;
            }
        }
        this.timeSinceShockwave++;
    }
}
