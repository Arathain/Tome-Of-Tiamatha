package net.arathain.tot.mixin;

import net.arathain.tot.common.init.ToTObjects;
import net.arathain.tot.common.util.ToTUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerScreenHandler.class)
public abstract class PlayerScreenHandlerMixin {

    @Shadow @Final private PlayerEntity owner;

    @Inject(method = "canInsertIntoSlot(Lnet/minecraft/item/ItemStack;Lnet/minecraft/screen/slot/Slot;)Z", at = @At("HEAD"), cancellable = true)
    private void tot$preventArmorInsertion(ItemStack stack, Slot slot, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = this.owner;
        if(ToTUtil.isDrider(player) && stack.isIn(ToTObjects.NO_DRIDER)) {
            cir.setReturnValue(false);
        }
    }
}
