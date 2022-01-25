package net.arathain.tot.client.entity.model;

import net.arathain.tot.TomeOfTiamatha;
import net.arathain.tot.common.entity.living.drider.DriderEntity;
import net.arathain.tot.common.entity.living.drider.weavekin.WeavechildEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedTickingGeoModel;

public class WeavechildEntityModel extends AnimatedTickingGeoModel<WeavechildEntity> {
    @Override
    public Identifier getModelLocation(WeavechildEntity object) {
        return new Identifier(TomeOfTiamatha.MODID, "geo/entity/weavechild.geo.json");
    }

    @Override
    public Identifier getTextureLocation(WeavechildEntity object) {
        return new Identifier(TomeOfTiamatha.MODID, "textures/entity/weavekin/weavechild.png");
    }

    @Override
    public Identifier getAnimationFileLocation(WeavechildEntity animatable) {
        return new Identifier(TomeOfTiamatha.MODID, "animations/entity/weavechild.animation.json");
    }
}
