package net.arathain.tot.client.entity.string;

import com.github.legoatoom.connectiblechains.ConnectibleChains;
import com.github.legoatoom.connectiblechains.util.Helper;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.arathain.tot.TomeOfTiamatha;
import net.arathain.tot.client.entity.model.drider.StringKnotEntityModel;
import net.arathain.tot.client.entity.renderer.string.StringCollisionEntityRenderer;
import net.arathain.tot.client.entity.renderer.string.StringKnotEntityRenderer;
import net.arathain.tot.common.entity.string.IncompleteStringLink;
import net.arathain.tot.common.entity.string.StringCollisionEntity;
import net.arathain.tot.common.entity.string.StringKnotEntity;
import net.arathain.tot.common.entity.string.StringLink;
import net.arathain.tot.common.init.ToTEntities;
import net.arathain.tot.common.network.NetworkingPackages;
import net.arathain.tot.common.network.PacketBufUtil;
import net.arathain.tot.common.util.StringUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.event.client.player.ClientPickBlockGatherCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;

import java.util.UUID;
/**
 * @author Qendolin
 * **/
public class StringClient {
    public static final EntityModelLayer STRING_KNOT = new EntityModelLayer(new Identifier(TomeOfTiamatha.MODID, "string_knot"), "main");
    private static StringKnotEntityRenderer StringKnotEntityRenderer;
    private static StringClientPacketHandler stringClientPacketHandler;

    public static void init() {
        initRenderers();

        registerNetworkEventHandlers();
        registerClientEventHandlers();

    }

    private static void initRenderers() {
        EntityRendererRegistry.register(ToTEntities.STRING_KNOT, ctx -> {
            StringKnotEntityRenderer = new StringKnotEntityRenderer(ctx);
            return StringKnotEntityRenderer;
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
                if (entity instanceof StringKnotEntity knot) {
                    return new ItemStack(Items.STRING);
                } else if (entity instanceof StringCollisionEntity collision) {
                    return new ItemStack(Items.STRING);
                }
            }
            return ItemStack.EMPTY;
        });

        ClientTickEvents.START_WORLD_TICK.register(world -> stringClientPacketHandler.tick());

    }

    public static StringKnotEntityRenderer getStringKnotEntityRenderer() {
        return StringKnotEntityRenderer;
    }

}
