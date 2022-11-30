package net.arathain.tot.client.entity.model.drider.weaver;

import net.arathain.tot.common.entity.living.drider.weaver.WebbingEntity;
import net.minecraft.client.model.*;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.client.util.math.MatrixStack;

public class WebbingEntityModel extends SinglePartEntityModel<WebbingEntity> {
    private final ModelPart webbing;

    public WebbingEntityModel(ModelPart root) {
        this.webbing = root.getChild("webbing");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData meshdefinition = new ModelData();
        ModelPartData partdefinition = meshdefinition.getRoot();

        ModelPartData bb_main = partdefinition.addChild("webbing", ModelPartBuilder.create().uv(0, 0).cuboid(-7.5F, -31.0F, -7.5F, 15.0F, 31.0F, 15.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

        return TexturedModelData.of(meshdefinition, 64, 64);
    }

    @Override
    public void setAngles(WebbingEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {

    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        this.webbing.render(matrices, vertices, light, overlay, red, green, blue, alpha);
    }

    @Override
    public ModelPart getPart() {
        return this.webbing;
    }
}
