package net.arathain.tot.mixin;

import net.arathain.tot.client.entity.model.drider.weaver.WebbingEntityModel;
import net.arathain.tot.common.entity.living.drider.weaver.WebbingEntity;
import net.arathain.tot.common.util.ToTUtil;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow public abstract void setHeadYaw(float headYaw);

    @Shadow public abstract void setBodyYaw(float bodyYaw);

    @Shadow public abstract float getYaw(float tickDelta);

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "setPositionInBed", at = @At("HEAD"))
    private void bedPosAdjustmentorsmthidk(CallbackInfo ci) {
        if(ToTUtil.isDrider(this)) {
                this.setBodyYaw(-this.getYaw());
                this.setHeadYaw(-this.getYaw());
                this.setPitch(-this.getPitch());
        }
    }
    @Inject(method = "canTarget(Lnet/minecraft/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
    private void webbingShenanigans(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if(entity.getVehicle() instanceof WebbingEntity) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "getGroup", at = @At("HEAD"), cancellable = true)
    private void getGroup(CallbackInfoReturnable<EntityGroup> callbackInfo) {if ((Object) this instanceof PlayerEntity) {
        if (ToTUtil.isDrider(this)) {
            callbackInfo.setReturnValue(EntityGroup.ARTHROPOD);
        }
    }

    }
}
