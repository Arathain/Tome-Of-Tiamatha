package net.arathain.tot.client.entity.renderer;

import net.arathain.tot.client.entity.model.ArachneEntityModel;
import net.arathain.tot.client.entity.renderer.layer.ArachneEyeLayer;
import net.arathain.tot.common.entity.living.drider.arachne.ArachneEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class ArachneEntityRenderer extends GeoEntityRenderer<ArachneEntity> {
    public ArachneEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new ArachneEntityModel());
        this.addLayer(new ArachneEyeLayer(this));
    }
}
