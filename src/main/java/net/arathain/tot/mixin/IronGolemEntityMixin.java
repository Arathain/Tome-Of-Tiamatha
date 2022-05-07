package net.arathain.tot.mixin;

import net.arathain.tot.common.util.ToTUtil;
import net.arathain.tot.common.init.ToTComponents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.TargetGoal;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IronGolemEntity.class)
public abstract class IronGolemEntityMixin extends GolemEntity {
    protected IronGolemEntityMixin(EntityType<? extends GolemEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "initGoals", at = @At("HEAD"))
    public void inmitGoalsm(CallbackInfo info) {
        this.goalSelector.add(3, new TargetGoal<>(this, PlayerEntity.class, 10, true, false, player -> ToTUtil.isDrider(player) || ToTComponents.ALIGNMENT_COMPONENT.get(player).getVAlignment() < 0));
    }
}
