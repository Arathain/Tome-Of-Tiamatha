package net.arathain.tot.client.entity.renderer;

import net.arathain.tot.client.entity.model.WeaverkinEggModel;
import net.arathain.tot.common.block.entity.WeaverkinEggBlockEntity;
import software.bernie.geckolib3.renderers.geo.GeoBlockRenderer;

public class WeaverkinEggRenderer extends GeoBlockRenderer<WeaverkinEggBlockEntity> {
    public WeaverkinEggRenderer() {
        super(new WeaverkinEggModel());
    }
}
