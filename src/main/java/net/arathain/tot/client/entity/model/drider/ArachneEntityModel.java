package net.arathain.tot.client.entity.model.drider;

import net.arathain.tot.TomeOfTiamatha;
import net.arathain.tot.common.entity.living.drider.arachne.ArachneEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedTickingGeoModel;
import software.bernie.geckolib3.model.provider.data.EntityModelData;

public class ArachneEntityModel extends AnimatedTickingGeoModel<ArachneEntity> {
    @Override
    public Identifier getModelLocation(ArachneEntity object) {
        return new Identifier(TomeOfTiamatha.MODID, "geo/entity/arachne.geo.json");
    }

    @Override
    public Identifier getTextureLocation(ArachneEntity object) {
        return new Identifier(TomeOfTiamatha.MODID, "textures/entity/drider/arachne/arachne.png");
    }

    @Override
    public Identifier getAnimationFileLocation(ArachneEntity animatable) {
        return new Identifier(TomeOfTiamatha.MODID, "animations/entity/arachne.animation.json");
    }
    @Override
    public void codeAnimations(ArachneEntity entity, Integer uniqueID, AnimationEvent customPredicate) {
        super.codeAnimations(entity, uniqueID, customPredicate);
        IBone head = this.getAnimationProcessor().getBone("head");
        EntityModelData extraData = (EntityModelData) customPredicate.getExtraDataOfType(EntityModelData.class).get(0);

        if (head != null) {
            head.setRotationX(head.getRotationX() + (extraData.headPitch * (float) Math.PI / 180F));
            head.setRotationY(extraData.netHeadYaw * ((float) Math.PI / 180F));
        }
    }
}
