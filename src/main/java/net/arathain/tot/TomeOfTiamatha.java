package net.arathain.tot;

import net.arathain.tot.common.init.ToTObjects;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.bernie.geckolib3.GeckoLib;

public class TomeOfTiamatha implements ModInitializer {
	public static String MODID = "tot";
	public static final ItemGroup GROUP = FabricItemGroupBuilder.build(new Identifier(MODID, MODID), () -> new ItemStack(Blocks.MAGENTA_BED));

	@Override
	public void onInitialize() {
		ToTObjects.init();
		GeckoLib.initialize();
	}
}
