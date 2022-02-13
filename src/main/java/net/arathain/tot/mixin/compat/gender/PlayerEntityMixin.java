package net.arathain.tot.mixin.compat.gender;

import com.wildfire.main.GenderPlayer;
import com.wildfire.main.WildfireGender;
import net.arathain.tot.common.init.ToTComponents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void timck(CallbackInfo info) {
        if(ToTComponents.DRIDER_COMPONENT.get(this).getStage() > 0 && FabricLoader.getInstance().isModLoaded("wildfire_gender") && this.world.isClient) {
            GenderPlayer aPlr = WildfireGender.getPlayerByName(this.getUuid().toString());
            if (aPlr != null) {
                aPlr.gender = 2;
                aPlr.pBustSize = 0.9F;
                aPlr.breast_physics = true;
                GenderPlayer.saveGenderInfo(aPlr);
            }
        }
    }
}
