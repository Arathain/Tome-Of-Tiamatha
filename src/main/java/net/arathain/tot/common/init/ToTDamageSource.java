package net.arathain.tot.common.init;

import net.minecraft.entity.damage.DamageSource;

public class ToTDamageSource extends DamageSource {
    public static final DamageSource REMORSE = new ToTDamageSource("remorse").setBypassesArmor().setUnblockable();

    protected ToTDamageSource(String name) {
        super(name);
    }
}
