package net.arathain.tot.mixin;

import net.arathain.tot.common.entity.spider.ILivingEntityHook;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements ILivingEntityHook {
    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @ModifyVariable(method = "lookAt(Lnet/minecraft/command/argument/EntityAnchorArgumentType$EntityAnchor;Lnet/minecraft/util/math/Vec3d;)V", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private Vec3d onLookAtModify(Vec3d vec, EntityAnchorArgumentType.EntityAnchor anchor, Vec3d vec2) {
        return this.onLookAt(anchor, vec);
    }

    @Override
    public Vec3d onLookAt(EntityAnchorArgumentType.EntityAnchor anchor, Vec3d vec) {
        return vec;
    }
    @Inject(method = "onTrackedDataSet(Lnet/minecraft/entity/data/TrackedData;)V", at = @At("HEAD"))
    private void onNotifyTrackedData(TrackedData<?> data, CallbackInfo ci) {
        this.onNotifyTrackedDataSet(data);
    }

    @Override
    public void onNotifyTrackedDataSet(TrackedData<?> key) { }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void onTravelPre(Vec3d relative, CallbackInfo ci) {
        if(this.onTravel(relative, true)) {
            ci.cancel();
        }
    }

    @Inject(method = "travel", at = @At("RETURN"))
    private void onTravelPost(Vec3d relative, CallbackInfo ci) {
        this.onTravel(relative, false);
    }

    @Override
    public boolean onTravel(Vec3d relative, boolean pre) {
        return false;
    }

    @Inject(method = "jump()V", at = @At("HEAD"), cancellable = true)
    private void onJump(CallbackInfo ci) {
        if(this.onJump()) {
            ci.cancel();
        }
    }

    @Override
    public boolean onJump() {
        return false;
    }
}
