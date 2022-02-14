package net.arathain.tot.compat;

import com.google.common.collect.ImmutableList;
import net.arathain.tot.TomeOfTiamatha;
import net.arathain.tot.common.block.HangingWebBlock;
import net.arathain.tot.common.effect.DriderCurseStatusEffect;
import net.arathain.tot.common.init.ToTEffects;
import net.arathain.tot.compat.wgo.GenderChangeStatusEffect;
import net.arathain.tot.compat.wgo.GenderSwapEffect;
import net.arathain.tot.mixin.compat.genderorigins.BrewingRecipeAccessor;
import net.fabricmc.fabric.api.biome.v1.*;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.FlowerBlock;
import net.minecraft.block.Material;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.util.Identifier;
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

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WildfireGenderOriginsCompat {
    private static final Map<StatusEffect, Identifier> STATUS_EFFECTS = new LinkedHashMap<>();
    private static final Map<Block, Identifier> BLOCKS = new LinkedHashMap<>();
    private static final Map<Item, Identifier> ITEMS = new LinkedHashMap<>();

    public static final StatusEffect GENDER_SWAP = createEffect("gender_swap", new GenderSwapEffect(StatusEffectCategory.BENEFICIAL, 0xf50056));
    public static final StatusEffect TESTOSTERONE = createEffect("testosterone", new GenderChangeStatusEffect(StatusEffectCategory.BENEFICIAL, 0xf50056, 1));
    public static final StatusEffect ESTROGEN = createEffect("estrogen", new GenderChangeStatusEffect(StatusEffectCategory.BENEFICIAL, 0xf50056, 0));
    public static final Potion ESTR_POT = new Potion("estrogen", new StatusEffectInstance(ESTROGEN, 1, 0));
    public static final Potion TESTO_POT = new Potion("testosterone", new StatusEffectInstance(TESTOSTERONE, 1, 0));
    public static final Block MAGEDEW = createBlock("magedew", new FlowerBlock(GENDER_SWAP, 10, FabricBlockSettings.of(Material.PLANT).noCollision().nonOpaque()), true);
    public static final ConfiguredFeature<SimpleBlockFeatureConfig, ?> MAGEDEW_FEATURE = register("magedew", Feature.SIMPLE_BLOCK.configure(new SimpleBlockFeatureConfig(BlockStateProvider.of(MAGEDEW))));
    public static final PlacedFeature MAGEDEW_PATCHES = register("magedew_patches", MAGEDEW_FEATURE.withPlacement(modifiersWithChance(3, null)));

    private static <FC extends FeatureConfig> ConfiguredFeature<FC, ?> register(String id, ConfiguredFeature<FC, ?> configuredFeature) {
        return Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, new Identifier(TomeOfTiamatha.MODID, id), configuredFeature);
    }
    private static PlacedFeature register(String id, PlacedFeature feature) {
        return Registry.register(BuiltinRegistries.PLACED_FEATURE, new Identifier(TomeOfTiamatha.MODID, id), feature);
    }

    private static List<PlacementModifier> modifiersWithChance(int chance, @Nullable PlacementModifier modifier) {
        ImmutableList.Builder<PlacementModifier> builder = ImmutableList.builder();

        if (modifier != null) {
            builder.add(modifier);
        }

        if (chance != 0) {
            builder.add(RarityFilterPlacementModifier.of(chance));
        }

        builder.add(SquarePlacementModifier.of());
        builder.add(PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP);
        builder.add(BiomePlacementModifier.of());
        return builder.build();
    }

    private static <T extends StatusEffect> T createEffect(String name, T effect) {
        STATUS_EFFECTS.put(effect, new Identifier(TomeOfTiamatha.MODID, name));
        return effect;
    }
    private static <T extends Block> T createBlock(String name, T block, boolean createItem) {
        BLOCKS.put(block, new Identifier(TomeOfTiamatha.MODID, name));
        if (createItem) {
            ITEMS.put(new BlockItem(block, new Item.Settings().group(TomeOfTiamatha.GROUP)), BLOCKS.get(block));
        }
        return block;
    }

    public static void init() {
        STATUS_EFFECTS.keySet().forEach(effect -> Registry.register(Registry.STATUS_EFFECT, STATUS_EFFECTS.get(effect), effect));
        BLOCKS.keySet().forEach(block -> Registry.register(Registry.BLOCK, BLOCKS.get(block), block));
        ITEMS.keySet().forEach(item -> Registry.register(Registry.ITEM, ITEMS.get(item), item));
        BiomeModification worldGen = BiomeModifications.create(new Identifier(TomeOfTiamatha.MODID, "features"));
        worldGen.add(ModificationPhase.ADDITIONS, BiomeSelectors.categories(Biome.Category.FOREST), biomeModificationContext -> biomeModificationContext.getGenerationSettings().addBuiltInFeature(GenerationStep.Feature.VEGETAL_DECORATION, MAGEDEW_PATCHES));
        BrewingRecipeAccessor.registerPotionRecipe(Potions.LONG_REGENERATION, MAGEDEW.asItem(), TESTO_POT);
        BrewingRecipeAccessor.registerPotionRecipe(Potions.STRONG_REGENERATION, MAGEDEW.asItem(), ESTR_POT);
        Registry.register(Registry.POTION, new Identifier(TomeOfTiamatha.MODID, "testosterone"), TESTO_POT);
        Registry.register(Registry.POTION, new Identifier(TomeOfTiamatha.MODID, "estrogen"), ESTR_POT);
    }
}
