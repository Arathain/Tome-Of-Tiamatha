package net.arathain.tot.mixin;

import net.arathain.tot.common.init.ToTObjects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ElytraItem.class)
public class ElytraItemMixin extends Item {

    public ElytraItemMixin(Settings settings) {
        super(settings);
    }
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void tot$elytra(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if(user.getStackInHand(hand).isIn(ToTObjects.NO_DRIDER)) {
            cir.setReturnValue(TypedActionResult.fail(user.getStackInHand(hand)));
        }
    }
}
