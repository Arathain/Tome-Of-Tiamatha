package net.arathain.tot.client.entity.string;

import net.arathain.tot.TomeOfTiamatha;
import net.arathain.tot.client.entity.model.drider.StringKnotEntityModel;
import net.arathain.tot.client.entity.renderer.string.StringCollisionEntityRenderer;
import net.arathain.tot.client.entity.renderer.string.StringKnotEntityRenderer;
import net.arathain.tot.common.entity.string.StringCollisionEntity;
import net.arathain.tot.common.entity.string.StringKnotEntity;
import net.arathain.tot.common.init.ToTEntities;
import net.arathain.tot.common.init.ToTObjects;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.event.client.player.ClientPickBlockGatherCallback;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents;
import org.quiltmc.qsl.networking.api.client.ClientPlayConnectionEvents;
/**
 * @author Qendolin
 * **/
public class StringClient {
    public static final EntityModelLayer STRING_KNOT = new EntityModelLayer(new Identifier(TomeOfTiamatha.MODID, "string_knot"), "main");
    private static StringKnotEntityRenderer stringKnotEntityRenderer;
    private static StringClientPacketHandler stringClientPacketHandler;

    public static void init() {
        initRenderers();

        registerNetworkEventHandlers();
        registerClientEventHandlers();

    }

    private static void initRenderers() {
        EntityRendererRegistry.register(ToTEntities.STRING_KNOT, ctx -> {
            stringKnotEntityRenderer = new StringKnotEntityRenderer(ctx);
            return stringKnotEntityRenderer;
        });
        EntityRendererRegistry.register(ToTEntities.STRING_COLLISION,
                StringCollisionEntityRenderer::new);

        EntityModelLayerRegistry.registerModelLayer(STRING_KNOT, StringKnotEntityModel::getTexturedModelData);
    }

    private static void registerNetworkEventHandlers() {
        stringClientPacketHandler = new StringClientPacketHandler();

        ClientPlayConnectionEvents.INIT.register((handler, client) -> {
            getStringKnotEntityRenderer().getStringRenderer().purge();
        });
    }

    private static void registerClientEventHandlers() {
        ClientPickBlockGatherCallback.EVENT.register((player, result) -> {
            if (result instanceof EntityHitResult) {
                Entity entity = ((EntityHitResult) result).getEntity();
                if (entity instanceof StringKnotEntity || entity instanceof StringCollisionEntity) {
                    return new ItemStack(ToTObjects.STEELSILK);
                }
            }
            return ItemStack.EMPTY;
        });

        ClientTickEvents.START.register(world -> stringClientPacketHandler.tick());

    }

    public static StringKnotEntityRenderer getStringKnotEntityRenderer() {
        return stringKnotEntityRenderer;
    }

}
