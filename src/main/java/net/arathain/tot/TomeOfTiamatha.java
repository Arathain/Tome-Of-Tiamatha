package net.arathain.tot;

import draylar.omegaconfig.OmegaConfig;
import io.github.ladysnake.locki.InventoryLock;
import io.github.ladysnake.locki.Locki;
import net.arathain.tot.common.init.ToTEffects;
import net.arathain.tot.common.init.ToTEntities;
import net.arathain.tot.common.init.ToTObjects;
import net.arathain.tot.common.init.ToTScaleTypes;
import net.arathain.tot.common.network.packet.DriderComponentPacket;
import net.arathain.tot.common.util.ToTCallbacks;
import net.arathain.tot.common.util.ToTUtil;
import net.arathain.tot.common.util.config.ToTConfig;
import net.arathain.tot.compat.WildfireGenderOriginsCompat;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib3.GeckoLib;

public class TomeOfTiamatha implements ModInitializer {
	public static String MODID = "tot";
	public static final ItemGroup GROUP = FabricItemGroupBuilder.build(new Identifier(MODID, MODID), () -> new ItemStack(Blocks.MAGENTA_BED));
	public static final InventoryLock DRIDER_LOCK = Locki.registerLock(new Identifier(MODID, "drider"), true);
	public static final ToTConfig CONFIG = OmegaConfig.register(ToTConfig.class);
	public static final Logger LOGGER = LoggerFactory.getLogger("tot");

	@Override
	public void onInitialize() {
		ToTObjects.init();
		GeckoLib.initialize();
		ToTEntities.init();
		ToTEffects.init();
		ToTScaleTypes.init();
		if(FabricLoader.getInstance().isModLoaded("wildfire_gender") && FabricLoader.getInstance().isModLoaded("origins")) {
			WildfireGenderOriginsCompat.init();
		}
		ServerPlayNetworking.registerGlobalReceiver(DriderComponentPacket.ID, DriderComponentPacket::handle);
		UseBlockCallback.EVENT.register(ToTCallbacks::stringUseEvent);
	}
}
