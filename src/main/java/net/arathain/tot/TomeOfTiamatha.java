package net.arathain.tot;

import draylar.omegaconfig.OmegaConfig;
import net.arathain.tot.common.init.*;
import net.arathain.tot.common.network.packet.DriderComponentPacket;
import net.arathain.tot.common.network.packet.RemorsePacket;
import net.arathain.tot.common.util.ToTCallbacks;
import net.arathain.tot.common.util.ToTUtil;
import net.arathain.tot.common.util.config.ToTConfig;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.item.group.api.QuiltItemGroup;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib3.GeckoLib;

public class TomeOfTiamatha implements ModInitializer {
	public static String MODID = "tot";
	public static final ItemGroup GROUP = QuiltItemGroup.createWithIcon(new Identifier(MODID, MODID), () -> new ItemStack(ToTObjects.SILKSTEEL_HELMET));
	public static final ToTConfig CONFIG = OmegaConfig.register(ToTConfig.class);
	public static final Logger LOGGER = LoggerFactory.getLogger("tot");

	@Override
	public void onInitialize(ModContainer mod) {
		ToTObjects.init();
		GeckoLib.initialize();
		ToTEntities.init();
		ToTEffects.init();
		ToTScaleTypes.init();
		ToTStructures.init();
		ServerPlayNetworking.registerGlobalReceiver(DriderComponentPacket.ID, DriderComponentPacket::handle);
		ServerPlayNetworking.registerGlobalReceiver(RemorsePacket.ID, RemorsePacket::handle);
		ServerPlayerEvents.AFTER_RESPAWN.register(((oldPlayer, newPlayer, alive) -> {
			if(ToTUtil.isDrider(oldPlayer) || ToTUtil.isDrider(newPlayer)) {
				DriderComponentPacket.scale(newPlayer);
			}
		}));
		UseBlockCallback.EVENT.register(ToTCallbacks::stringUseEvent);
	}
}
