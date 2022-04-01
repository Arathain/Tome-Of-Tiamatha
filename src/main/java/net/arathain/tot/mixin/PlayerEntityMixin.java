package net.arathain.tot.mixin;

import net.arathain.tot.common.component.DriderPlayerComponent;
import net.arathain.tot.common.entity.living.drider.DriderEntity;
import net.arathain.tot.common.entity.living.drider.weaver.WebbingEntity;
import net.arathain.tot.common.init.ToTComponents;
import net.minecraft.block.BlockState;
import net.minecraft.block.CobwebBlock;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "shouldDismount", at = @At("HEAD"), cancellable = true)
    private void webbingScuffedry(CallbackInfoReturnable<Boolean> cir) {
        if(this.getVehicle() instanceof WebbingEntity) {
            cir.setReturnValue(false);
        }
    }

    @Override
    public boolean isSneaking() {
        if(this.getVehicle() instanceof WebbingEntity) {
            return false;
        }
        return super.isSneaking();
    }

    @Override
    public boolean isInSneakingPose() {
        if(this.getVehicle() instanceof WebbingEntity) {
            return false;
        }
        return super.isInSneakingPose();
    }

    @Override
    public boolean isClimbing() {
        if(!super.isClimbing() && !(this.isOnGround() || this.isSprinting()) && ToTComponents.DRIDER_COMPONENT.get(this).isDrider() && horizontalCollision || this.getBlockStateAtPos().getBlock() instanceof CobwebBlock) {
            this.climbingPos = Optional.of(this.getBlockPos());
            return true;
        } else {
            return super.isClimbing();
        }
    }

    @Override
    public boolean canHaveStatusEffect(StatusEffectInstance effect) {
        if(ToTComponents.DRIDER_COMPONENT.get(this).isDrider() && (effect.getEffectType() == StatusEffects.SATURATION || effect.getEffectType() == StatusEffects.HUNGER || effect.getEffectType() == StatusEffects.POISON)) {
            return false;
        }
        return super.canHaveStatusEffect(effect);
    }

    @Inject(method = "getDimensions", at = @At("HEAD"), cancellable = true)
    private void driderDims(EntityPose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        if(pose == EntityPose.CROUCHING && ToTComponents.DRIDER_COMPONENT.get(this).isDrider()) {
            cir.setReturnValue(DriderEntity.crouchingDimensions);
        }
    }

    @Inject(at = @At("HEAD"), method = "handleFallDamage", cancellable = true)
    private void handleFallDamage(float fallDistance, float damageMultiplier, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if(ToTComponents.DRIDER_COMPONENT.get(this).isDrider()) {
            cir.setReturnValue(false);
        }

    }
    @Inject(at = @At("HEAD"), method = "getActiveEyeHeight", cancellable = true)
    private void driderEyes(EntityPose pose, EntityDimensions dimensions, CallbackInfoReturnable<Float> cir) {
        if(pose == EntityPose.CROUCHING && ToTComponents.DRIDER_COMPONENT.get(this).isDrider()) {
            cir.setReturnValue(dimensions.height - 0.5f);
        }
    }

    @Inject(at = @At("HEAD"), method = "slowMovement", cancellable = true)
    public void slowMovement(BlockState state, Vec3d multiplier, CallbackInfo info) {
        if (ToTComponents.DRIDER_COMPONENT.get(this).isDrider() && state.getBlock() instanceof CobwebBlock) {
            info.cancel();
            this.movementMultiplier = new Vec3d(3, 3, 3);
        }
    }
    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void adjustDriderSneaking(CallbackInfo info) {
        if (ToTComponents.DRIDER_COMPONENT.get(this).isDrider()) {
            if(isInSneakingPose()) {
                EntityAttributeInstance modifiableattributeinstance = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
                modifiableattributeinstance.removeModifier(DriderPlayerComponent.DRIDER_MOVEMENT_SPEED_MODIFIER);
                modifiableattributeinstance.addTemporaryModifier(DriderPlayerComponent.DRIDER_MOVEMENT_SPEED_MODIFIER);
            } else {
                if (this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).hasModifier(DriderPlayerComponent.DRIDER_MOVEMENT_SPEED_MODIFIER))
                    this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).removeModifier(DriderPlayerComponent.DRIDER_MOVEMENT_SPEED_MODIFIER);
            }
        }
    }
}
