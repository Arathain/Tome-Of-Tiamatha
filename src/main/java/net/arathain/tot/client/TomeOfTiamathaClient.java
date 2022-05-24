package net.arathain.tot.client;

import draylar.omegaconfiggui.OmegaConfigGui;
import net.arathain.tot.TomeOfTiamatha;
import net.arathain.tot.client.entity.model.drider.DriderDenDoorModel;
import net.arathain.tot.client.entity.model.drider.weaver.WebbingEntityModel;
import net.arathain.tot.client.entity.renderer.drider.*;
import net.arathain.tot.client.entity.renderer.drider.weaver.WeaverEntityRenderer;
import net.arathain.tot.client.entity.renderer.drider.weaver.WebbingEntityRenderer;
import net.arathain.tot.client.entity.renderer.raven.NevermoreEntityRenderer;
import net.arathain.tot.client.entity.renderer.raven.RavenEntityRenderer;
import net.arathain.tot.client.entity.shader.ToTShaderHandler;
import net.arathain.tot.client.entity.string.StringClient;
import net.arathain.tot.client.particle.ToTParticles;
import net.arathain.tot.common.init.ToTComponents;
import net.arathain.tot.common.init.ToTEffects;
import net.arathain.tot.common.init.ToTEntities;
import net.arathain.tot.common.init.ToTObjects;
import net.arathain.tot.common.network.packet.DriderComponentPacket;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.block.extensions.api.client.BlockRenderLayerMap;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents;

public class TomeOfTiamathaClient implements ClientModInitializer {
    public static final EntityModelLayer WEBBING_MODEL_LAYER = new EntityModelLayer(new Identifier(TomeOfTiamatha.MODID, "webbing"), "main");
    public static final EntityModelLayer DEN_DOOR_MODEL_LAYER = new EntityModelLayer(new Identifier(TomeOfTiamatha.MODID, "door"), "main");
    @Override
    public void onInitializeClient(ModContainer mod) {
        StringClient.init();
        BlockRenderLayerMap.put(RenderLayer.getCutout(), ToTObjects.HANGING_WEB);
        EntityRendererRegistry.register(ToTEntities.NEVERMORE, NevermoreEntityRenderer::new);
        EntityRendererRegistry.register(ToTEntities.DRIDER, DriderEntityRenderer::new);
        EntityRendererRegistry.register(ToTEntities.ARACHNE, ArachneEntityRenderer::new);
        EntityRendererRegistry.register(ToTEntities.WEAVECHILD, WeavechildEntityRenderer::new);
        EntityRendererRegistry.register(ToTEntities.WEAVER, WeaverEntityRenderer::new);
        EntityRendererRegistry.register(ToTEntities.WEAVETHRALL, WeavethrallEntityRenderer::new);
        EntityRendererRegistry.register(ToTEntities.RAVEN, RavenEntityRenderer::new);
        BlockEntityRendererRegistry.register(ToTEntities.WEAVERKIN_EGG, (BlockEntityRendererFactory.Context rendererDispatcherIn) -> new WeaverkinEggRenderer());
        OmegaConfigGui.registerConfigScreen(TomeOfTiamatha.CONFIG);
        EntityRendererRegistry.register(ToTEntities.WEBBING, WebbingEntityRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(WEBBING_MODEL_LAYER, WebbingEntityModel::getTexturedModelData);
        EntityRendererRegistry.register(ToTEntities.DEN_DOOR, DriderDenDoorRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(DEN_DOOR_MODEL_LAYER, DriderDenDoorModel::getTexturedModelData);
        ToTShaderHandler.init();
        ToTParticles.init();
        ClientTickEvents.END.register(world -> {
            PlayerEntity player = MinecraftClient.getInstance().player;
            if (player != null && player.hasStatusEffect(ToTEffects.BROODS_CURSE) && player.getStatusEffect(ToTEffects.BROODS_CURSE).getDuration() < 80 && !ToTComponents.DRIDER_COMPONENT.get(player).isDrider()) {
                DriderComponentPacket.send();

            }
        });
    }
}
