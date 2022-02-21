package net.arathain.tot.client.entity.string;

import net.arathain.tot.client.entity.model.drider.StringKnotEntityModel;
import net.arathain.tot.client.entity.renderer.string.StringCollisionEntityRenderer;
import net.arathain.tot.client.entity.renderer.string.StringKnotEntityRenderer;
import net.arathain.tot.common.entity.string.StringCollisionEntity;
import net.arathain.tot.common.entity.string.StringKnotEntity;
import net.arathain.tot.common.init.ToTEntities;
import net.arathain.tot.common.network.NetworkingPackages;
import net.arathain.tot.common.network.PacketBufUtil;
import net.arathain.tot.common.util.StringUtils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.event.client.player.ClientPickBlockGatherCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;

import java.util.UUID;

public class StringClient {
    public static final EntityModelLayer CHAIN_KNOT = new EntityModelLayer(StringUtils.identifier("string_knot"), "main");
    private static StringKnotEntityRenderer stringKnotEntityRenderer = null;
    public static void init() {
        initRenderers();
        registerReceiverClientPackages();
    }
    private static void initRenderers() {
        EntityRendererRegistry.register(ToTEntities.STRING_KNOT, ctx -> {
            stringKnotEntityRenderer = new StringKnotEntityRenderer(ctx);
            return stringKnotEntityRenderer;
        });
        EntityRendererRegistry.register(ToTEntities.STRING_COLLISION,
                StringCollisionEntityRenderer::new);

        EntityModelLayerRegistry.registerModelLayer(CHAIN_KNOT, StringKnotEntityModel::getTexturedModelData);
    }

    private static void registerReceiverClientPackages() {
        ClientPlayNetworking.registerGlobalReceiver(NetworkingPackages.S2C_STRING_ATTACH_PACKET_ID,
                (client, handler, packetByteBuf, responseSender) -> {
                    int[] fromTo = packetByteBuf.readIntArray();
                    int fromPlayer = packetByteBuf.readInt();
                    client.execute(() -> {
                        if (client.world != null) {
                            Entity entity = client.world.getEntityById(fromTo[0]);
                            if (entity instanceof StringKnotEntity) {
                                ((StringKnotEntity) entity).addHoldingEntityId(fromTo[1], fromPlayer);
                            } else {
                                LogManager.getLogger().warn("Received Attach Chain Package to unknown Entity.");
                            }
                        }
                    });
                });

        ClientPlayNetworking.registerGlobalReceiver(NetworkingPackages.S2C_STRING_DETACH_PACKET_ID,
                (client, handler, packetByteBuf, responseSender) -> {
                    int[] fromTo = packetByteBuf.readIntArray();
                    client.execute(() -> {
                        if (client.world != null) {
                            Entity entity = client.world.getEntityById(fromTo[0]);
                            if (entity instanceof StringKnotEntity) {
                                ((StringKnotEntity) entity).removeHoldingEntityId(fromTo[1]);
                            }
                        }
                    });
                });

        ClientPlayNetworking.registerGlobalReceiver(NetworkingPackages.S2C_MULTI_STRING_ATTACH_PACKET_ID,
                (client, handler, packetByteBuf, responseSender) -> {
                    int from = packetByteBuf.readInt();
                    int[] tos = packetByteBuf.readIntArray();
                    client.execute(() -> {
                        if (client.world != null) {
                            Entity entity = client.world.getEntityById(from);
                            if (entity instanceof StringKnotEntity) {
                                ((StringKnotEntity) entity).addHoldingEntityIds(tos);
                            }
                        }
                    });
                });

        ClientPlayNetworking.registerGlobalReceiver(NetworkingPackages.S2C_SPAWN_STRING_COLLISION_PACKET,
                (client, handler, buf, responseSender) -> {
                    int entityTypeID = buf.readVarInt();
                    EntityType<?> entityType = Registry.ENTITY_TYPE.get(entityTypeID);
                    UUID uuid = buf.readUuid();
                    int entityId = buf.readVarInt();
                    Vec3d pos = PacketBufUtil.readVec3d(buf);
                    float pitch = PacketBufUtil.readAngle(buf);
                    float yaw = PacketBufUtil.readAngle(buf);

                    int startId = buf.readVarInt();
                    int endId = buf.readVarInt();

                    client.execute(() -> {
                        if (MinecraftClient.getInstance().world == null){
                            throw new IllegalStateException("Tried to spawn entity in a null world!");
                        }
                        Entity e = entityType.create(MinecraftClient.getInstance().world);
                        if (e == null){
                            throw new IllegalStateException("Failed to create instance of entity \"" + entityTypeID + "\"");
                        }
                        e.setPosition(pos.x, pos.y, pos.z);
                        e.setPitch(pitch);
                        e.setYaw(yaw);
                        e.setId(entityId);
                        e.setUuid(uuid);
                        e.setVelocity(Vec3d.ZERO);
                        if (e instanceof StringCollisionEntity){

                            ((StringCollisionEntity) e).setStartOwnerId(startId);
                            ((StringCollisionEntity) e).setEndOwnerId(endId);
                        }
                        MinecraftClient.getInstance().world.addEntity(entityId, e);
                    });
                });

        ClientPlayNetworking.registerGlobalReceiver(NetworkingPackages.S2C_SPAWN_STRING_KNOT_PACKET,
                (client, handler, buf, responseSender) -> {
                    int entityTypeID = buf.readVarInt();
                    EntityType<?> entityType = Registry.ENTITY_TYPE.get(entityTypeID);
                    UUID uuid = buf.readUuid();
                    int entityId = buf.readVarInt();
                    Vec3d pos = PacketBufUtil.readVec3d(buf);
                    float pitch = PacketBufUtil.readAngle(buf);
                    float yaw = PacketBufUtil.readAngle(buf);

                    client.execute(() -> {
                        if (MinecraftClient.getInstance().world == null){
                            throw new IllegalStateException("Tried to spawn entity in a null world!");
                        }
                        Entity e = entityType.create(MinecraftClient.getInstance().world);
                        if (e == null){
                            throw new IllegalStateException("Failed to create instance of entity \"" + entityTypeID + "\"");
                        }
                        e.setPosition(pos.x, pos.y, pos.z);
                        e.setPitch(pitch);
                        e.setYaw(yaw);
                        e.setId(entityId);
                        e.setUuid(uuid);
                        e.setVelocity(Vec3d.ZERO);
                        MinecraftClient.getInstance().world.addEntity(entityId, e);
                    });
                });

        ClientPickBlockGatherCallback.EVENT.register((player, result) -> {
            if (result instanceof EntityHitResult){
                Entity entity = ((EntityHitResult) result).getEntity();
                if (entity instanceof StringKnotEntity || entity instanceof StringCollisionEntity){
                    return new ItemStack(Items.STRING);
                }
            }
            return ItemStack.EMPTY;
        });
    }
    public StringKnotEntityRenderer getChainKnotEntityRenderer() {
        return stringKnotEntityRenderer;
    }
}
