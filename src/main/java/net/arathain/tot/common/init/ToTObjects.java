package net.arathain.tot.common.init;

import com.github.crimsondawn45.fabricshieldlib.lib.object.FabricShieldItem;
import net.arathain.tot.TomeOfTiamatha;
import net.arathain.tot.common.block.HangingWebBlock;
import net.arathain.tot.common.item.GazingLilyItem;
import net.arathain.tot.common.item.SilksteelArmorItem;
import net.arathain.tot.common.item.SynthesisScepterItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterials;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;

import java.util.LinkedHashMap;
import java.util.Map;

public class ToTObjects {
    private static final Map<Block, Identifier> BLOCKS = new LinkedHashMap<>();
    private static final Map<Item, Identifier> ITEMS = new LinkedHashMap<>();

    //registry starts here
    public static final Block HANGING_WEB = createBlock("hanging_web", new HangingWebBlock(FabricBlockSettings.of(Material.COBWEB).noCollision().requiresTool().strength(4.0F).nonOpaque()), true);
    public static final Item SILKSTEEL_HELMET = createItem("silksteel_helmet", new SilksteelArmorItem(ToTArmorMaterials.SILKSTEEL, EquipmentSlot.HEAD, new Item.Settings().group(TomeOfTiamatha.GROUP).rarity(Rarity.UNCOMMON)));
    public static final Item SILKSTEEL_CHESTPLATE = createItem("silksteel_chestplate", new SilksteelArmorItem(ToTArmorMaterials.SILKSTEEL, EquipmentSlot.CHEST, new Item.Settings().group(TomeOfTiamatha.GROUP).rarity(Rarity.UNCOMMON)));
    public static final Item SYNTHESIS_SCEPTRE = createItem("synthesis_sceptre", new SynthesisScepterItem(ToolMaterials.NETHERITE, new Item.Settings().group(TomeOfTiamatha.GROUP).rarity(Rarity.RARE)));
    public static final Item GAZING_LILY = createItem("gazing_lily", new GazingLilyItem(new Item.Settings().group(TomeOfTiamatha.GROUP).rarity(Rarity.UNCOMMON)));
    public static final Item SILKSTEEL_SHIELD = createItem("silksteel_shield", new FabricShieldItem(new FabricItemSettings().group(TomeOfTiamatha.GROUP).maxDamage(2600), 6, 8));
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
