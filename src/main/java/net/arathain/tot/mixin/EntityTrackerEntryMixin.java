package net.arathain.tot.mixin;

import io.netty.buffer.Unpooled;
import net.arathain.tot.common.entity.string.StringKnotEntity;
import net.arathain.tot.common.network.NetworkingPackages;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.EntityTrackerEntry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

/**
 * @author legoatoom
 * **/
@Mixin(EntityTrackerEntry.class)
abstract class EntityTrackerEntryMixin {
    @Shadow
    @Final
    private Entity entity;

    @Inject(method = "sendPackets", at = @At("TAIL"))
    private void sendPackages(Consumer<Packet<?>> sender, CallbackInfo ci) {
        if (this.entity instanceof StringKnotEntity stringKnotEntity) {
            PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
            //Write our id and the id of the one we connect to.
            int[] ids = stringKnotEntity.getHoldingEntities().stream().mapToInt(Entity::getId).toArray();
            if (ids.length > 0) {
                passedData.writeInt(stringKnotEntity.getId());
                passedData.writeIntArray(ids);
                sender.accept(ServerPlayNetworking.createS2CPacket(NetworkingPackages.S2C_MULTI_STRING_ATTACH_PACKET_ID, passedData));
            }
        }
    }


}
