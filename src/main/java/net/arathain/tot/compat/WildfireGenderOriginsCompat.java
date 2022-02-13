package net.arathain.tot.compat;

import net.arathain.tot.TomeOfTiamatha;
import net.arathain.tot.common.effect.DriderCurseStatusEffect;
import net.arathain.tot.compat.wgo.GenderChangeStatusEffect;
import net.arathain.tot.compat.wgo.GenderSwapEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.LinkedHashMap;
import java.util.Map;

public class WildfireGenderOriginsCompat {
    private static final Map<StatusEffect, Identifier> STATUS_EFFECTS = new LinkedHashMap<>();

    public static final StatusEffect GENDER_SWAP = create("gender_swap", new GenderSwapEffect(StatusEffectCategory.BENEFICIAL, 0xf50056));
    public static final StatusEffect TESTOSTERONE = create("testosterone", new GenderChangeStatusEffect(StatusEffectCategory.BENEFICIAL, 0xf50056, 1));
    public static final StatusEffect ESTROGEN = create("estrogen", new GenderChangeStatusEffect(StatusEffectCategory.BENEFICIAL, 0xf50056, 0));


    private static <T extends StatusEffect> T create(String name, T effect) {
        STATUS_EFFECTS.put(effect, new Identifier(TomeOfTiamatha.MODID, name));
        return effect;
    }

    public static void init() {
        STATUS_EFFECTS.keySet().forEach(effect -> Registry.register(Registry.STATUS_EFFECT, STATUS_EFFECTS.get(effect), effect));
    }
}
