package net.arathain.tot.common.entity.living.goal;

import net.arathain.tot.common.entity.living.raven.NevermoreEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public class NevermoreYeetGoal extends NevermoreAttackGoal {
    public NevermoreYeetGoal(NevermoreEntity quothed) {
        super(quothed);
    }

    @Override
    protected SoundEvent getChargeSound() {
        return SoundEvents.ENTITY_IRON_GOLEM_REPAIR;
    }

    @Override
    protected int getAttackId() {
        return 1;
    }

    @Override
    protected int getCooldown() {
        return 60;
    }

    @Override
    protected int getWarmupTime() {

        return 16;
    }

    @Override
    public void start() {
        LivingEntity target = this.nevermore.getTarget();
        if (target != null) {
            this.nevermore.teleportTo(target);
        }
        super.start();
    }

    @Override
    protected void attack() {
        LivingEntity target = this.nevermore.getTarget();
        if (target != null && this.nevermore.squaredDistanceTo(target) < 4) {
            float f = (float) this.nevermore.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            float g = (int)f > 0 ? f / 2.0F + (float)this.nevermore.getRandom().nextInt((int)f) : f;
            boolean bl = target.damage(DamageSource.mob(this.nevermore), g);
            if (bl) {
                target.setVelocity(target.getVelocity().add(0.0D, 1.2000000059604645D, 0.0D));
                this.nevermore.applyDamageEffects(this.nevermore, target);
            }

            this.nevermore.playSound(nevermore.getAttackSound(), 1.0F, 1.0F);
        }
    }


}
