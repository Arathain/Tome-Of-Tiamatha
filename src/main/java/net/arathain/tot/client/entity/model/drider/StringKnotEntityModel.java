package net.arathain.tot.client.entity.model.drider;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.entity.Entity;

/**
 * Model for the {@link net.arathain.tot.common.entity.string.StringKnotEntity}.
 * Similar to the {@link net.minecraft.client.render.entity.model.LeashKnotEntityModel} code.
 *
 * @see net.minecraft.client.render.entity.LeashKnotEntityRenderer
 * @author legoatoom
 */
@Environment(EnvType.CLIENT)
public class StringKnotEntityModel<T extends Entity> extends SinglePartEntityModel<T> {
    private final ModelPart chainKnot;
    private final ModelPart root;

    public StringKnotEntityModel(ModelPart root) {
        this.root = root;
        this.chainKnot = root.getChild("knot");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        modelPartData.addChild("knot", ModelPartBuilder.create().uv(0, 0).cuboid(-3.0F, -8.0F, -3.0F, 6.0F, 8.0F, 6.0F), ModelTransform.NONE);
        return TexturedModelData.of(modelData, 32, 32);
    }

    public void setAngles(T entity, float limbAngle, float limbDistance, float customAngle, float headYaw, float headPitch) {
        this.chainKnot.yaw = headYaw * 0.017453292F;
        this.chainKnot.pitch = headPitch * 0.017453292F;
    }

    @Override
    public ModelPart getPart() {
        return chainKnot;
    }
}
