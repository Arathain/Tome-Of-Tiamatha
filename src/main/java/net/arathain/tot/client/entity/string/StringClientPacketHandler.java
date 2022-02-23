package net.arathain.tot.client.entity.string;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.arathain.tot.common.entity.string.IncompleteStringLink;
import net.arathain.tot.common.entity.string.StringKnotEntity;
import net.arathain.tot.common.entity.string.StringLink;
import net.arathain.tot.common.network.NetworkingPackages;
import net.arathain.tot.common.network.PacketBufUtil;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;

import java.util.UUID;

public class StringClientPacketHandler {
    /**
     * Links where this is the primary and the secondary doesn't yet exist / hasn't yet loaded.
     * They are kept in a separate list to prevent accidental accesses of the secondary which would
     * result in a NPE. The links will try to be completed each world tick.
     */
    private final ObjectList<IncompleteStringLink> incompleteLinks = new ObjectArrayList<>(256);

    public StringClientPacketHandler() {
        register();
    }

    private void register() {
        ClientPlayNetworking.registerGlobalReceiver(NetworkingPackages.S2C_STRING_ATTACH_PACKET_ID,
                (client, handler, packetByteBuf, responseSender) -> {
                    int fromId = packetByteBuf.readVarInt();
                    int toId = packetByteBuf.readVarInt();
                    client.execute(() -> {
                        if (client.world == null) return;
                        Entity from = client.world.getEntityById(fromId);
                        if (from instanceof StringKnotEntity knot) {
                            Entity to = client.world.getEntityById(toId);
                            if (to == null) {
                                incompleteLinks.add(new IncompleteStringLink(knot, toId));
                            } else {
                                StringLink.create(knot, to);
                            }
                        } else {
                            throw createBadActionTargetException("attach from", from, fromId, "string knot");
                        }
                    });
                });

        ClientPlayNetworking.registerGlobalReceiver(NetworkingPackages.S2C_STRING_DETACH_PACKET_ID,
                (client, handler, packetByteBuf, responseSender) -> {
                    int fromId = packetByteBuf.readVarInt();
                    int toId = packetByteBuf.readVarInt();
                    client.execute(() -> {
                        if (client.world == null) return;
                        Entity from = client.world.getEntityById(fromId);
                        Entity to = client.world.getEntityById(toId);
                        if (from instanceof StringKnotEntity knot) {
                            if (to == null) {
                                for (IncompleteStringLink link : incompleteLinks) {
                                    if (link.primary == from && link.secondaryId == toId)
                                        link.destroy();
                                }
                            } else {
                                for (StringLink link : knot.getLinks()) {
                                    if (link.secondary == to) {
                                        link.destroy(true);
                                    }
                                }
                            }
                        } else {
                            throw createBadActionTargetException("detach from", from, fromId, "string knot");
                        }
                    });
                });

        ClientPlayNetworking.registerGlobalReceiver(NetworkingPackages.S2C_MULTI_STRING_ATTACH_PACKET_ID,
                (client, handler, packetByteBuf, responseSender) -> {
                    int fromId = packetByteBuf.readInt();
                    int[] toIds = packetByteBuf.readIntArray();
                    client.execute(() -> {
                        if (client.world == null) return;
                        Entity from = client.world.getEntityById(fromId);
                        if (from instanceof StringKnotEntity knot) {
                            for (int toId : toIds) {
                                Entity to = client.world.getEntityById(toId);

                                if (to == null) {
                                    incompleteLinks.add(new IncompleteStringLink(knot, toId));
                                } else {
                                    StringLink.create(knot, to);
                                }
                            }
                        } else {
                            throw createBadActionTargetException("multi-attach from", from, fromId, "string knot");
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

                    client.execute(() -> {
                        if (client.world == null) {
                            throw new IllegalStateException("Tried to spawn entity in a null world!");
                        }
                        Entity e = entityType.create(client.world);
                        if (e == null) {
                            throw new IllegalStateException("Failed to create instance of entity " + entityTypeID);
                        }
                        e.setPosition(pos.x, pos.y, pos.z);
                        e.setId(entityId);
                        e.setUuid(uuid);
                        e.setVelocity(Vec3d.ZERO);
                        client.world.addEntity(entityId, e);
                    });
                });

        ClientPlayNetworking.registerGlobalReceiver(NetworkingPackages.S2C_SPAWN_STRING_KNOT_PACKET,
                (client, handler, buf, responseSender) -> {
                    int entityTypeId = buf.readVarInt();
                    EntityType<?> entityType = Registry.ENTITY_TYPE.get(entityTypeId);
                    UUID uuid = buf.readUuid();
                    int entityId = buf.readVarInt();
                    Vec3d pos = PacketBufUtil.readVec3d(buf);

                    client.execute(() -> {
                        if (client.world == null) {
                            throw new IllegalStateException("Tried to spawn entity in a null world!");
                        }
                        Entity e = entityType.create(client.world);
                        if (e == null) {
                            throw new IllegalStateException("Failed to create instance of entity " + entityTypeId);
                        }
                        e.setPosition(pos.x, pos.y, pos.z);
                        e.setId(entityId);
                        e.setUuid(uuid);
                        e.setVelocity(Vec3d.ZERO);
                        if (e instanceof StringKnotEntity knot) {
                            knot.setGraceTicks((byte) 0);
                        }
                        client.world.addEntity(entityId, e);
                    });
                });
    }
    private RuntimeException createBadActionTargetException(String action, Entity target, int targetId, String expectedTarget) {
        return new IllegalStateException(String.format("Tried to %s %s (#%d) which is not %s",
                action, target, targetId, expectedTarget
        ));
    }

    public void tick() {
        incompleteLinks.removeIf(IncompleteStringLink::tryCompleteOrRemove);
    }
}
