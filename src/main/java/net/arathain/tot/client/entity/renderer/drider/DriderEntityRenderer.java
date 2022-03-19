package net.arathain.tot.client.entity.renderer.drider;

import net.arathain.tot.client.entity.model.drider.DriderEntityModel;
import net.arathain.tot.client.entity.renderer.layer.DriderChestplateLayer;
import net.arathain.tot.client.entity.renderer.layer.DriderEyeLayer;
import net.arathain.tot.client.entity.renderer.layer.DriderHelmetLayer;
import net.arathain.tot.client.entity.renderer.layer.ScuffedFixLayer;
import net.arathain.tot.common.entity.living.drider.DriderEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.Vec3f;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class DriderEntityRenderer extends GeoEntityRenderer<DriderEntity> {
    private DriderEntity driderEntity;

    public DriderEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new DriderEntityModel());
        this.addLayer(new DriderHelmetLayer(this));
        this.addLayer(new DriderChestplateLayer(this));
        this.addLayer(new ScuffedFixLayer(this));
        this.addLayer(new DriderEyeLayer(this));
    }

    @Override
    public void renderEarly(DriderEntity driderEntity, MatrixStack stackIn, float ticks, VertexConsumerProvider vertexConsumerProvider, VertexConsumer vertexBuilder, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float partialTicks) {
        this.driderEntity = driderEntity;
        super.renderEarly(driderEntity, stackIn, ticks, vertexConsumerProvider, vertexBuilder, packedLightIn, packedOverlayIn, red, green, blue, partialTicks);
    }

    @Override
    public void renderRecursively(GeoBone bone, MatrixStack stack, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        if (bone.getName().equals("rightItem") && !mainHand.isEmpty()) {
            if(mainHand.getUseAction() == UseAction.BLOCK) {
                stack.push();
                stack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(270));
                if(driderEntity.isBlocking() && driderEntity.getItemUseTimeLeft() > 0) {
                    stack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(10));
                    stack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(20));
                    stack.translate(-0.15,0.2,-0.2f);
                } else {
                    stack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(0));
                    stack.translate(0,0,0);
                    stack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(0));
                }
                stack.scale(0.68f, 0.68f, 0.68f);
                stack.translate(0.35,0.5,1.0f);
                MinecraftClient.getInstance().getItemRenderer().renderItem(mainHand, ModelTransformation.Mode.THIRD_PERSON_RIGHT_HAND, packedLightIn, packedOverlayIn, stack, rtb, 0);
                stack.pop();
                bufferIn = rtb.getBuffer(RenderLayer.getEntityTranslucent(whTexture));
            } else {
                stack.push();
                stack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(270));
                stack.scale(0.68f, 0.68f, 0.68f);
                stack.translate(0.35,0.5,0.9f);
                MinecraftClient.getInstance().getItemRenderer().renderItem(mainHand, ModelTransformation.Mode.THIRD_PERSON_RIGHT_HAND, packedLightIn, packedOverlayIn, stack, rtb, 0);
                stack.pop();
                bufferIn = rtb.getBuffer(RenderLayer.getEntityTranslucent(whTexture));
            }

        }
        if (bone.getName().equals("leftItem")) {
            if(!offHand.isEmpty() && offHand.getUseAction() == UseAction.BLOCK) {
                stack.push();
                stack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(270));
                stack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180));
                if(driderEntity.isBlocking() && driderEntity.getItemUseTimeLeft() > 0) {
                    stack.multiply(Vec3f.NEGATIVE_Y.getDegreesQuaternion(10));
                    stack.multiply(Vec3f.NEGATIVE_X.getDegreesQuaternion(20));
                    stack.translate(-0.15,0.2,0.2f);
                } else {
                    stack.multiply(Vec3f.NEGATIVE_Y.getDegreesQuaternion(0));
                    stack.translate(0,0,0);
                    stack.multiply(Vec3f.NEGATIVE_X.getDegreesQuaternion(0));
                }
                stack.scale(0.68f, 0.68f, 0.68f);
                stack.translate(0.35, 0.5, -1.8f);
                MinecraftClient.getInstance().getItemRenderer().renderItem(offHand, ModelTransformation.Mode.THIRD_PERSON_LEFT_HAND, packedLightIn, packedOverlayIn, stack, rtb, 0);
                stack.pop();
                bufferIn = rtb.getBuffer(RenderLayer.getEntityTranslucent(whTexture));
            } else {
                stack.push();
                stack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(160));
                stack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180));
                stack.scale(0.68f, 0.68f, 0.68f);
                stack.translate(0.32, -1.4, -0.4f);
                MinecraftClient.getInstance().getItemRenderer().renderItem(offHand, ModelTransformation.Mode.THIRD_PERSON_LEFT_HAND, packedLightIn, packedOverlayIn, stack, rtb, 0);
                stack.pop();
                bufferIn = rtb.getBuffer(RenderLayer.getEntityTranslucent(whTexture));
            }
        }
        super.renderRecursively(bone, stack, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    }

    @Override
    public RenderLayer getRenderType(DriderEntity animatable, float partialTicks, MatrixStack stack, VertexConsumerProvider renderTypeBuffer, VertexConsumer vertexBuilder, int packedLightIn, Identifier textureLocation) {
        return RenderLayer.getEntityTranslucent(this.getTextureLocation(animatable));
    }

}
