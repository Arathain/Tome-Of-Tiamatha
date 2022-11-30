package net.arathain.tot.client.entity.model.raven;

import net.arathain.tot.TomeOfTiamatha;
import net.arathain.tot.common.entity.living.raven.RavenEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedTickingGeoModel;

public class RavenEntityModel extends AnimatedTickingGeoModel<RavenEntity> {
    @Override
    public Identifier getModelResource(RavenEntity object) {
        return new Identifier(TomeOfTiamatha.MODID, "geo/entity/raven.geo.json");
    }

    @Override
    public Identifier getTextureResource(RavenEntity object) {
        return new Identifier(TomeOfTiamatha.MODID, "textures/entity/raven/raven_"+ object.getRavenType().toString().toLowerCase() + ".png");
    }

    @Override
    public Identifier getAnimationResource(RavenEntity animatable) {
        return new Identifier(TomeOfTiamatha.MODID, "animations/entity/raven.animation.json");
    }
}

