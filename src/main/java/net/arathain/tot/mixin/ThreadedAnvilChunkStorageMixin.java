package net.arathain.tot.mixin;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.arathain.tot.common.entity.string.StringKnotEntity;
import net.arathain.tot.common.entity.string.StringPacketCreator;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
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

    @Inject(
            method = "sendChunkDataPackets",
            at = @At(value = "TAIL")
    )
    private void sendAttachChainPackets(ServerPlayerEntity player, MutableObject<ChunkDataS2CPacket> cachedDataPacket, WorldChunk chunk, CallbackInfo ci) {
        ObjectIterator<ThreadedAnvilChunkStorage.EntityTracker> trackers = this.entityTrackers.values().iterator();
        List<StringKnotEntity> knots = Lists.newArrayList();

        while (trackers.hasNext()) {
            ThreadedAnvilChunkStorage.EntityTracker entityTracker = trackers.next();
            Entity entity = entityTracker.entity;
            if (entity != player && entity.getChunkPos().equals(chunk.getPos())) {
                if (entity instanceof StringKnotEntity knot && !knot.getLinks().isEmpty()) {
                    knots.add(knot);
                }
            }
        }

        for (StringKnotEntity knot : knots) {
            Packet<?> packet = StringPacketCreator.createMultiAttach(knot);
            if (packet != null) player.networkHandler.sendPacket(packet);
        }
    }
}