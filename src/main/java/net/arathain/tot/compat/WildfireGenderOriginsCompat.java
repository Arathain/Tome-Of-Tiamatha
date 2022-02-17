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

import static net.arathain.tot.common.init.ToTObjects.MAGEDEW;

public class WildfireGenderOriginsCompat {
    private static final Map<StatusEffect, Identifier> STATUS_EFFECTS = new LinkedHashMap<>();

    public static final StatusEffect GENDER_SWAP = createEffect("gender_swap", new GenderSwapEffect(StatusEffectCategory.BENEFICIAL, 0xf50056));
    public static final StatusEffect TESTOSTERONE = createEffect("testosterone", new GenderChangeStatusEffect(StatusEffectCategory.BENEFICIAL, 0xf50056, 1));
    public static final StatusEffect ESTROGEN = createEffect("estrogen", new GenderChangeStatusEffect(StatusEffectCategory.BENEFICIAL, 0xf50056, 0));
    public static final Potion ESTR_POT = new Potion("estrogen", new StatusEffectInstance(ESTROGEN, 10, 0));
    public static final Potion TESTO_POT = new Potion("testosterone", new StatusEffectInstance(TESTOSTERONE, 10, 0));


    private static <T extends StatusEffect> T createEffect(String name, T effect) {
        STATUS_EFFECTS.put(effect, new Identifier(TomeOfTiamatha.MODID, name));
        return effect;
    }

    public static void init() {
        STATUS_EFFECTS.keySet().forEach(effect -> Registry.register(Registry.STATUS_EFFECT, STATUS_EFFECTS.get(effect), effect));
        BrewingRecipeAccessor.registerPotionRecipe(Potions.LONG_REGENERATION, MAGEDEW.asItem(), TESTO_POT);
        BrewingRecipeAccessor.registerPotionRecipe(Potions.STRONG_REGENERATION, MAGEDEW.asItem(), ESTR_POT);
        Registry.register(Registry.POTION, new Identifier(TomeOfTiamatha.MODID, "testosterone"), TESTO_POT);
        Registry.register(Registry.POTION, new Identifier(TomeOfTiamatha.MODID, "estrogen"), ESTR_POT);
    }
}
