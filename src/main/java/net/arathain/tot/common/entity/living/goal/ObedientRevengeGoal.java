package net.arathain.tot.common.entity.living.goal;

import net.arathain.tot.common.util.ToTUtil;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.GameRules;

public class ObedientRevengeGoal extends RevengeGoal {
    public ObedientRevengeGoal(PathAwareEntity mob, Class<?>... noRevengeTypes) {
        super(mob, noRevengeTypes);
    }
    @Override
    public boolean canStart() {
        int i = this.mob.getLastAttackedTime();
        LivingEntity livingEntity = this.mob.getAttacker();
        if (ToTUtil.isDrider(livingEntity)) {
            return false;
        }
        return super.canStart();
    }
}
