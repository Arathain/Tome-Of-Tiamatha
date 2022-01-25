package net.arathain.tot.client.entity.renderer.layer;

import net.arathain.tot.TomeOfTiamatha;
import net.arathain.tot.common.entity.living.drider.DriderEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.DyeableArmorItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.renderers.geo.GeoLayerRenderer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

public class DriderHelmetLayer extends GeoLayerRenderer<DriderEntity> {

    public DriderHelmetLayer(IGeoRenderer<DriderEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(MatrixStack matrixStackIn, VertexConsumerProvider bufferIn, int packedLightIn, DriderEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if(!entitylivingbaseIn.getEquippedStack(EquipmentSlot.HEAD).isEmpty()) {
            Identifier location = new Identifier(TomeOfTiamatha.MODID, "textures/entity/drider/armor/" + entitylivingbaseIn.getEquippedStack(EquipmentSlot.HEAD).getItem().toString() + ".png");
            RenderLayer armor = RenderLayer.getEntityTranslucent(location);
            if (entitylivingbaseIn.getEquippedStack(EquipmentSlot.HEAD).getItem() instanceof DyeableArmorItem) {
                int i = ((DyeableArmorItem)entitylivingbaseIn.getEquippedStack(EquipmentSlot.HEAD).getItem()).getColor((entitylivingbaseIn.getEquippedStack(EquipmentSlot.HEAD)));
                float f = (float)(i >> 16 & 0xFF) / 255.0F;
                float g = (float)(i >> 8 & 0xFF) / 255.0F;
                float h = (float)(i & 0xFF) / 255.0F;
                this.getRenderer().render(this.getEntityModel().getModel(this.getEntityModel().getModelLocation(entitylivingbaseIn)), entitylivingbaseIn, partialTicks, armor, matrixStackIn, bufferIn, bufferIn.getBuffer(armor), packedLightIn, OverlayTexture.DEFAULT_UV, f, g, h, 1.0f);
            }
        else {
                this.getRenderer().render(this.getEntityModel().getModel(this.getEntityModel().getModelLocation(entitylivingbaseIn)), entitylivingbaseIn, partialTicks, armor, matrixStackIn, bufferIn, bufferIn.getBuffer(armor), packedLightIn, OverlayTexture.DEFAULT_UV, 1.0f, 1.0f, 1.0f, 1.0f);
            }
        }
    }
}
