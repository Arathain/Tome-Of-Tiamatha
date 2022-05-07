package net.arathain.tot.client.entity.renderer.drider;

import net.arathain.tot.client.entity.model.drider.WeavechildEntityModel;
import net.arathain.tot.client.entity.renderer.layer.WeavechildEyeLayer;
import net.arathain.tot.common.entity.living.drider.weavekin.WeavechildEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib3q.renderers.geo.GeoEntityRenderer;

public class WeavechildEntityRenderer extends GeoEntityRenderer<WeavechildEntity> {
    public WeavechildEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new WeavechildEntityModel());
        this.addLayer(new WeavechildEyeLayer(this));
    }
}
