package net.arathain.tot.common.entity.string;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.arathain.tot.common.network.NetworkingPackages;
import net.arathain.tot.common.network.PacketBufUtil;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

/**
 * <a href="https://fabricmc.net/wiki/tutorial:projectiles">This class is from a tutorial</a> Qendolin edited some things to make it more useful for themselves.
 */
public class StringPacketCreator {
    /**
     * Creates a spawn packet for {@code e} with additional data from {@code extraData}.
     *
     * @param e         The entity to spawn
     * @param packetID  The spawn packet id
     * @param extraData Extra data supplier
     * @return A S2C packet
     */
    public static Packet<?> createSpawn(Entity e, Identifier packetID, Function<PacketByteBuf, PacketByteBuf> extraData) {
        if (e.world.isClient)
            throw new IllegalStateException("Called on the logical client!");
        PacketByteBuf byteBuf = new PacketByteBuf(Unpooled.buffer());
        byteBuf.writeVarInt(Registry.ENTITY_TYPE.getRawId(e.getType()));
        byteBuf.writeUuid(e.getUuid());
        byteBuf.writeVarInt(e.getId());

        PacketBufUtil.writeVec3d(byteBuf, e.getPos());
        // pitch and yaw don't matter so don't send them
        byteBuf = extraData.apply(byteBuf);
        return ServerPlayNetworking.createS2CPacket(packetID, byteBuf);
    }

    /**
     * Creates a multi attach packet for a knot
     *
     * @param knot the primary knot
     * @return Packet or null if no data is to be sent
     */
    @Nullable
    public static Packet<?> createMultiAttach(StringKnotEntity knot) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        List<StringLink> links = knot.getLinks();
        IntList ids = new IntArrayList(links.size());
        for (StringLink link : links) {
            if (link.primary == knot) {
                ids.add(link.secondary.getId());
            }
        }
        if (ids.size() > 0) {
            buf.writeInt(knot.getId());
            buf.writeIntList(ids);
            return ServerPlayNetworking.createS2CPacket(NetworkingPackages.S2C_MULTI_STRING_ATTACH_PACKET_ID, buf);
        }
        return null;
    }
}
