package net.arathain.tot.client.entity.model.raven;

import net.arathain.tot.TomeOfTiamatha;
import net.arathain.tot.common.entity.living.drider.arachne.ArachneEntity;
import net.arathain.tot.common.entity.living.raven.RavenEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedTickingGeoModel;
import software.bernie.geckolib3.model.provider.data.EntityModelData;

public class RavenEntityModel extends AnimatedTickingGeoModel<RavenEntity> {
    @Override
    public Identifier getModelLocation(RavenEntity object) {
        return new Identifier(TomeOfTiamatha.MODID, "geo/entity/raven.geo.json");
    }

    @Override
    public Identifier getTextureLocation(RavenEntity object) {
        return new Identifier(TomeOfTiamatha.MODID, "textures/entity/raven/raven.png");
    }

    @Override
    public Identifier getAnimationFileLocation(RavenEntity animatable) {
        return new Identifier(TomeOfTiamatha.MODID, "animations/entity/raven.animation.json");
    }
}

