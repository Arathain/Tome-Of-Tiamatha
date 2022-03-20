package net.arathain.tot.mixin;

import net.arathain.tot.common.util.ToTUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @ModifyVariable(method = "setPose", at = @At("HEAD"), argsOnly = true)
    private EntityPose modifySetPose(EntityPose pose) {
        if (((Entity) (Object) this) instanceof PlayerEntity player && ToTUtil.isDrider(player) && (pose == EntityPose.FALL_FLYING || pose == EntityPose.SWIMMING)) {
            return EntityPose.STANDING;
        }
        return pose;
    }
}
