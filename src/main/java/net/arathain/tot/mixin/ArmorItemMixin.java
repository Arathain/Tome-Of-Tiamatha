package net.arathain.tot.mixin;


import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorItem.class)
public abstract class ArmorItemMixin extends Item {
    public ArmorItemMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void cancelUse(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
//        ItemStack itemStack = user.getStackInHand(hand);
//        if (InventoryKeeper.get(user).isLocked(DefaultInventoryNodes.FEET) && MobEntity.getPreferredEquipmentSlot(itemStack).equals(EquipmentSlot.FEET)) {
//            cir.setReturnValue(TypedActionResult.fail(user.getStackInHand(hand)));
//        }
//        if (InventoryKeeper.get(user).isLocked(DefaultInventoryNodes.LEGS) && MobEntity.getPreferredEquipmentSlot(itemStack).equals(EquipmentSlot.LEGS)) {
//            cir.setReturnValue(TypedActionResult.fail(user.getStackInHand(hand)));
//        }
    }
}
