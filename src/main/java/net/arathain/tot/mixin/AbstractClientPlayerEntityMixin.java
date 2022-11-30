package net.arathain.tot.mixin;

import com.mojang.authlib.GameProfile;
import net.arathain.tot.TomeOfTiamatha;
import net.arathain.tot.common.init.ToTComponents;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerEntityMixin extends PlayerEntity {
    public AbstractClientPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile, @Nullable PlayerPublicKey publicKey) {
        super(world, pos, yaw, gameProfile, publicKey);
    }

    @Inject(method = "getSkinTexture", at = @At("HEAD"), cancellable = true)
    private void getDriderSkinTexture(CallbackInfoReturnable<Identifier> cir) {
        if(ToTComponents.DRIDER_COMPONENT.get(this).getStage() > 0) {
            cir.setReturnValue(new Identifier(TomeOfTiamatha.MODID, "textures/entity/drider/tf/tf_" + ToTComponents.DRIDER_COMPONENT.get(this).getVariant().toString().toLowerCase() + ".png"));
        }
    }
    @Inject(method = "getModel", at = @At("HEAD"), cancellable = true)
    private void getDriderSlimModel(CallbackInfoReturnable<String> cir) {
        if(ToTComponents.DRIDER_COMPONENT.get(this).getStage() > 0) {
            cir.setReturnValue("slim");
        }
    }
}
