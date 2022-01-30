package net.arathain.tot.client.entity.model;

import net.arathain.tot.TomeOfTiamatha;
import net.arathain.tot.common.entity.living.drider.weavekin.WeavethrallEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedTickingGeoModel;
import software.bernie.geckolib3.model.provider.data.EntityModelData;

public class WeavethrallEntityModel extends AnimatedTickingGeoModel<WeavethrallEntity> {

    @Override
    public Identifier getModelLocation(WeavethrallEntity object) {
        return new Identifier(TomeOfTiamatha.MODID, "geo/entity/weavethrall.geo.json");
    }

    @Override
    public Identifier getTextureLocation(WeavethrallEntity object) {
        return new Identifier(TomeOfTiamatha.MODID, "textures/entity/weavekin/weavethrall.png");
    }

    @Override
    public Identifier getAnimationFileLocation(WeavethrallEntity animatable) {
        return new Identifier(TomeOfTiamatha.MODID, "animations/entity/weavethrall.animation.json");
    }

    @Override
    public void codeAnimations(WeavethrallEntity entity, Integer uniqueID, AnimationEvent<?> customPredicate) {
        super.codeAnimations(entity, uniqueID, customPredicate);
        IBone head = this.getAnimationProcessor().getBone("head");
        EntityModelData extraData = (EntityModelData) customPredicate.getExtraDataOfType(EntityModelData.class).get(0);
        if (head != null) {
            head.setRotationX(head.getRotationX() + (extraData.headPitch * (float) Math.PI / 180F));
            head.setRotationY(extraData.netHeadYaw * ((float) Math.PI / 180F));
        }
    }
}
