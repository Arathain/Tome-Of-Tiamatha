package net.arathain.tot.common.entity.living.goal;

import net.arathain.tot.common.util.ToTUtil;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.mob.PathAwareEntity;

public class ArachneRevengeGoal extends RevengeGoal {
    public ArachneRevengeGoal(PathAwareEntity mob, Class<?>... noRevengeTypes) {
        super(mob, noRevengeTypes);
    }

    @Override
    public boolean canStart() {
        return !ToTUtil.isDrider(this.mob.getAttacker()) && super.canStart();
    }
}
