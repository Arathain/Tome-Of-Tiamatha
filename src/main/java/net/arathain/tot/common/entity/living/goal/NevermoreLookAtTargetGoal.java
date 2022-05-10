package net.arathain.tot.common.entity.living.goal;

import net.arathain.tot.common.entity.living.raven.NevermoreEntity;
import net.minecraft.entity.ai.goal.Goal;

import java.util.EnumSet;

public class NevermoreLookAtTargetGoal extends Goal {
    private final NevermoreEntity nevermore;

    public NevermoreLookAtTargetGoal(NevermoreEntity aaa) {
        this.nevermore = aaa;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {
        return this.nevermore.getAttackState() != 0;
    }

    @Override
    public void start() {
        super.start();
        this.nevermore.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (this.nevermore.getTarget() != null) {
            this.nevermore.getLookControl().lookAt(this.nevermore.getTarget(), (float)this.nevermore.getLookYawSpeed(), (float)this.nevermore.getLookPitchSpeed());
        }
    }
}
