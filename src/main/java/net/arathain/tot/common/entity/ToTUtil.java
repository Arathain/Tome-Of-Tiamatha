package net.arathain.tot.common.entity;

import net.arathain.tot.common.component.DriderPlayerComponent;
import net.arathain.tot.common.init.ToTComponents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class ToTUtil {
    public static boolean isDrider(Entity entity) {
        if (entity instanceof PlayerEntity player) {
            DriderPlayerComponent transformationComponent = ToTComponents.DRIDER_COMPONENT.get(player);
            if (transformationComponent.isDrider()) {
                return true;
            }
        }
        return entity instanceof DriderEntity;
    }
}
