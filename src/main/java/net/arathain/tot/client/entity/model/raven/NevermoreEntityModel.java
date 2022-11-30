package net.arathain.tot.client.entity.model.raven;

import net.arathain.tot.TomeOfTiamatha;
import net.arathain.tot.common.entity.living.raven.NevermoreEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedTickingGeoModel;

public class NevermoreEntityModel extends AnimatedTickingGeoModel<NevermoreEntity> {
    @Override
    public Identifier getModelResource(NevermoreEntity object) {
        return new Identifier(TomeOfTiamatha.MODID, "geo/entity/nevermore.geo.json");
    }

    @Override
    public Identifier getTextureResource(NevermoreEntity object) {
        return new Identifier(TomeOfTiamatha.MODID, "textures/entity/raven/nevermore.png");
    }

    @Override
    public Identifier getAnimationResource(NevermoreEntity animatable) {
        return new Identifier(TomeOfTiamatha.MODID, "animations/entity/nevermore.animation.json");
    }
}
