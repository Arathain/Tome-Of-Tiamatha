package net.arathain.tot.client.entity.model;

import net.arathain.tot.TomeOfTiamatha;
import net.arathain.tot.common.entity.DriderEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.util.Identifier;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.model.AnimatedTickingGeoModel;
import software.bernie.geckolib3.model.provider.data.EntityModelData;

public class DriderEntityModel extends AnimatedTickingGeoModel<DriderEntity> {
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
        return new Identifier(TomeOfTiamatha.MODID, "animations/entity/drider.animation.json");
    }

    @Override
    public void codeAnimations(DriderEntity entity, Integer uniqueID, AnimationEvent customPredicate) {
        super.codeAnimations(entity, uniqueID, customPredicate);
        IBone head = this.getAnimationProcessor().getBone("head");
        IBone torso = this.getAnimationProcessor().getBone("torso");
        EntityModelData extraData = (EntityModelData) customPredicate.getExtraDataOfType(EntityModelData.class).get(0);
        IBone leftArm = this.getAnimationProcessor().getBone("leftarm");
        IBone leftItem = this.getAnimationProcessor().getBone("leftItem");
        IBone sendHelp = this.getAnimationProcessor().getBone("rightarm");
        IBone leftGlove = this.getAnimationProcessor().getBone("glove2");
        leftGlove.setHidden(entity.getEquippedStack(EquipmentSlot.CHEST).isEmpty());

        if (head != null) {
            head.setRotationX(extraData.headPitch * (float) Math.PI / 180F);
            head.setRotationY(extraData.netHeadYaw * ((float) Math.PI / 180F));
        }
        if (torso != null) {
            torso.setRotationY((float) MathHelper.clamp(extraData.netHeadYaw * ((float) Math.PI / 180F), -0.4, 0.4));
            if(head != null)
            head.setRotationY(head.getRotationY() - torso.getRotationY());
        }
        if(leftArm != null) {
            leftArm.setRotationX(leftArm.getRotationX());
            leftArm.setRotationY(leftArm.getRotationY());
            if(entity.isBlocking() && entity.getItemUseTimeLeft() > 0 && entity.getOffHandStack().getUseAction() == UseAction.BLOCK && !(entity.getMainHandStack().getUseAction() == UseAction.BLOCK)){
                leftArm.setRotationX(leftArm.getRotationX() + 1.3f);
                leftArm.setRotationY(leftArm.getRotationY() - 1);
            }
        }

        if (sendHelp != null) {
            if(entity.isBlocking() && entity.getItemUseTimeLeft() > 0 && entity.getMainHandStack().getUseAction() == UseAction.BLOCK){
                sendHelp.setRotationX(sendHelp.getRotationX() + 1.3f);
                sendHelp.setRotationY(sendHelp.getRotationY() + 1);
            } else {
                sendHelp.setRotationX(Vec3f.POSITIVE_X.getRadialQuaternion((float) (MathHelper.cos(entity.limbAngle * 0.6662F + 3.1415927F) * 2.0F * entity.limbDistance * 0.5F + (MathHelper.lerp(MathHelper.clamp(customPredicate.animationTick, 0, 1), entity.lastHandSwingProgress * 8, entity.handSwingProgress * 8)))).getX());
                sendHelp.setRotationZ((sendHelp.getRotationZ() + (entity.handSwingProgress * 0.5f)));
            }
        }
    }


}
