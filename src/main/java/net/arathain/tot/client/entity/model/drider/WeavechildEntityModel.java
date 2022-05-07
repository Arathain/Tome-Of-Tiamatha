package net.arathain.tot.client.entity.model.drider;

import net.arathain.tot.TomeOfTiamatha;
import net.arathain.tot.common.entity.living.drider.DriderEntity;
import net.arathain.tot.common.entity.living.drider.weavekin.WeavechildEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3q.model.AnimatedTickingGeoModel;

public class WeavechildEntityModel extends AnimatedTickingGeoModel<WeavechildEntity> {
    @Override
    public Identifier getModelResource(WeavechildEntity object) {
        return new Identifier(TomeOfTiamatha.MODID, "geo/entity/weavechild.geo.json");
    }

    @Override
    public Identifier getTextureResource(WeavechildEntity object) {
        return new Identifier(TomeOfTiamatha.MODID, "textures/entity/weavekin/weavechild.png");
    }

    @Override
    public Identifier getAnimationResource(WeavechildEntity animatable) {
        return new Identifier(TomeOfTiamatha.MODID, "animations/entity/weavechild.animation.json");
    }
}
