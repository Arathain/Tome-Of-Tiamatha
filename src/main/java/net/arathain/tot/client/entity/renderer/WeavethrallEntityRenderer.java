package net.arathain.tot.client.entity.renderer;

import net.arathain.tot.client.entity.model.WeavethrallEntityModel;
import net.arathain.tot.client.entity.renderer.layer.WeavethrallEyeLayer;
import net.arathain.tot.common.entity.living.drider.weavekin.WeavethrallEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class WeavethrallEntityRenderer extends GeoEntityRenderer<WeavethrallEntity> {
    public WeavethrallEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new WeavethrallEntityModel());
        this.addLayer(new WeavethrallEyeLayer(this));
    }
}
