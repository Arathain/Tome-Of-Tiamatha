package net.arathain.tot.mixin;

import com.google.common.collect.Lists;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.arathain.tot.common.entity.string.StringKnotEntity;
import net.arathain.tot.common.network.NetworkingPackages;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.world.chunk.WorldChunk;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Keeps track of string connections.
 * @author legoatoom
 * **/
@Mixin(ThreadedAnvilChunkStorage.class)
public abstract class ThreadedAnvilChunkStorageMixin {

    @Shadow
    @Final
    private Int2ObjectMap<ThreadedAnvilChunkStorage.EntityTracker> entityTrackers;

    @Inject(method = "sendChunkDataPackets", at = @At(value = "TAIL"))
    private void sendAttachChainPackets(ServerPlayerEntity player, MutableObject<ChunkDataS2CPacket> cachedDataPacket, WorldChunk chunk, CallbackInfo ci) {
        ObjectIterator<ThreadedAnvilChunkStorage.EntityTracker> var6 = this.entityTrackers.values().iterator();
        List<StringKnotEntity> list = Lists.newArrayList();

        while (var6.hasNext()) {
            ThreadedAnvilChunkStorage.EntityTracker entityTracker = var6.next();
            Entity entity = entityTracker.entity;
            if (entity != player && entity.getChunkPos().equals(chunk.getPos())) {
                if (entity instanceof StringKnotEntity && !((StringKnotEntity) entity).getHoldingEntities().isEmpty()) {
                    list.add((StringKnotEntity) entity);
                }
            }
        }

        if (!list.isEmpty()) {
            for (StringKnotEntity stringKnotEntity : list) {
                PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
                //Write our id and the id of the one we connect to.
                int[] ids = stringKnotEntity.getHoldingEntities().stream().mapToInt(Entity::getId).toArray();
                if (ids.length > 0) {
                    passedData.writeInt(stringKnotEntity.getId());
                    passedData.writeIntArray(ids);
                    ServerPlayNetworking.send(player, NetworkingPackages.S2C_MULTI_STRING_ATTACH_PACKET_ID, passedData);
                }
            }
        }
    }
}