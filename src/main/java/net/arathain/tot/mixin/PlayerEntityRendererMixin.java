package net.arathain.tot.mixin;

import net.arathain.tot.common.entity.DriderEntity;
import net.arathain.tot.common.entity.ToTUtil;
import net.arathain.tot.common.init.ToTComponents;
import net.arathain.tot.common.init.ToTEntities;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.bernie.example.client.renderer.entity.ReplacedCreeperRenderer;
import software.bernie.geckolib3.core.processor.IBone;

import java.util.Optional;

@Environment(EnvType.CLIENT)
@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {


    @Shadow
    protected abstract void setModelPose(AbstractClientPlayerEntity player);

    public PlayerEntityRendererMixin(EntityRendererFactory.Context ctx, PlayerEntityModel<AbstractClientPlayerEntity> model, float shadowRadius) {
        super(ctx, model, shadowRadius);
    }

    @Inject(method = "render(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"), cancellable = true)
    private void render(AbstractClientPlayerEntity player, float yaw, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, CallbackInfo callbackInfo) {
        LivingEntity entity = getDrider(player);
        if (entity != null) {
            entity.age = player.age;
            entity.hurtTime = player.hurtTime;
            entity.maxHurtTime = Integer.MAX_VALUE;
            entity.limbDistance = player.limbDistance;
            entity.lastLimbDistance = player.lastLimbDistance;
            entity.limbAngle = player.limbAngle;
            entity.headYaw = player.headYaw;
            entity.prevHeadYaw = player.prevHeadYaw;
            entity.bodyYaw = player.bodyYaw;
            entity.prevBodyYaw = player.prevBodyYaw;
            entity.handSwinging = player.handSwinging;
            entity.handSwingTicks = player.handSwingTicks;
            entity.handSwingProgress = player.handSwingProgress;
            entity.lastHandSwingProgress = player.lastHandSwingProgress;
            entity.setPitch(player.getPitch());
            entity.prevPitch = player.prevPitch;
            entity.preferredHand = player.preferredHand;
            entity.setStackInHand(Hand.MAIN_HAND, player.getMainHandStack());
            entity.setStackInHand(Hand.OFF_HAND, player.getOffHandStack());
            entity.setCurrentHand(player.getActiveHand() == null ? Hand.MAIN_HAND : player.getActiveHand());
            //blursed
            entity.equipStack(EquipmentSlot.HEAD, player.getEquippedStack(EquipmentSlot.HEAD));
            entity.equipStack(EquipmentSlot.CHEST, player.getEquippedStack(EquipmentSlot.CHEST));
            entity.equipStack(EquipmentSlot.LEGS, player.getEquippedStack(EquipmentSlot.LEGS));
            entity.equipStack(EquipmentSlot.FEET, player.getEquippedStack(EquipmentSlot.FEET));
            entity.setSneaking(player.isSneaking());
            entity.setPose(player.getPose());
            if (player.hasVehicle()) {
                entity.startRiding(player.getVehicle(), true);
            }
            float width = 1 / (entity.getType().getWidth() / EntityType.PLAYER.getWidth());
            matrixStack.scale(width, 1 / (entity.getType().getHeight() / EntityType.PLAYER.getHeight()), width);
            MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(entity).render(entity, yaw, tickDelta, matrixStack, vertexConsumerProvider, light);
            callbackInfo.cancel();
        }
    }
    @Inject(method = "renderArm", at = @At("HEAD"), cancellable = true)
    private void renderArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, ModelPart arm, ModelPart sleeve, CallbackInfo ci) {
        LivingEntity entity = getDrider(player);
        if(entity != null) {
            ci.cancel();
        }
    }

    private static DriderEntity getDrider(PlayerEntity player) {
        if (ToTUtil.isDrider(player)) {
            DriderEntity entity = ToTEntities.DRIDER.create(player.world);
            assert entity != null;
            entity.getDataTracker().set(DriderEntity.TYPE, ToTComponents.DRIDER_COMPONENT.get(player).getVariant().toString());
            return entity;
        }
        return null;
    }
}
