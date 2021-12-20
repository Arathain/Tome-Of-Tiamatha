package net.arathain.tot.client.entity.renderer;

import net.arathain.tot.client.entity.model.DriderEntityModel;
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
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class DriderEntityRenderer extends GeoEntityRenderer<DriderEntity> {
    private ItemStack mainStack;
    private ItemStack offStack;
    private VertexConsumerProvider vertexConsumerProvider;
    public DriderEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new DriderEntityModel());
    }

    @Override
    public void renderEarly(DriderEntity ratEntity, MatrixStack stackIn, float ticks, VertexConsumerProvider vertexConsumerProvider, VertexConsumer vertexBuilder, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float partialTicks) {
        this.mainStack = ratEntity.getEquippedStack(EquipmentSlot.MAINHAND);
        this.offStack = ratEntity.getEquippedStack(EquipmentSlot.OFFHAND);
        this.vertexConsumerProvider = vertexConsumerProvider;

        super.renderEarly(ratEntity, stackIn, ticks, vertexConsumerProvider, vertexBuilder, packedLightIn, packedOverlayIn, red,
                green, blue, partialTicks);
    }

    @Override
    public void renderRecursively(GeoBone bone, MatrixStack stack, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        if (bone.getName().equals("rightItem")) {
            stack.push();
            stack.scale(1.0f, 1.0f, 1.0f);
            stack.translate(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());
            MinecraftClient.getInstance().getItemRenderer().renderItem(mainStack, ModelTransformation.Mode.THIRD_PERSON_RIGHT_HAND, packedLightIn, packedOverlayIn, stack, this.vertexConsumerProvider, 0);
            stack.pop();
            bufferIn = rtb.getBuffer(RenderLayer.getEntityTranslucent(whTexture));
        }
        if (bone.getName().equals("leftItem")) {
            stack.push();
            stack.scale(1.0f, 1.0f, 1.0f);
            stack.translate(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());
            MinecraftClient.getInstance().getItemRenderer().renderItem(offStack, ModelTransformation.Mode.THIRD_PERSON_LEFT_HAND, packedLightIn, packedOverlayIn, stack, this.vertexConsumerProvider, 0);
            stack.pop();
            bufferIn = rtb.getBuffer(RenderLayer.getEntityTranslucent(whTexture));
        }
        super.renderRecursively(bone, stack, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    }

}
