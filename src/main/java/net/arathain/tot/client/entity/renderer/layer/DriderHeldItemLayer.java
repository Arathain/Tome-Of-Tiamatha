package net.arathain.tot.client.entity.renderer.layer;

import net.arathain.tot.common.entity.living.drider.DriderEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.Vec3f;
import software.bernie.geckolib3.renderers.geo.GeoLayerRenderer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

public class DriderHeldItemLayer extends GeoLayerRenderer<DriderEntity> {
    private final IGeoRenderer<DriderEntity> renderer;
    public DriderHeldItemLayer(IGeoRenderer<DriderEntity> entityRendererIn) {
        super(entityRendererIn);
        renderer = entityRendererIn;
    }



    @Override
    public void render(MatrixStack stack, VertexConsumerProvider bufferIn, int packedLightIn, DriderEntity driderEntity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        var rightBone = renderer.getGeoModelProvider().getModel(this.getEntityModel().getModelLocation(driderEntity)).getBone("rightItem").get();
        var leftBone = renderer.getGeoModelProvider().getModel(this.getEntityModel().getModelLocation(driderEntity)).getBone("leftItem").get();

        if (rightBone.getName().equals("rightItem") && !driderEntity.getMainHandStack().isEmpty()) {
            if(driderEntity.getMainHandStack().getUseAction() == UseAction.BLOCK) {
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
                MinecraftClient.getInstance().getItemRenderer().renderItem(driderEntity.getMainHandStack(), ModelTransformation.Mode.THIRD_PERSON_RIGHT_HAND, packedLightIn, OverlayTexture.DEFAULT_UV, stack, bufferIn, 0);
                stack.pop();
            } else {
                stack.push();
                stack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(270));
                stack.scale(0.68f, 0.68f, 0.68f);
                stack.translate(0.35,0.5,0.9f);
                MinecraftClient.getInstance().getItemRenderer().renderItem(driderEntity.getMainHandStack(), ModelTransformation.Mode.THIRD_PERSON_RIGHT_HAND, packedLightIn, OverlayTexture.DEFAULT_UV, stack, bufferIn, 0);
                stack.pop();
            }

        }
        if (leftBone.getName().equals("leftItem")) {
            if(!driderEntity.getOffHandStack().isEmpty() && driderEntity.getOffHandStack().getUseAction() == UseAction.BLOCK) {
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
                MinecraftClient.getInstance().getItemRenderer().renderItem(driderEntity.getOffHandStack(), ModelTransformation.Mode.THIRD_PERSON_LEFT_HAND, packedLightIn, OverlayTexture.DEFAULT_UV, stack, bufferIn, 0);
                stack.pop();
            } else {
                stack.push();
                stack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(160));
                stack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180));
                stack.scale(0.68f, 0.68f, 0.68f);
                stack.translate(0.32, -1.4, -0.4f);
                MinecraftClient.getInstance().getItemRenderer().renderItem(driderEntity.getOffHandStack(), ModelTransformation.Mode.THIRD_PERSON_LEFT_HAND, packedLightIn, OverlayTexture.DEFAULT_UV, stack, bufferIn, 0);
                stack.pop();
            }
        }
    }
}
