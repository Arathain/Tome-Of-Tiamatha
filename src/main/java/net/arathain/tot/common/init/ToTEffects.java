package net.arathain.tot.common.init;

import net.arathain.tot.TomeOfTiamatha;
import net.arathain.tot.common.effect.DriderCurseStatusEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.LinkedHashMap;
import java.util.Map;

public class ToTEffects {
    private static final Map<StatusEffect, Identifier> STATUS_EFFECTS = new LinkedHashMap<>();

    public static final StatusEffect BROODS_CURSE = create("broods_curse", new DriderCurseStatusEffect(StatusEffectCategory.BENEFICIAL, 0x605448));

    private static <T extends StatusEffect> T create(String name, T effect) {
        STATUS_EFFECTS.put(effect, new Identifier(TomeOfTiamatha.MODID, name));
        return effect;
    }

    public static void init() {
        STATUS_EFFECTS.keySet().forEach(effect -> Registry.register(Registry.STATUS_EFFECT, STATUS_EFFECTS.get(effect), effect));
    }
}
