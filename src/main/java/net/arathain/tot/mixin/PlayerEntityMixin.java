package net.arathain.tot.mixin;

import net.arathain.tot.common.init.ToTComponents;
import net.minecraft.block.BlockState;
import net.minecraft.block.CobwebBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public boolean isClimbing() {
        if(!super.isClimbing() && ToTComponents.DRIDER_COMPONENT.get(this).isDrider() && horizontalCollision || this.getBlockStateAtPos().getBlock() instanceof CobwebBlock) {
            return true;
        } else {
            return super.isClimbing();
        }
    }
    @Inject(at = @At("HEAD"), method = "handleFallDamage", cancellable = true)
    private void handleFallDamage(float fallDistance, float damageMultiplier, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if(ToTComponents.DRIDER_COMPONENT.get(this).isDrider() && !this.hasStatusEffect(StatusEffects.WEAKNESS)) {
            cir.setReturnValue(false);
        }

    }

    @Inject(at = @At("HEAD"), method = "slowMovement", cancellable = true)
    public void slowMovement(BlockState state, Vec3d multiplier, CallbackInfo info) {
        if (ToTComponents.DRIDER_COMPONENT.get(this).isDrider() && state.getBlock() instanceof CobwebBlock) {
            info.cancel();
        }
    }
}
