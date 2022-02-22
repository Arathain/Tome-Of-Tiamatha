package net.arathain.tot.common.entity.living.goal;

import net.arathain.tot.common.entity.living.raven.RavenEntity;
import net.minecraft.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.entity.passive.TameableEntity;

public class RavenFollowOwnerGoal extends FollowOwnerGoal {
    private TameableEntity tamed;
    public RavenFollowOwnerGoal(TameableEntity tameable, double speed, float minDistance, float maxDistance, boolean leavesAllowed) {
        super(tameable, speed, minDistance, maxDistance, leavesAllowed);
        tamed = tameable;
    }

    @Override
    public boolean canStart() {
        return !tamed.getDataTracker().get(RavenEntity.GOING_TO_RECEIVER) && super.canStart();
    }

    @Override
    public boolean shouldContinue() {
        if(tamed.getDataTracker().get(RavenEntity.GOING_TO_RECEIVER)) {
            return false;
        }
        return super.shouldContinue();
    }
}
