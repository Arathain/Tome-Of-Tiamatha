package net.arathain.tot.client.entity.renderer.raven;

import net.arathain.tot.client.entity.model.raven.NevermoreEntityModel;
import net.arathain.tot.common.entity.living.raven.NevermoreEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class NevermoreEntityRenderer extends GeoEntityRenderer<NevermoreEntity> {
    public NevermoreEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new NevermoreEntityModel());
    }
}
