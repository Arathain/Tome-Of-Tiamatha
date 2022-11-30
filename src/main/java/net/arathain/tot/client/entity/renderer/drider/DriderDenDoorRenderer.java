package net.arathain.tot.client.entity.renderer.drider;

import net.arathain.tot.TomeOfTiamatha;
import net.arathain.tot.client.TomeOfTiamathaClient;
import net.arathain.tot.client.entity.model.drider.DriderDenDoorModel;
import net.arathain.tot.common.entity.living.drider.arachne.DriderDenDoorEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;

@Environment(value = EnvType.CLIENT)
public class DriderDenDoorRenderer extends EntityRenderer<DriderDenDoorEntity> {
    public static final Identifier TEXTURE = new Identifier(TomeOfTiamatha.MODID, "textures/entity/drider/arachne/door.png");
    public DriderDenDoorModel model;

    public DriderDenDoorRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.shadowRadius = 0.4f;
        this.model = new DriderDenDoorModel(context.getPart(TomeOfTiamathaClient.DEN_DOOR_MODEL_LAYER));
    }
    @Override
    public void render(DriderDenDoorEntity webbing, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        matrixStack.push();
        matrixStack.translate(0.0, 1.5f, 0.0);
        matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(180f));

        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(this.model.getLayer(TEXTURE));
        this.model.render(matrixStack, vertexConsumer, i, OverlayTexture.DEFAULT_UV, 1.0f, 1.0f, 1.0f, 1.0f);
        matrixStack.pop();
        super.render(webbing, f, g, matrixStack, vertexConsumerProvider, i);
    }

    @Override
    public Identifier getTexture(DriderDenDoorEntity entity) {
        return TEXTURE;
    }
}
