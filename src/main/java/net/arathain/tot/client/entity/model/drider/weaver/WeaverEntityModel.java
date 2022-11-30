package net.arathain.tot.client.entity.model.drider.weaver;

import net.arathain.tot.TomeOfTiamatha;
import net.arathain.tot.common.entity.living.drider.weaver.WeaverEntity;
import net.coderbot.iris.Iris;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;
import org.quiltmc.loader.api.QuiltLoader;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedTickingGeoModel;
import software.bernie.geckolib3.model.provider.data.EntityModelData;

public class WeaverEntityModel extends AnimatedTickingGeoModel<WeaverEntity> {
    @Override
    public Identifier getModelResource(WeaverEntity object) {
        return new Identifier(TomeOfTiamatha.MODID, "geo/entity/weaver.geo.json");
    }

    @Override
    public Identifier getTextureResource(WeaverEntity object) {
        return new Identifier(TomeOfTiamatha.MODID, "textures/entity/drider/weaver/weaver.png");
    }

    @Override
    public Identifier getAnimationResource(WeaverEntity animatable) {
        return new Identifier(TomeOfTiamatha.MODID, "animations/entity/weaver.animation.json");
    }


    @Override
    public void codeAnimations(WeaverEntity entity, Integer uniqueID, AnimationEvent customPredicate) {
        super.codeAnimations(entity, uniqueID, customPredicate);
        IBone head = this.getAnimationProcessor().getBone("head");
        EntityModelData extraData = (EntityModelData) customPredicate.getExtraDataOfType(EntityModelData.class).get(0);
        IBone leftArm = this.getAnimationProcessor().getBone("leftarm");
        IBone sendHelp = this.getAnimationProcessor().getBone("rightarm");
        IBone leftGlove = this.getAnimationProcessor().getBone("glove2");
        leftGlove.setHidden(entity.getEquippedStack(EquipmentSlot.CHEST).isEmpty());

        if (head != null) {
            head.setRotationX(head.getRotationX() + (extraData.headPitch * (float) Math.PI / 180F * (QuiltLoader.isModLoaded("iris") && Iris.getIrisConfig().areShadersEnabled() ? 0.5f : 1)));
            head.setRotationY(extraData.netHeadYaw * ((float) Math.PI / 180F));
        }
        if(leftArm != null) {
            leftArm.setRotationX(leftArm.getRotationX());
            leftArm.setRotationY(leftArm.getRotationY());
            if(entity.isBlocking() && entity.getItemUseTimeLeft() > 0 && entity.getOffHandStack().getUseAction() == UseAction.BLOCK && !(entity.getMainHandStack().getUseAction() == UseAction.BLOCK)){
                leftArm.setRotationX(leftArm.getRotationX() + 1.3f);
                leftArm.setRotationY(leftArm.getRotationY() - 1);
            } else {
                leftArm.setRotationX(Vec3f.POSITIVE_X.getRadialQuaternion((float) (MathHelper.cos(entity.limbAngle * 0.6662F + 3.1415927F) * 2.0F * entity.limbDistance * 0.5F + ((entity.getActiveHand() == Hand.OFF_HAND) ? (MathHelper.lerp(MathHelper.clamp(customPredicate.animationTick, 0, 1), entity.lastHandSwingProgress * 8, entity.handSwingProgress * 8)) : 0))).getX());
                leftArm.setRotationZ((float) (leftArm.getRotationZ() - ((entity.getActiveHand() == Hand.OFF_HAND) ? (MathHelper.lerp(MathHelper.clamp(customPredicate.animationTick, 0, 1), entity.lastHandSwingProgress * 0.8f, entity.handSwingProgress * 0.8f)) : 0)));
            }
        }

        if (sendHelp != null) {
            if(entity.isBlocking() && entity.getItemUseTimeLeft() > 0 && entity.getMainHandStack().getUseAction() == UseAction.BLOCK){
                sendHelp.setRotationX(sendHelp.getRotationX() + 1.3f);
                sendHelp.setRotationY(sendHelp.getRotationY() + 1);
            } else {
                sendHelp.setRotationX(Vec3f.POSITIVE_X.getRadialQuaternion((float) (MathHelper.cos(entity.limbAngle * 0.6662F + 3.1415927F) * -2.0F * entity.limbDistance * 0.5F + ((entity.getActiveHand() == Hand.MAIN_HAND) ? (MathHelper.lerp(MathHelper.clamp(customPredicate.animationTick, 0, 1), entity.lastHandSwingProgress * 8, entity.handSwingProgress * 8)) : 0))).getX());
                sendHelp.setRotationZ((float) (sendHelp.getRotationZ() + ((entity.getActiveHand() == Hand.MAIN_HAND) ? (MathHelper.lerp(MathHelper.clamp(customPredicate.animationTick, 0, 1), entity.lastHandSwingProgress * 0.8f, entity.handSwingProgress * 0.8f)) : 0)));
            }
        }
    }


}

