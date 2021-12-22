package net.arathain.tot.client.entity.renderer;

import net.arathain.tot.client.entity.model.DriderEntityModel;
import net.arathain.tot.client.entity.renderer.layer.DriderChestplateLayer;
import net.arathain.tot.client.entity.renderer.layer.DriderHelmetLayer;
import net.arathain.tot.common.entity.DriderEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class DriderEntityRenderer extends GeoEntityRenderer<DriderEntity> {
    private ItemStack mainStack;
    private ItemStack offStack;
    private VertexConsumerProvider vertexConsumerProvider;
    public DriderEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new DriderEntityModel());
        this.addLayer(new DriderHelmetLayer(this));
        this.addLayer(new DriderChestplateLayer(this));
    }

    @Override
    public void renderEarly(DriderEntity driderEntity, MatrixStack stackIn, float ticks, VertexConsumerProvider vertexConsumerProvider, VertexConsumer vertexBuilder, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float partialTicks) {
        this.mainStack = driderEntity.getEquippedStack(EquipmentSlot.MAINHAND);
        this.offStack = driderEntity.getEquippedStack(EquipmentSlot.OFFHAND);
        this.vertexConsumerProvider = vertexConsumerProvider;

        super.renderEarly(driderEntity, stackIn, ticks, vertexConsumerProvider, vertexBuilder, packedLightIn, packedOverlayIn, red, green, blue, partialTicks);
    }

    @Override
    public void renderRecursively(GeoBone bone, MatrixStack stack, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        if (bone.getName().equals("rightItem")) {
            stack.push();
            stack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(270));
            stack.scale(0.6f, 0.6f, 0.6f);
            stack.translate(0.35,0.6,1.1f);
            MinecraftClient.getInstance().getItemRenderer().renderItem(mainStack, ModelTransformation.Mode.THIRD_PERSON_RIGHT_HAND, packedLightIn, packedOverlayIn, stack, this.vertexConsumerProvider, 0);
            stack.pop();
            bufferIn = rtb.getBuffer(RenderLayer.getEntityTranslucent(whTexture));
        }
        if (bone.getName().equals("leftItem")) {
            stack.push();
            stack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(270));
            stack.scale(0.6f, 0.6f, 0.6f);
            stack.translate(-0.35,0.6,1.1f);
            MinecraftClient.getInstance().getItemRenderer().renderItem(offStack, ModelTransformation.Mode.THIRD_PERSON_RIGHT_HAND, packedLightIn, packedOverlayIn, stack, this.vertexConsumerProvider, 0);
            stack.pop();
            bufferIn = rtb.getBuffer(RenderLayer.getEntityTranslucent(whTexture));
        }
        super.renderRecursively(bone, stack, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    }

    @Override
    public RenderLayer getRenderType(DriderEntity animatable, float partialTicks, MatrixStack stack, VertexConsumerProvider renderTypeBuffer, VertexConsumer vertexBuilder, int packedLightIn, Identifier textureLocation) {
        return RenderLayer.getEntityTranslucent(getTextureLocation(animatable));
    }

}
