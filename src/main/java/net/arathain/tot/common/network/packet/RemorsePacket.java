package net.arathain.tot.common.network.packet;

import net.arathain.tot.TomeOfTiamatha;
import net.arathain.tot.common.init.ToTDamageSource;
import net.arathain.tot.common.init.ToTObjects;
import net.fabricmc.fabric.impl.networking.ClientSidePacketRegistryImpl;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.PacketSender;

import javax.annotation.Nullable;

public class RemorsePacket {
    public static final Identifier ID = new Identifier(TomeOfTiamatha.MODID, "remorse");

    public static void send(@Nullable Entity entity) {
        PacketByteBuf buf = PacketByteBufs.create();

        if(entity != null)
            buf.writeInt(entity.getId());

        ClientSidePacketRegistryImpl.INSTANCE.sendToServer(ID, buf);
    }

    public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler network, PacketByteBuf buf, PacketSender sender) {
        int entityId = buf.isReadable() ? buf.readInt() : -1;

        server.execute(() -> {
            if(player.getStackInHand(Hand.MAIN_HAND).getItem().equals(ToTObjects.REMORSE)) {
                float f = (float)player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                float g;
                g = EnchantmentHelper.getAttackDamage(player.getMainHandStack(), player.getGroup());

                float h = player.getAttackCooldownProgress(0.5F);
                f *= 0.2F + h * h * 0.8F;
                g *= h;
                f += g;
                player.damage(ToTDamageSource.REMORSE, f);
            }
        });
    }
}
