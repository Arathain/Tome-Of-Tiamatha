package net.arathain.tot.client.entity.model.drider;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.arathain.tot.common.entity.living.drider.arachne.DriderDenDoorEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

public class DriderDenDoorModel extends SinglePartEntityModel<DriderDenDoorEntity> {
    private final ModelPart bb_main;

    public DriderDenDoorModel(ModelPart root) {
        this.bb_main = root.getChild("bb_main");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData meshdefinition = new ModelData();
        ModelPartData partdefinition = meshdefinition.getRoot();

        ModelPartData bb_main = partdefinition.addChild("bb_main", ModelPartBuilder.create().uv(0, 0).cuboid(-24.0F, -48.0F, -24.0F, 48.0F, 48.0F, 48.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

        return TexturedModelData.of(meshdefinition, 256, 256);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        bb_main.render(matrices, vertices, light, overlay, red, green, blue, alpha);
    }

    @Override
    public ModelPart getPart() {
        return bb_main;
    }

    @Override
    public void setAngles(DriderDenDoorEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {

    }
}
