package net.arathain.tot.client.entity.model;

import net.arathain.tot.TomeOfTiamatha;
import net.arathain.tot.common.entity.DriderEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class DriderEntityModel extends AnimatedGeoModel<DriderEntity> {
    @Override
    public Identifier getModelLocation(DriderEntity object) {
        return new Identifier(TomeOfTiamatha.MODID, "geo/entity/drider.geo.json");
    }

    @Override
    public Identifier getTextureLocation(DriderEntity object) {
        return new Identifier(TomeOfTiamatha.MODID, "textures/entity/drider_" + object.getDriderType().toString().toLowerCase() + ".png");
    }

    @Override
    public Identifier getAnimationFileLocation(DriderEntity animatable) {
        return new Identifier(TomeOfTiamatha.MODID, "animations/entity/drider.geo.json");
    }
}
