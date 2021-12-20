package net.arathain.tot.mixin;

import net.arathain.tot.common.init.ToTComponents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public boolean isClimbing() {
        if(!super.isClimbing() && ToTComponents.DRIDER_COMPONENT.get(this).isDrider() && horizontalCollision) {
            return true;
        } else {
            return super.isClimbing();
        }
    }
}
