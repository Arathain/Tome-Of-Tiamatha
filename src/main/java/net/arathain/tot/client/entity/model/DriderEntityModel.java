package net.arathain.tot.client.entity.model;

import net.arathain.tot.TomeOfTiamatha;
import net.arathain.tot.common.entity.DriderEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.model.provider.data.EntityModelData;

public class DriderEntityModel extends AnimatedGeoModel<DriderEntity> {
    @Override
    public Identifier getModelLocation(DriderEntity object) {
        return new Identifier(TomeOfTiamatha.MODID, "geo/entity/drider.geo.json");
    }

    @Override
    public Identifier getTextureLocation(DriderEntity object) {
        return new Identifier(TomeOfTiamatha.MODID, "textures/entity/drider/drider_" + object.getDriderType().toString().toLowerCase() + ".png");
    }

    @Override
    public Identifier getAnimationFileLocation(DriderEntity animatable) {
        return new Identifier(TomeOfTiamatha.MODID, "animations/entity/drider.geo.json");
    }

    @Override
    public void setLivingAnimations(DriderEntity entity, Integer uniqueID, AnimationEvent customPredicate) {
        super.setLivingAnimations(entity, uniqueID, customPredicate);
        IBone head = this.getAnimationProcessor().getBone("head");
        IBone headDress = this.getAnimationProcessor().getBone("headdress");
        IBone armor = this.getAnimationProcessor().getBone("armor");
        IBone glove1 = this.getAnimationProcessor().getBone("glove1");
        IBone glove2 = this.getAnimationProcessor().getBone("glove2");
        EntityModelData extraData = (EntityModelData) customPredicate.getExtraDataOfType(EntityModelData.class).get(0);
        IBone leftarm = this.getAnimationProcessor().getBone("leftArm");
        IBone rightarm = this.getAnimationProcessor().getBone("rightArm");
        headDress.setHidden(entity.getEquippedStack(EquipmentSlot.HEAD).isEmpty());
        armor.setHidden(entity.getEquippedStack(EquipmentSlot.CHEST).isEmpty());
        glove2.setHidden(entity.getEquippedStack(EquipmentSlot.CHEST).isEmpty());
        glove1.setHidden(entity.getEquippedStack(EquipmentSlot.CHEST).isEmpty());

        if (head != null) {
            head.setRotationX(extraData.headPitch * ((float) Math.PI / 180F));
            head.setRotationY(extraData.netHeadYaw * ((float) Math.PI / 180F));
        }
        if (leftarm != null) {
            leftarm.setRotationX(Vec3f.POSITIVE_X.getRadialQuaternion((float) (MathHelper.cos(entity.limbAngle * 0.6662F) * 2.0F * entity.limbDistance * 0.5F)).getX());
        }
        if (rightarm != null) {
            rightarm.setRotationX(Vec3f.POSITIVE_X.getRadialQuaternion((float) (MathHelper.cos(entity.limbAngle * 0.6662F + 3.1415927F) * 2.0F * entity.limbDistance * 0.5F + (MathHelper.lerp(MathHelper.clamp(customPredicate.animationTick, 0, 1), entity.lastHandSwingProgress * 8, entity.handSwingProgress * 8)))).getX());
            rightarm.setRotationZ((rightarm.getRotationZ() + (entity.handSwingProgress * 0.5f)));
        }
    }


}
