package net.arathain.tot.common.entity.living.goal;

import net.arathain.tot.common.util.ToTUtil;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;

public class ArachneRevengeGoal extends RevengeGoal {
    public ArachneRevengeGoal(PathAwareEntity mob, Class<?>... noRevengeTypes) {
        super(mob, noRevengeTypes);
    }

    @Override
    public boolean canStart() {
        if (ToTUtil.isDrider(this.mob.getAttacker())) return false;
        if (!(this.mob.getAttacker() instanceof PlayerEntity)) return true;
        PlayerEntity player = (PlayerEntity) this.mob.getAttacker();
        return !player.getAbilities().creativeMode && !ToTUtil.isDrider(player) && super.canStart();
    }
}
