package net.arathain.tot.client.entity.renderer.layer;

import net.arathain.tot.TomeOfTiamatha;
import net.arathain.tot.common.entity.DriderEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.DyeableArmorItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.renderers.geo.GeoLayerRenderer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

public class ScuffedFixLayer extends GeoLayerRenderer<DriderEntity> {

    public ScuffedFixLayer(IGeoRenderer<DriderEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(MatrixStack matrixStackIn, VertexConsumerProvider bufferIn, int packedLightIn, DriderEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if(!entitylivingbaseIn.getEquippedStack(EquipmentSlot.CHEST).isEmpty()) {
            Identifier location = new Identifier(TomeOfTiamatha.MODID, "textures/entity/drider/drider_" + entitylivingbaseIn.getDriderType().toString().toLowerCase() + ".png");
            RenderLayer armor = RenderLayer.getEntityCutoutNoCull(location);
            this.getRenderer().render(this.getEntityModel().getModel(this.getEntityModel().getModelLocation(entitylivingbaseIn)), entitylivingbaseIn, partialTicks, armor, matrixStackIn, bufferIn, bufferIn.getBuffer(armor), packedLightIn, OverlayTexture.DEFAULT_UV, 1.0f, 1.0f, 1.0f, 1.0f);
        }
    }
}