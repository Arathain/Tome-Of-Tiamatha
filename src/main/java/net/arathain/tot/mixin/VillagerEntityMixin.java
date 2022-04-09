package net.arathain.tot.mixin;

import net.arathain.tot.common.init.ToTComponents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.raid.RaidManager;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin extends MerchantEntity {
    @Shadow protected abstract boolean canLevelUp();

    public VillagerEntityMixin(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void punishDeath(DamageSource source, CallbackInfo info) {
        if (source.getSource() != null) {

            List<PlayerEntity> entities = world.getEntitiesByClass(
                    PlayerEntity.class,
                    new Box(
                            this.getX() - 10, this.getY() - 10, this.getZ() - 10,
                            this.getX() + 10, this.getY() + 10, this.getZ() + 10
                    ), (entity) -> true
            );
            if (!world.isClient) {
                for (PlayerEntity entity : entities) {
                    if(entity.equals(source.getSource())) {
                        ToTComponents.ALIGNMENT_COMPONENT.get(entity).incrementVAlignment(-50);
                    }
                }
            }
        }
    }

    @Inject(method = "afterUsing", at = @At("HEAD"))
    public void happyTrade(TradeOffer offer, CallbackInfo info) {
        if (this.getCustomer() != null) {
            ToTComponents.ALIGNMENT_COMPONENT.get(this.getCustomer()).incrementVAlignment(1);
            if(this.canLevelUp()) {
                ToTComponents.ALIGNMENT_COMPONENT.get(this.getCustomer()).incrementVAlignment(2);
            }
        }

    }
}
