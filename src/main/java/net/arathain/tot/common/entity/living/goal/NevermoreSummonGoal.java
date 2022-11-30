package net.arathain.tot.common.entity.living.goal;

import net.arathain.tot.common.entity.living.raven.NevermoreEntity;
import net.arathain.tot.common.entity.living.raven.RavenEntity;
import net.arathain.tot.common.init.ToTEntities;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.random.RandomGenerator;

import java.util.Random;

public class NevermoreSummonGoal extends NevermoreAttackGoal {
    public NevermoreSummonGoal(NevermoreEntity quothed) {
        super(quothed);
    }

    @Override
    protected SoundEvent getChargeSound() {
        return SoundEvents.ENTITY_IRON_GOLEM_REPAIR;
    }

    @Override
    protected int getAttackId() {
        return 2;
    }

    @Override
    protected int getCooldown() {
        return 80;
    }

    @Override
    protected int getWarmupTime() {
        return 40;
    }

    @Override
    protected void attack() {
        LivingEntity target = this.nevermore.getTarget();
        if(target != null && !(nevermore.getWorld().getOtherEntities(nevermore, nevermore.getBoundingBox().expand(40), (entity -> entity instanceof RavenEntity)).size() > 8)) {
            RandomGenerator random = nevermore.getRandom();
            for (int i = 1; i < random.nextInt(8) + 1; i++) {
                RavenEntity raven = new RavenEntity(ToTEntities.RAVEN, nevermore.world);
                raven.setPosition(nevermore.getPos().add((random.nextFloat() * 0.4f - 0.2f), (random.nextFloat() * 0.4f - 0.2f), (random.nextFloat() * 0.4f - 0.2f)));
                raven.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 1000, 1), nevermore);
                raven.setTarget(target);
                nevermore.world.spawnEntity(raven);
            }
        }
    }
}
