package net.arathain.tot.client.entity.renderer.drider.weaver;

import net.arathain.tot.client.entity.model.drider.weaver.WeaverEntityModel;
import net.arathain.tot.common.entity.living.drider.weaver.WeaverEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3q.renderers.geo.GeoEntityRenderer;

public class WeaverEntityRenderer extends GeoEntityRenderer<WeaverEntity> {
    public WeaverEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new WeaverEntityModel());
    }
    @Override
    public RenderLayer getRenderType(WeaverEntity animatable, float partialTicks, MatrixStack stack, VertexConsumerProvider renderTypeBuffer, VertexConsumer vertexBuilder, int packedLightIn, Identifier textureLocation) {
        return RenderLayer.getEntityTranslucent(getTextureLocation(animatable));
    }
}
