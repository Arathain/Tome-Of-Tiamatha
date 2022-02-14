package net.arathain.tot.compat.wgo;

import com.wildfire.main.GenderPlayer;
import com.wildfire.main.WildfireGender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.InstantStatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

public class GenderChangeStatusEffect extends InstantStatusEffect {
    private final int gender;
    public GenderChangeStatusEffect(StatusEffectCategory statusEffectCategory, int i, int gaemr) {
        super(statusEffectCategory, i);
        gender = gaemr;
    }

    @Override
    public boolean isInstant() {
        return false;
    }

    @Override
    public void applyInstantEffect(@Nullable Entity source, @Nullable Entity attacker, LivingEntity target, int amplifier, double proximity) {
        applyUpdateEffect(target, amplifier);
        super.applyInstantEffect(source, attacker, target, amplifier, proximity);
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
