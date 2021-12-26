package net.arathain.tot.mixin;

import net.arathain.tot.common.entity.ToTUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.CobwebBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SpiderEntity.class)
public class SpiderEntityMixin extends HostileEntity {
    protected SpiderEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/goal/GoalSelector;add(ILnet/minecraft/entity/ai/goal/Goal;)V", ordinal = 7), method = "initGoals")
    private void redirectTargetGoal(GoalSelector goalSelector, int priority, Goal goal) {
        Goal newGoal = new ActiveTargetGoal<>(this, PlayerEntity.class, 10, true, false, player -> !ToTUtil.isDrider(player));
        goalSelector.add(priority, newGoal);
    }

    @Override
    public void slowMovement(BlockState state, Vec3d multiplier) {
        if(!(state.getBlock() instanceof CobwebBlock)) {
            super.slowMovement(state, multiplier);
        }
    }
}
