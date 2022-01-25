package net.arathain.tot.client.entity.renderer;

import net.arathain.tot.client.entity.model.WeavechildEntityModel;
import net.arathain.tot.common.entity.living.drider.weavekin.WeavechildEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class WeavechildEntityRenderer extends GeoEntityRenderer<WeavechildEntity> {
    public WeavechildEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new WeavechildEntityModel());
    }
}
