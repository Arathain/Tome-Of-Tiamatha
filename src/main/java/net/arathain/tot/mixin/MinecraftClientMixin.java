package net.arathain.tot.mixin;

import net.arathain.tot.common.init.ToTObjects;
import net.arathain.tot.common.item.RemorseItem;
import net.arathain.tot.common.network.packet.RemorsePacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBind;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import javax.swing.text.JTextComponent;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow @Nullable
    public ClientPlayerEntity player;
    @Shadow protected int attackCooldown;
    @Shadow
    @Nullable public HitResult crosshairTarget;
    @Unique
    private boolean attackQueued = false;


    @Inject(method = "handleInputEvents", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/MinecraftClient;doAttack()Z",
            ordinal = 0
    ))
    public void combattweaks$handleAttacking(CallbackInfo info) {
        if(player != null) {
            if(player.getStackInHand(player.getActiveHand()).isOf(ToTObjects.REMORSE)) {
                if(player.getAttackCooldownProgress(0.5F) == 1F && (!player.getItemCooldownManager().isCoolingDown(player.getMainHandStack().getItem())) && crosshairTarget != null) {
                    RemorsePacket.send(crosshairTarget.getType() == HitResult.Type.ENTITY ? ((EntityHitResult) crosshairTarget).getEntity() : null);

                    if(crosshairTarget.getType() == HitResult.Type.BLOCK)
                        player.resetLastAttackedTicks();
                }
            }
        }

        if(!info.isCancelled() && attackQueued)
            attackQueued = false;
    }

    @ModifyVariable(
            method = "startIntegratedServer(Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Function;ZLnet/minecraft/client/MinecraftClient$WorldLoadAction;)V",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient$WorldLoadAction;NONE:Lnet/minecraft/client/MinecraftClient$WorldLoadAction;", ordinal = 0),
            ordinal = 2,
            index = 11,
            name = "bl2",
            require = 1
    )
    private boolean replaceBl2(boolean bl2) {
        return false;
    }
}
