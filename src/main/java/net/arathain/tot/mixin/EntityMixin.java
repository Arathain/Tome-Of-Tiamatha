package net.arathain.tot.mixin;

import net.arathain.tot.common.entity.spider.IEntityInitDataTrackerHook;
import net.arathain.tot.common.entity.spider.IEntityMovementHook;
import net.arathain.tot.common.entity.spider.IEntityReadWriteHook;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin implements IEntityMovementHook, IEntityReadWriteHook, IEntityInitDataTrackerHook {
    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
    private void onMovePre(MovementType type, Vec3d pos, CallbackInfo ci) {
        if(this.onMove(type, pos, true)) {
            ci.cancel();
        }
    }

    @Inject(method = "move", at = @At("RETURN"))
    private void onMovePost(MovementType type, Vec3d pos, CallbackInfo ci) {
        this.onMove(type, pos, false);
    }

    @Override
    public boolean onMove(MovementType type, Vec3d pos, boolean pre) {
        return false;
    }

    @Inject(method = "getLandingPos", at = @At("RETURN"), cancellable = true)
    private void onGetOnPosition(CallbackInfoReturnable<BlockPos> ci) {
        BlockPos adjusted = this.getAdjustedOnPosition(ci.getReturnValue());
        if(adjusted != null) {
            ci.setReturnValue(adjusted);
        }
    }

    @Override
    public BlockPos getAdjustedOnPosition(BlockPos onPosition) {
        return null;
    }

    @Inject(method = "isLogicalSideForUpdatingMovement", at = @At("RETURN"), cancellable = true)
    private void onCanTriggerWalking(CallbackInfoReturnable<Boolean> ci) {
        ci.setReturnValue(this.getAdjustedCanTriggerWalking(ci.getReturnValue()));
    }

    @Override
    public boolean getAdjustedCanTriggerWalking(boolean canTriggerWalking) {
        return canTriggerWalking;
    }

    @Inject(method = "readNbt", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V",
            shift = At.Shift.AFTER
    ))
    private void onRead(NbtCompound nbt, CallbackInfo ci) {
        this.onRead(nbt);
    }

    @Override
    public void onRead(NbtCompound nbt) { }

    @Inject(method = "writeNbt", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V",
            shift = At.Shift.AFTER
    ))
    private void onWrite(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> ci) {
        this.onWrite(nbt);
    }

    @Override
    public void onWrite(NbtCompound nbt) { }

    @Shadow(prefix = "shadow$")
    protected void shadow$initDataTracker() { }

    @Redirect(method = "<init>*", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;initDataTracker()V"
    ))
    private void onRegisterData(Entity _this) {
        this.shadow$initDataTracker();

        if(_this == (Object) this) {
            this.onInitDataTracker();
        }
    }

    @Override
    public void onInitDataTracker() { }
}
