package net.arathain.tot;

import io.github.ladysnake.locki.InventoryLock;
import io.github.ladysnake.locki.Locki;
import net.arathain.tot.common.init.ToTEffects;
import net.arathain.tot.common.init.ToTEntities;
import net.arathain.tot.common.init.ToTObjects;
import net.arathain.tot.common.init.ToTScaleTypes;
import net.arathain.tot.common.network.packet.DriderComponentPacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.GeckoLib;

public class TomeOfTiamatha implements ModInitializer {
	public static String MODID = "tot";
	public static final ItemGroup GROUP = FabricItemGroupBuilder.build(new Identifier(MODID, MODID), () -> new ItemStack(Blocks.MAGENTA_BED));
	public static final InventoryLock DRIDER_LOCK = Locki.registerLock(new Identifier(MODID, "drider"), true);

	@Override
	public void onInitialize() {
		ToTObjects.init();
		GeckoLib.initialize();
		ToTEntities.init();
		ToTEffects.init();
		ToTScaleTypes.init();
		ServerPlayNetworking.registerGlobalReceiver(DriderComponentPacket.ID, DriderComponentPacket::handle);
	}
}
