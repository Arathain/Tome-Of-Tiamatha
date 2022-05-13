package net.arathain.tot.common.entity.living.goal;

import net.arathain.tot.common.entity.living.raven.NevermoreEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.sound.SoundEvent;

public abstract class NevermoreAttackGoal extends Goal {
    protected final NevermoreEntity nevermore;
    protected int attackCooldown;
    protected int nextAttackTime;

    public NevermoreAttackGoal(NevermoreEntity quothed) {
        nevermore = quothed;
    }
    @Override
    public void start() {
        this.attackCooldown = this.getWarmupTime();
        this.nextAttackTime = this.nevermore.age + this.getCooldown();
        this.nevermore.playSound(this.getChargeSound(), 1.0F, 1.0F);
        this.nevermore.setAttackState(this.getAttackId());
    }
    @Override
    public boolean canStart() {
        LivingEntity target = this.nevermore.getTarget();
        if (target != null && target.isAlive() && this.nevermore.getAttackState() == 0) {
            return this.nevermore.age >= this.nextAttackTime;
        } else {
            return false;
        }
    }

    @Override
    public boolean shouldContinue() {
        LivingEntity target = this.nevermore.getTarget();
        return target != null && target.isAlive() && this.attackCooldown > 0;
    }

    protected abstract SoundEvent getChargeSound();
    protected abstract int getAttackId();

    @Override
    public void stop() {
        this.nevermore.setAttackState(0);
    }

    @Override
    public void tick() {
        --this.attackCooldown;
        if (this.attackCooldown == 0) {
            this.attack();
            this.nevermore.playSound(this.nevermore.getAttackSound(), 1.0F, 1.0F);
            this.attackCooldown = this.getWarmupTime();
            this.nevermore.setAttackState(0);
        }
    }
    protected abstract int getCooldown();

    protected abstract int getWarmupTime();

    protected abstract void attack();
}