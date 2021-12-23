package net.arathain.tot.mixin;

import net.arathain.tot.common.entity.spider.IMobEntityHook;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin implements IMobEntityHook {
    @Inject(method = "mobTick()V", at = @At("HEAD"))
    private void onLivingTick(CallbackInfo ci) {
        this.onLivingTick();
    }

    @Override
    public void onLivingTick() { }

    @Inject(method = "tick()V", at = @At("RETURN"))
    private void onTick(CallbackInfo ci) {
        this.onTick();
    }

    @Override
    public void onTick() { }

    @Shadow(prefix = "shadow$")
    protected void shadow$initGoals() { }

    @Redirect(method = "<init>*", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/mob/MobEntity;initGoals()V"
    ))
    private void onInitGoals(MobEntity _this) {
        this.shadow$initGoals();

        if(_this == (Object) this) {
            this.onInitGoals();
        }
    }

    @Override
    public void onInitGoals() { }

    @Inject(method = "createNavigation", at = @At("HEAD"), cancellable = true)
    private void onCreateNavigation(World world, CallbackInfoReturnable<EntityNavigation> ci) {
        EntityNavigation navigator = this.onCreateNavigation(world);
        if(navigator != null) {
            ci.setReturnValue(navigator);
        }
    }

    @Override
    public EntityNavigation onCreateNavigation(World world) {
        return null;
    }
}
