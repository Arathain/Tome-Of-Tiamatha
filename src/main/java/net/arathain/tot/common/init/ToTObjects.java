package net.arathain.tot.common.init;

import com.github.crimsondawn45.fabricshieldlib.lib.event.ShieldBlockCallback;
import net.arathain.tot.TomeOfTiamatha;
import net.arathain.tot.common.block.HangingWebBlock;
import net.arathain.tot.common.block.WeaverkinEggBlock;
import net.arathain.tot.common.item.*;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.FlowerBlock;
import net.minecraft.block.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.tag.TagKey;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;

import java.util.LinkedHashMap;
import java.util.Map;
public class ToTObjects {
    private static final Map<Block, Identifier> BLOCKS = new LinkedHashMap<>();
    private static final Map<Item, Identifier> ITEMS = new LinkedHashMap<>();

    public static final TagKey<Item> MEAT = TagKey.of(Registry.ITEM_KEY, new Identifier(TomeOfTiamatha.MODID, "meat"));
    //registry starts here
    public static final Block HANGING_WEB = createBlock("hanging_web", new HangingWebBlock(FabricBlockSettings.of(Material.COBWEB).noCollision().requiresTool().strength(4.0F).nonOpaque()), true);
    public static final Block WEAVEKIN_EGG = createBlock("weavekin_egg", new WeaverkinEggBlock(FabricBlockSettings.of(Material.SOLID_ORGANIC).requiresTool().strength(4.0F).nonOpaque()), true);
    public static final Item SILKSTEEL_HELMET = createItem("silksteel_helmet", new SilksteelArmorItem(ToTArmorMaterials.SILKSTEEL, EquipmentSlot.HEAD, new Item.Settings().group(TomeOfTiamatha.GROUP).rarity(Rarity.UNCOMMON)));
    public static final Item SILKSTEEL_CHESTPLATE = createItem("silksteel_chestplate", new SilksteelArmorItem(ToTArmorMaterials.SILKSTEEL, EquipmentSlot.CHEST, new Item.Settings().group(TomeOfTiamatha.GROUP).rarity(Rarity.UNCOMMON)));
    public static final Item SYNTHESIS_SCEPTRE = createItem("synthesis_sceptre", new SynthesisScepterItem(ToolMaterials.NETHERITE, new Item.Settings().group(TomeOfTiamatha.GROUP).rarity(Rarity.RARE)));
    public static final Item SILKSTEEL_INGOT = createItem("silksteel_ingot", new Item(new Item.Settings().group(TomeOfTiamatha.GROUP)));
    public static final Item SILKSTEEL_SWORD = createItem("silksteel_sword", new SilksteelSwordItem(ToTToolMaterials.SILKSTEEL, 1, -2.0f, new Item.Settings().group(TomeOfTiamatha.GROUP)));
    public static final Item STEELSILK = createItem("steelsilk", new Item(new Item.Settings().group(TomeOfTiamatha.GROUP)));
    public static final Item GAZING_LILY = createItem("gazing_lily", new GazingLilyItem(new Item.Settings().group(TomeOfTiamatha.GROUP).rarity(Rarity.UNCOMMON)));
    public static final Item SILKSTEEL_SHIELD = createItem("silksteel_shield", new SilksteelShieldItem(new FabricItemSettings().group(TomeOfTiamatha.GROUP).maxDamage(1600), 60, 8));

    public static final Item RAVEN_SPAWN_EGG = createItem("raven_spawn_egg", new SpawnEggItem(ToTEntities.RAVEN, 0x182c3b, 0x828c78, (new Item.Settings()).group(ItemGroup.MISC)));
    //registry end here


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
        ShieldBlockCallback.EVENT.register((defender, source, amount, hand, shield) -> {
            if(shield.getItem().equals(SILKSTEEL_SHIELD)) {
                Entity attacker = source.getAttacker();

                assert attacker != null;
                if(defender instanceof PlayerEntity) {
                    attacker.damage(DamageSource.player((PlayerEntity) defender), (int)Math.round(amount * 0.33F));
                } else {
                    attacker.damage(DamageSource.mob(defender), (int)Math.round(amount * 0.33F));
                }
            }

            return ActionResult.PASS;
        });
    }
}
