package net.arathain.tot.common.init;

import com.github.crimsondawn45.fabricshieldlib.lib.event.ShieldBlockCallback;
import com.github.crimsondawn45.fabricshieldlib.lib.object.FabricShieldItem;
import com.google.common.collect.ImmutableList;
import net.arathain.tot.TomeOfTiamatha;
import net.arathain.tot.common.block.HangingWebBlock;
import net.arathain.tot.common.block.WeaverkinEggBlock;
import net.arathain.tot.common.item.GazingLilyItem;
import net.arathain.tot.common.item.SilksteelArmorItem;
import net.arathain.tot.common.item.SynthesisScepterItem;
import net.fabricmc.fabric.api.biome.v1.BiomeModification;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tag.TagFactory;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.FlowerBlock;
import net.minecraft.block.Material;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterials;
import net.minecraft.tag.Tag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.decorator.BiomePlacementModifier;
import net.minecraft.world.gen.decorator.PlacementModifier;
import net.minecraft.world.gen.decorator.RarityFilterPlacementModifier;
import net.minecraft.world.gen.decorator.SquarePlacementModifier;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static net.arathain.tot.compat.WildfireGenderOriginsCompat.GENDER_SWAP;

public class ToTObjects {
    private static final Map<Block, Identifier> BLOCKS = new LinkedHashMap<>();
    private static final Map<Item, Identifier> ITEMS = new LinkedHashMap<>();

    public static final Tag<Item> MEAT = TagFactory.ITEM.create(new Identifier(TomeOfTiamatha.MODID, "meat"));
    private static final Block magedewSettings = new FlowerBlock(StatusEffects.INSTANT_HEALTH, 10, FabricBlockSettings.of(Material.PLANT).noCollision().nonOpaque());
    private static final Block magedewCompatSettings = new FlowerBlock(GENDER_SWAP, 10, FabricBlockSettings.of(Material.PLANT).noCollision().nonOpaque());
    //registry starts here
    public static final Block HANGING_WEB = createBlock("hanging_web", new HangingWebBlock(FabricBlockSettings.of(Material.COBWEB).noCollision().requiresTool().strength(4.0F).nonOpaque()), true);
    public static final Block WEAVEKIN_EGG = createBlock("weavekin_egg", new WeaverkinEggBlock(FabricBlockSettings.of(Material.SOLID_ORGANIC).requiresTool().strength(4.0F).nonOpaque()), true);
    public static Block MAGEDEW = createBlock("magedew", FabricLoader.getInstance().isModLoaded("wildfire_gender") ? magedewCompatSettings : magedewSettings, true);

    public static final Item SILKSTEEL_HELMET = createItem("silksteel_helmet", new SilksteelArmorItem(ToTArmorMaterials.SILKSTEEL, EquipmentSlot.HEAD, new Item.Settings().group(TomeOfTiamatha.GROUP).rarity(Rarity.UNCOMMON)));
    public static final Item SILKSTEEL_CHESTPLATE = createItem("silksteel_chestplate", new SilksteelArmorItem(ToTArmorMaterials.SILKSTEEL, EquipmentSlot.CHEST, new Item.Settings().group(TomeOfTiamatha.GROUP).rarity(Rarity.UNCOMMON)));
    public static final Item SYNTHESIS_SCEPTRE = createItem("synthesis_sceptre", new SynthesisScepterItem(ToolMaterials.NETHERITE, new Item.Settings().group(TomeOfTiamatha.GROUP).rarity(Rarity.RARE)));
    public static final Item GAZING_LILY = createItem("gazing_lily", new GazingLilyItem(new Item.Settings().group(TomeOfTiamatha.GROUP).rarity(Rarity.UNCOMMON)));
    public static final Item SILKSTEEL_SHIELD = createItem("silksteel_shield", new FabricShieldItem(new FabricItemSettings().group(TomeOfTiamatha.GROUP).maxDamage(2600), 40, 8));
    //registry end here

    //funky bits (do not touch)
    public static final ConfiguredFeature<SimpleBlockFeatureConfig, ?> MAGEDEW_FEATURE = register("magedew", Feature.SIMPLE_BLOCK.configure(new SimpleBlockFeatureConfig(BlockStateProvider.of(MAGEDEW))));
    public static final PlacedFeature MAGEDEW_PATCHES = register("magedew_patches", MAGEDEW_FEATURE.withPlacement(modifiersWithChance()));

    private static <FC extends FeatureConfig> ConfiguredFeature<FC, ?> register(String id, ConfiguredFeature<FC, ?> configuredFeature) {
        return Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, new Identifier(TomeOfTiamatha.MODID, id), configuredFeature);
    }
    private static PlacedFeature register(String id, PlacedFeature feature) {
        return Registry.register(BuiltinRegistries.PLACED_FEATURE, new Identifier(TomeOfTiamatha.MODID, id), feature);
    }

    private static List<PlacementModifier> modifiersWithChance() {
        ImmutableList.Builder<PlacementModifier> builder = ImmutableList.builder();

        builder.add(RarityFilterPlacementModifier.of(3));

        builder.add(SquarePlacementModifier.of());
        builder.add(PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP);
        builder.add(BiomePlacementModifier.of());
        return builder.build();
    }

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
        BLOCKS.keySet().forEach(block -> Registry.register(Registry.BLOCK, BLOCKS.get(block), block.equals(magedewSettings) && FabricLoader.getInstance().isModLoaded("wildfire_gender") ? magedewCompatSettings : block));
        ITEMS.keySet().forEach(item -> Registry.register(Registry.ITEM, ITEMS.get(item), item));
        BiomeModification worldGen = BiomeModifications.create(new Identifier(TomeOfTiamatha.MODID, "features"));
        worldGen.add(ModificationPhase.ADDITIONS, BiomeSelectors.categories(Biome.Category.FOREST), biomeModificationContext -> biomeModificationContext.getGenerationSettings().addBuiltInFeature(GenerationStep.Feature.VEGETAL_DECORATION, MAGEDEW_PATCHES));
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
