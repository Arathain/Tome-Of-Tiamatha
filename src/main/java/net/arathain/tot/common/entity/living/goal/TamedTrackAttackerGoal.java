package net.arathain.tot.common.entity.living.goal;

import net.arathain.tot.common.entity.living.entityinterface.TameableHostileEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.mob.MobEntity;

import java.util.EnumSet;

public class TamedTrackAttackerGoal extends TrackTargetGoal {
    private final TameableHostileEntity tameable;
    private LivingEntity attacker;
    private int lastAttackedTime;

    public TamedTrackAttackerGoal(TameableHostileEntity tameable) {
        super((MobEntity) tameable, false);
        this.tameable = tameable;
        this.setControls(EnumSet.of(Control.TARGET));
    }

    public boolean canStart() {
        if (this.tameable.isTamed() && (this.tameable != null)) {
            LivingEntity livingEntity = this.tameable.getOwner();
            if (livingEntity == null) {
                return false;
            } else {
                this.attacker = livingEntity.getAttacker();
                if (this.attacker != null) {
                    int i = livingEntity.getLastAttackedTime();
                    return i != this.lastAttackedTime && this.canTrack(this.attacker, TargetPredicate.DEFAULT) && this.tameable.canAttackWithOwner(this.attacker, livingEntity);
                }
            }
        }
        return false;
    }
    public void start() {
        this.mob.setTarget(this.attacker);
        LivingEntity livingEntity = this.tameable.getOwner();
        if (livingEntity != null) {
            this.lastAttackedTime = livingEntity.getLastAttackedTime();
        }

        super.start();
    }
}
