package net.arathain.tot.mixin;

import net.arathain.tot.common.init.ToTObjects;
import net.arathain.tot.common.util.ToTUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net/minecraft/screen/PlayerScreenHandler$1")
public abstract class PlayerScreenHandlerMixin extends Slot {

    public PlayerScreenHandlerMixin(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Inject(method = "canInsert(Lnet/minecraft/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void tot$preventArmorInsertion(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = ((PlayerInventory)inventory).player;
        if(ToTUtil.isDrider(player) && stack.isIn(ToTObjects.NO_DRIDER)) {
            cir.setReturnValue(false);
        }
    }
}
