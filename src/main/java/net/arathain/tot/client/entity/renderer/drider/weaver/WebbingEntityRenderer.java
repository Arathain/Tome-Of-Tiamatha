package net.arathain.tot.client.entity.renderer.drider.weaver;

import net.arathain.tot.TomeOfTiamatha;
import net.arathain.tot.client.TomeOfTiamathaClient;
import net.arathain.tot.client.entity.model.drider.weaver.WebbingEntityModel;
import net.arathain.tot.common.entity.living.drider.weaver.WebbingEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

@Environment(value = EnvType.CLIENT)
public class WebbingEntityRenderer extends EntityRenderer<WebbingEntity> {
    public static final Identifier TEXTURE = new Identifier(TomeOfTiamatha.MODID, "textures/entity/drider/weaver/webbing.png");
    public WebbingEntityModel model;

    public WebbingEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.shadowRadius = 0.4f;
        this.model = new WebbingEntityModel(context.getPart(TomeOfTiamathaClient.WEBBING_MODEL_LAYER));
    }
    @Override
    public void render(WebbingEntity webbing, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        matrixStack.push();
        matrixStack.translate(0.0, 0.375, 0.0);

        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(this.model.getLayer(TEXTURE));
        this.model.render(matrixStack, vertexConsumer, i, OverlayTexture.DEFAULT_UV, 1.0f, 1.0f, 1.0f, 1.0f);
        matrixStack.pop();
        super.render(webbing, f, g, matrixStack, vertexConsumerProvider, i);
    }

    @Override
    public Identifier getTexture(WebbingEntity entity) {
        return TEXTURE;
    }
}

