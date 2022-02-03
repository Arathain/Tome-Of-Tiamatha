package net.arathain.tot.client.entity.renderer.layer;

import net.arathain.tot.TomeOfTiamatha;
import net.arathain.tot.common.entity.living.drider.weavekin.WeavechildEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import software.bernie.geckolib3.renderers.geo.GeoLayerRenderer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

public class WeavechildEyeLayer extends GeoLayerRenderer<WeavechildEntity> {
    public WeavechildEyeLayer(IGeoRenderer<WeavechildEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(MatrixStack matrixStackIn, VertexConsumerProvider bufferIn, int packedLightIn, WeavechildEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
            Identifier location = new Identifier(TomeOfTiamatha.MODID, "textures/entity/weavekin/weavechild_emissive.png");
            RenderLayer armor = RenderLayer.getEyes(location);
            this.getRenderer().render(this.getEntityModel().getModel(this.getEntityModel().getModelLocation(entitylivingbaseIn)), entitylivingbaseIn, partialTicks, armor, matrixStackIn, bufferIn, bufferIn.getBuffer(armor), -packedLightIn, OverlayTexture.DEFAULT_UV, 1.0f, 1.0f, 1.0f, (MathHelper.clamp(120f - packedLightIn, 0, 120f) / 160f));
    }
}
