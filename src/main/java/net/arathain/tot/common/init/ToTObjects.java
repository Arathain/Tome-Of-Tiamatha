package net.arathain.tot.common.init;

import net.arathain.tot.TomeOfTiamatha;
import net.arathain.tot.common.block.HangingWebBlock;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.LinkedHashMap;
import java.util.Map;

public class ToTObjects {
    private static final Map<Block, Identifier> BLOCKS = new LinkedHashMap<>();
    private static final Map<Item, Identifier> ITEMS = new LinkedHashMap<>();

    //registry starts here
    public static final Block HANGING_WEB = createBlock("hanging_web", new HangingWebBlock(FabricBlockSettings.of(Material.COBWEB).noCollision().requiresTool().strength(4.0F).nonOpaque()), true);
    public static final Item SPELLWOVEN_STEEL_INGOT = createItem("spellwoven_steel_ingot", new Item(new Item.Settings().group(TomeOfTiamatha.GROUP)));
    //registry end here

    //funky bits (do not touch)

    private static <T extends Block> T createBlock(String name, T block, boolean createItem) {
        BLOCKS.put(block, new Identifier(TomeOfTiamatha.MODID, name));
        if (createItem) {
            ITEMS.put(new BlockItem(block, new Item.Settings().group(TomeOfTiamatha.GROUP)), BLOCKS.get(block));
        }
        return block;
    }
    private static <T extends Item> T createItem(String name, T item) {
        ITEMS.put(item, new Identifier(TomeOfTiamatha.MODID, name));
        return item;
    }

    public static void init() {
        BLOCKS.keySet().forEach(block -> Registry.register(Registry.BLOCK, BLOCKS.get(block), block));
        ITEMS.keySet().forEach(item -> Registry.register(Registry.ITEM, ITEMS.get(item), item));

    }
}
