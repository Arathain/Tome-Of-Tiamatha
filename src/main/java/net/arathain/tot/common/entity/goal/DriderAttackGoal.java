package net.arathain.tot.common.entity.goal;

import net.arathain.tot.common.entity.DriderEntity;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;

public class DriderAttackGoal extends MeleeAttackGoal {
    private final DriderEntity drider;
    private int ticks;

    public DriderAttackGoal(DriderEntity drider, double speed, boolean pauseWhenMobIdle) {
        super(drider, speed, pauseWhenMobIdle);
        this.drider = drider;
    }

    @Override
    public void start() {
        super.start();
        this.ticks = 0;
    }

    @Override
    public void stop() {
        super.stop();
        this.drider.setAttacking(false);
    }

    @Override
    public void tick() {
        super.tick();
        ++this.ticks;
        if (this.ticks >= 2 && this.getCooldown() < this.getMaxCooldown() / 4) {
            this.drider.setAttacking(true);
        } else {
            this.drider.setAttacking(false);
        }
    }
}
