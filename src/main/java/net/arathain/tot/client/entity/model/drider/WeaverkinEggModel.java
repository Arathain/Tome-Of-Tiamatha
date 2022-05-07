package net.arathain.tot.client.entity.model.drider;

import net.arathain.tot.TomeOfTiamatha;
import net.arathain.tot.common.block.entity.WeaverkinEggBlockEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3q.model.AnimatedGeoModel;

public class WeaverkinEggModel extends AnimatedGeoModel<WeaverkinEggBlockEntity> {
    private static final Identifier TEXTURE_IDENTIFIER = new Identifier(TomeOfTiamatha.MODID, "textures/block/weavekin_egg.png");
    private static final Identifier MODEL_IDENTIFIER = new Identifier(TomeOfTiamatha.MODID, "geo/block/weavekin_egg.geo.json");
    private static final Identifier ANIMATION_IDENTIFIER = new Identifier(TomeOfTiamatha.MODID, "animations/block/weavekin_egg.animation.json");
    @Override
    public Identifier getModelResource(WeaverkinEggBlockEntity object) {
        return MODEL_IDENTIFIER;
    }

    @Override
    public Identifier getTextureResource(WeaverkinEggBlockEntity object) {
        return TEXTURE_IDENTIFIER;
    }

    @Override
    public Identifier getAnimationResource(WeaverkinEggBlockEntity animatable) {
        return ANIMATION_IDENTIFIER;
    }
}
