package net.arathain.tot.mixin;

import net.arathain.tot.common.init.ToTComponents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.PatrolEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RaiderEntity.class)
public abstract class RaiderEntityMixin extends PatrolEntity {
    @Shadow public abstract boolean hasActiveRaid();

    protected RaiderEntityMixin(EntityType<? extends PatrolEntity> entityType, World world) {
        super(entityType, world);
    }
    @Inject(method = "onDeath", at = @At("HEAD"))
    private void death(DamageSource source, CallbackInfo info) {
        if (source.getSource() != null) {
            if (source.getSource() instanceof PlayerEntity player) {
                ToTComponents.VI_ALIGNMENT_COMPONENT.get(player).incrementIAlignment(-40);
                if(this.hasActiveRaid()) {
                    ToTComponents.VI_ALIGNMENT_COMPONENT.get(player).incrementVAlignment(10);
                }
            }
        }
    }
}
