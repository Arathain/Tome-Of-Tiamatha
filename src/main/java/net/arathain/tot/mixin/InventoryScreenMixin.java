package net.arathain.tot.mixin;

import net.arathain.tot.common.entity.DriderEntity;
import net.arathain.tot.common.entity.ToTUtil;
import net.arathain.tot.common.init.ToTComponents;
import net.arathain.tot.common.init.ToTEntities;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Environment(EnvType.CLIENT)
@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractInventoryScreen<PlayerScreenHandler> {

    public InventoryScreenMixin(PlayerScreenHandler screenHandler, PlayerInventory playerInventory, Text text) {
        super(screenHandler, playerInventory, text);
    }
    @Inject(method = "drawEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;scale(FFF)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILSOFT)
    private static void drawDrider(int x, int y, int size, float mouseX, float mouseY, LivingEntity entity, CallbackInfo ci, float f, float g, MatrixStack matrixstack, MatrixStack matrixstack2) {
        matrixstack2.scale(1.4f, 1.0f / ToTEntities.DRIDER.getHeight(), 1.4f);
    }

    private static DriderEntity getDrider(PlayerEntity player) {
        if (ToTUtil.isDrider(player)) {
            DriderEntity entity = ToTEntities.DRIDER.create(player.world);
            assert entity != null;
            entity.getDataTracker().set(DriderEntity.TYPE, ToTComponents.DRIDER_COMPONENT.get(player).getVariant().toString());
            return entity;
        }
        return null;
    }
}
