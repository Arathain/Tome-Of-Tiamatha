package net.arathain.tot.common.network.packet;

import io.netty.buffer.Unpooled;
import net.arathain.tot.TomeOfTiamatha;
import net.arathain.tot.common.init.ToTComponents;
import net.arathain.tot.common.init.ToTEffects;
import net.arathain.tot.common.init.ToTEntities;
import net.arathain.tot.common.init.ToTScaleTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.quiltmc.qsl.networking.api.PacketSender;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;
import virtuoel.pehkui.api.ScaleData;

public class DriderComponentPacket {
    public static final Identifier ID = new Identifier(TomeOfTiamatha.MODID, "drider");

    private static final float DRIDER_WIDTH = ToTEntities.DRIDER.getWidth() / EntityType.PLAYER.getWidth();
    private static final float DRIDER_HEIGHT = ToTEntities.DRIDER.getHeight() / EntityType.PLAYER.getHeight();

    public static void send() {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        ClientPlayNetworking.send(ID, buf);
    }


    public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler network, PacketByteBuf buf, PacketSender sender) {
        server.execute(() -> {
            handleDridering(player);
        });
    }
    @SuppressWarnings("ConstantConditions")
    public static void handleDridering(PlayerEntity player) {
        ToTComponents.DRIDER_COMPONENT.maybeGet(player).ifPresent(driderComponent -> {
            if(player.hasStatusEffect(ToTEffects.BROODS_CURSE)) {
                if(player.getStatusEffect(ToTEffects.BROODS_CURSE).getAmplifier() < 3) {
                    player.addStatusEffect( new StatusEffectInstance(ToTEffects.BROODS_CURSE, 1200, player.getStatusEffect(ToTEffects.BROODS_CURSE).getAmplifier() + 1, player.getStatusEffect(ToTEffects.BROODS_CURSE).getAmplifier() > 3, true));
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 40, 0));
                    driderComponent.setStage(player.getStatusEffect(ToTEffects.BROODS_CURSE).getAmplifier() + 1);
                    if(driderComponent.getStage() == 0) {
                        driderComponent.updateVariant();
                    }
                } else if(player.getStatusEffect(ToTEffects.BROODS_CURSE).getAmplifier() >= 3) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 60, 0));
                    ScaleData width = ToTScaleTypes.MODIFY_WIDTH_TYPE.getScaleData(player);
                    ScaleData height = ToTScaleTypes.MODIFY_HEIGHT_TYPE.getScaleData(player);
                    width.setScale(width.getBaseScale() * DRIDER_WIDTH);
                    height.setScale(height.getBaseScale() * DRIDER_HEIGHT);
                    driderComponent.setStage(player.getStatusEffect(ToTEffects.BROODS_CURSE).getAmplifier() + 1);
                    driderComponent.setDrider(true);
                    player.removeStatusEffect(ToTEffects.BROODS_CURSE);
                }
            }

        });
    }
}
