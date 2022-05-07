package net.arathain.tot.mixin;

import net.arathain.tot.common.util.ToTUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.TargetGoal;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TargetGoal.class)
public abstract class TargetGoalMixin extends TrackTargetGoal {
    @Shadow
    protected LivingEntity targetEntity;
    public TargetGoalMixin(MobEntity mob, boolean checkVisibility) {
        super(mob, checkVisibility);
    }
    @Inject(at = @At("TAIL"), method = "findClosestTarget")
    protected void findTarget(CallbackInfo callbackInfo) {
        if(targetEntity != null && !ToTUtil.isLookingAt(this.mob, targetEntity)){
            targetEntity = null;
        }
    }

}
