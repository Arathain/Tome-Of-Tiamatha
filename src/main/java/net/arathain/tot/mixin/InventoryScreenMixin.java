package net.arathain.tot.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.arathain.tot.common.entity.DriderEntity;
import net.arathain.tot.common.entity.ToTUtil;
import net.arathain.tot.common.init.ToTComponents;
import net.arathain.tot.common.init.ToTEntities;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractInventoryScreen<PlayerScreenHandler> {

    public InventoryScreenMixin(PlayerScreenHandler screenHandler, PlayerInventory playerInventory, Text text) {
        super(screenHandler, playerInventory, text);
    }
    //@Inject(method = "drawEntity", at = @At("HEAD"), cancellable = true)
    private static void drawDrider(int x, int y, int size, float mouseX, float mouseY, LivingEntity entity, CallbackInfo ci) {
        if(entity instanceof PlayerEntity player) {
            DriderEntity driderEntity = getDrider(player);
            if (driderEntity != null) {
                float f = (float) Math.atan(mouseX / 40.0f);
                float g = (float) Math.atan(mouseY / 40.0f);
                MatrixStack matrixStack = RenderSystem.getModelViewStack();
                matrixStack.push();
                matrixStack.translate(x, y, 1050.0);
                matrixStack.scale(1.0f, 1.0f, -1.0f);
                RenderSystem.applyModelViewMatrix();
                MatrixStack matrixStack2 = new MatrixStack();
                matrixStack2.translate(0.0, 0.0, 1000.0);
                matrixStack2.scale(size, size, size);
                Quaternion quaternion = Vec3f.POSITIVE_Z.getDegreesQuaternion(180.0f);
                Quaternion quaternion2 = Vec3f.POSITIVE_X.getDegreesQuaternion(g * 20.0f);
                quaternion.hamiltonProduct(quaternion2);
                matrixStack2.multiply(quaternion);
                float h = driderEntity.bodyYaw;
                float i = driderEntity.getYaw();
                float j = driderEntity.getPitch();
                float k = driderEntity.prevHeadYaw;
                float l = entity.headYaw;
                driderEntity.bodyYaw = 180.0f + f * 20.0f;
                driderEntity.setYaw(180.0f + f * 40.0f);
                driderEntity.setPitch(-g * 20.0f);
                driderEntity.headYaw = driderEntity.getYaw();
                driderEntity.prevHeadYaw = driderEntity.getYaw();
                driderEntity.setStackInHand(Hand.MAIN_HAND, player.getMainHandStack());
                driderEntity.setStackInHand(Hand.OFF_HAND, player.getOffHandStack());
                driderEntity.setCurrentHand(player.getActiveHand() == null ? Hand.MAIN_HAND : player.getActiveHand());
                //blursed
                driderEntity.equipStack(EquipmentSlot.HEAD, player.getEquippedStack(EquipmentSlot.HEAD));
                driderEntity.equipStack(EquipmentSlot.CHEST, player.getEquippedStack(EquipmentSlot.CHEST));
                driderEntity.equipStack(EquipmentSlot.LEGS, player.getEquippedStack(EquipmentSlot.LEGS));
                driderEntity.equipStack(EquipmentSlot.FEET, player.getEquippedStack(EquipmentSlot.FEET));
                driderEntity.setSneaking(player.isSneaking());
                driderEntity.setPose(player.getPose());
                DiffuseLighting.method_34742();
                EntityRenderDispatcher entityRenderDispatcher = MinecraftClient.getInstance().getEntityRenderDispatcher();
                quaternion2.conjugate();
                entityRenderDispatcher.setRotation(quaternion2);
                entityRenderDispatcher.setRenderShadows(false);
                VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
                RenderSystem.runAsFancy(() -> entityRenderDispatcher.render((LivingEntity) driderEntity, 0.0, 0.0, 0.0, 0.0f, 1.0f, matrixStack2, immediate, 0xF000F0));
                immediate.draw();
                entityRenderDispatcher.setRenderShadows(true);
                driderEntity.bodyYaw = h;
                driderEntity.setYaw(i);
                driderEntity.setPitch(j);
                driderEntity.prevHeadYaw = k;
                driderEntity.headYaw = l;
                matrixStack.pop();
                RenderSystem.applyModelViewMatrix();
                DiffuseLighting.enableGuiDepthLighting();
                ci.cancel();
            }
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
