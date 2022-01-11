package net.arathain.tot.client;

import net.arathain.tot.TomeOfTiamatha;
import net.arathain.tot.client.entity.renderer.DriderEntityRenderer;
import net.arathain.tot.common.init.ToTComponents;
import net.arathain.tot.common.init.ToTEffects;
import net.arathain.tot.common.init.ToTEntities;
import net.arathain.tot.common.init.ToTObjects;
import net.arathain.tot.common.network.packet.DriderComponentPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;

public class TomeOfTiamathaClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), ToTObjects.HANGING_WEB);
        EntityRendererRegistry.register(ToTEntities.DRIDER, DriderEntityRenderer::new);
        ClientTickEvents.END_WORLD_TICK.register(new ClientTickEvents.EndWorldTick() {

            @Override
            public void onEndTick(ClientWorld world) {
                PlayerEntity player = MinecraftClient.getInstance().player;
                if (player != null && player.hasStatusEffect(ToTEffects.BROODS_CURSE) && player.getStatusEffect(ToTEffects.BROODS_CURSE).getDuration() < 80 && !ToTComponents.DRIDER_COMPONENT.get(player).isDrider()) {
                    DriderComponentPacket.send();

                }
            }
        });
    }
}
