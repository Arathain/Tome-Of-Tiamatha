package net.arathain.tot.compat.wgo;

import com.wildfire.main.GenderPlayer;
import com.wildfire.main.WildfireGender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.InstantStatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

public class GenderSwapEffect extends InstantStatusEffect {
    boolean hasApplied;
    public GenderSwapEffect(StatusEffectCategory statusEffectCategory, int i) {
        super(statusEffectCategory, i);
        hasApplied = false;
    }

    @Override
    public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        hasApplied = false;
        super.onApplied(entity, attributes, amplifier);
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        if(entity instanceof PlayerEntity player) {
            if(player.getWorld().isClient) {
                GenderPlayer aPlr = WildfireGender.getPlayerByName(player.getUuid().toString());
                if (aPlr != null && aPlr.gender != 2 && !hasApplied) {
                    aPlr.gender = aPlr.gender == 1 ? 0 : 1;
                    GenderPlayer.saveGenderInfo(aPlr);
                    hasApplied = true;
                }
            }
        }
        super.applyUpdateEffect(entity, amplifier);
    }

}
