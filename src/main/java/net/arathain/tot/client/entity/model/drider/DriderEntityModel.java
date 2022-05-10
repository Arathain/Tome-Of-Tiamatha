package net.arathain.tot.client.entity.model.drider;

import net.arathain.tot.TomeOfTiamatha;
import net.arathain.tot.common.entity.living.drider.DriderEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.PillagerEntityRenderer;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.CrossbowItem;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.UseAction;
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
        return new Identifier(TomeOfTiamatha.MODID, "animations/entity/drider.animation.json");
    }


    @Override
    public void setLivingAnimations(DriderEntity entity, Integer uniqueID, AnimationEvent customPredicate) {
        super.setLivingAnimations(entity, uniqueID, customPredicate);
        IBone head = this.getAnimationProcessor().getBone("head");
        IBone torso = this.getAnimationProcessor().getBone("torso");
        EntityModelData extraData = (EntityModelData) customPredicate.getExtraDataOfType(EntityModelData.class).get(0);
        IBone leftArm = this.getAnimationProcessor().getBone("leftarm");
        IBone sendHelp = this.getAnimationProcessor().getBone("rightarm");

        if (head != null) {
            head.setRotationX(head.getRotationX() + (MinecraftClient.getInstance().isPaused() ? 0 :(extraData.headPitch * (float) Math.PI / 180F)));
            head.setRotationY(extraData.netHeadYaw * ((float) Math.PI / 180F));
        }
        if (torso != null) {
            if(entity.getItemUseTimeLeft() > 0 && (entity.getMainHandStack().getUseAction() == UseAction.BOW || entity.getOffHandStack().getUseAction() == UseAction.BOW) || (entity.getMainHandStack().getUseAction() == UseAction.CROSSBOW || entity.getOffHandStack().getUseAction() == UseAction.CROSSBOW)) {
                torso.setRotationY(torso.getRotationY() + extraData.netHeadYaw * ((float) Math.PI / 180F));
            }
            if(head != null)
                head.setRotationY(head.getRotationY() - torso.getRotationY());
        }
        if(leftArm != null) {
            leftArm.setRotationX(leftArm.getRotationX());
            leftArm.setRotationY(leftArm.getRotationY());
            if(entity.isBlocking() && entity.getItemUseTimeLeft() > 0 && entity.getOffHandStack().getUseAction() == UseAction.BLOCK && !(entity.getMainHandStack().getUseAction() == UseAction.BLOCK)){
                leftArm.setRotationX(leftArm.getRotationX() + (MinecraftClient.getInstance().isPaused() ? 0 : 1.3f));
                leftArm.setRotationY(leftArm.getRotationY() - (MinecraftClient.getInstance().isPaused() ? 0 : 1));
            } else if(entity.getItemUseTimeLeft() > 0 && (entity.getMainHandStack().getUseAction() == UseAction.BOW || entity.getOffHandStack().getUseAction() == UseAction.BOW)) {
                leftArm.setRotationX(leftArm.getRotationX() + (MinecraftClient.getInstance().isPaused() ? 0 : 1.3f));
                leftArm.setRotationX(leftArm.getRotationX() + (MinecraftClient.getInstance().isPaused() ? 0 :(extraData.headPitch * (float) Math.PI / 180F)));
            } else if((entity.getMainHandStack().getUseAction() == UseAction.CROSSBOW || entity.getOffHandStack().getUseAction() == UseAction.CROSSBOW)) {
                leftArm.setRotationX(leftArm.getRotationX() + (MinecraftClient.getInstance().isPaused() ? 0 : 1.3f));
                leftArm.setRotationX(leftArm.getRotationX() + (MinecraftClient.getInstance().isPaused() ? 0 :(extraData.headPitch * (float) Math.PI / 180F)));
            } else {
                leftArm.setRotationX(Vec3f.POSITIVE_X.getRadialQuaternion((float) (MathHelper.cos(entity.limbAngle * 0.6662F + 3.1415927F) * 2.0F * entity.limbDistance * 0.5F + ((entity.getActiveHand() == Hand.OFF_HAND) ? (MathHelper.lerp(MathHelper.clamp(customPredicate.animationTick, 0, 1), entity.lastHandSwingProgress * 8, entity.handSwingProgress * 8)) : 0))).getX());
                leftArm.setRotationZ((float) (leftArm.getRotationZ() - ((entity.getActiveHand() == Hand.OFF_HAND) ? (MathHelper.lerp(MathHelper.clamp(customPredicate.animationTick, 0, 1), entity.lastHandSwingProgress * 0.8f, entity.handSwingProgress * 0.8f)) : 0)));
            }
        }

        if (sendHelp != null) {
            if(entity.isBlocking() && entity.getItemUseTimeLeft() > 0 && entity.getMainHandStack().getUseAction() == UseAction.BLOCK){
                sendHelp.setRotationX(sendHelp.getRotationX() + (MinecraftClient.getInstance().isPaused() ? 0 : 1.3f));
                sendHelp.setRotationY(sendHelp.getRotationY() + (MinecraftClient.getInstance().isPaused() ? 0 : 1));
            } else if(entity.getItemUseTimeLeft() > 0 && (entity.getMainHandStack().getUseAction() == UseAction.BOW || entity.getOffHandStack().getUseAction() == UseAction.BOW)) {
                sendHelp.setRotationX(sendHelp.getRotationX() + (MinecraftClient.getInstance().isPaused() ? 0 : 1.3f));
                sendHelp.setRotationX(sendHelp.getRotationX() + (MinecraftClient.getInstance().isPaused() ? 0 :(extraData.headPitch * (float) Math.PI / 180F)));
            } else if((entity.getMainHandStack().getUseAction() == UseAction.CROSSBOW || entity.getOffHandStack().getUseAction() == UseAction.CROSSBOW)) {
                if(entity.getItemUseTimeLeft() > 0) {
                    sendHelp.setRotationY(0.8f);
                    sendHelp.setRotationX(0.97079635f);
                    if(leftArm != null) {
                        leftArm.setRotationY(-0.97079635f);
                    }
                    float f = CrossbowItem.getPullTime(entity.getActiveItem());
                    float g = MathHelper.clamp((float)entity.getItemUseTime(), 0.0f, f);
                    float h = g / f;
                    sendHelp.setRotationY(MathHelper.lerp(h, 0.4f, 0.85f));
                    sendHelp.setRotationX(MathHelper.lerp(h, sendHelp.getRotationX(), -1.5707964f));
                } else {
                    sendHelp.setRotationX(sendHelp.getRotationX() + (MinecraftClient.getInstance().isPaused() ? 0 : 1.3f));
                    sendHelp.setRotationX(sendHelp.getRotationX() + (MinecraftClient.getInstance().isPaused() ? 0 :(extraData.headPitch * (float) Math.PI / 180F)));
                }
            } else {
                sendHelp.setRotationX(Vec3f.POSITIVE_X.getRadialQuaternion((float) (MathHelper.cos(entity.limbAngle * 0.6662F + 3.1415927F) * -2.0F * entity.limbDistance * 0.5F + ((entity.getActiveHand() == Hand.MAIN_HAND) ? (MathHelper.lerp(MathHelper.clamp(customPredicate.animationTick, 0, 1), entity.lastHandSwingProgress * 8, entity.handSwingProgress * 8)) : 0))).getX());
                sendHelp.setRotationZ((float) (sendHelp.getRotationZ() + ((entity.getActiveHand() == Hand.MAIN_HAND) ? (MathHelper.lerp(MathHelper.clamp(customPredicate.animationTick, 0, 1), entity.lastHandSwingProgress * 0.8f, entity.handSwingProgress * 0.8f)) : 0)));
            }
        }
    }


}
