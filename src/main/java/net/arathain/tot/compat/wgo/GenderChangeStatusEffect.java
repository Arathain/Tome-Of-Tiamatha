package net.arathain.tot.compat.wgo;

import com.wildfire.main.GenderPlayer;
import com.wildfire.main.WildfireGender;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.InstantStatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;

public class GenderChangeStatusEffect extends InstantStatusEffect {
    private final int gender;
    public GenderChangeStatusEffect(StatusEffectCategory statusEffectCategory, int i, int gaemr) {
        super(statusEffectCategory, i);
        gender = gaemr;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        if(entity instanceof PlayerEntity player) {
            if(player.getWorld().isClient) {
                GenderPlayer aPlr = WildfireGender.getPlayerByName(player.getUuid().toString());
                if (aPlr != null) {
                    aPlr.gender = gender;
                    GenderPlayer.saveGenderInfo(aPlr);
                }
            }
        }
        super.applyUpdateEffect(entity, amplifier);
    }

}
