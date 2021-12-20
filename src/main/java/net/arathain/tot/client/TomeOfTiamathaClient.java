package net.arathain.tot.client;

import net.arathain.tot.TomeOfTiamatha;
import net.arathain.tot.client.entity.renderer.DriderEntityRenderer;
import net.arathain.tot.common.init.ToTEntities;
import net.arathain.tot.common.init.ToTObjects;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.minecraft.client.render.RenderLayer;

public class TomeOfTiamathaClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), ToTObjects.HANGING_WEB);
        EntityRendererRegistry.INSTANCE.register(ToTEntities.DRIDER, DriderEntityRenderer::new);

    }
}
