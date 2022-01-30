package net.arathain.tot.mixin;

import net.arathain.tot.common.init.ToTObjects;
import net.arathain.tot.common.util.ToTUtil;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.Tag;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Inject(method = "getTooltip", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0, shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private void addUnusableTooltip(@Nullable PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir, List<Text> list) {
        if(player != null) {
            if(ToTUtil.isDrider(player) && ((ItemStack)(Object)this).isFood() && (!((ItemStack)(Object)this).isIn(ToTObjects.MEAT) && !((ItemStack)(Object)this).getItem().getFoodComponent().isMeat())) {
                MutableText preventText = new TranslatableText("tot.text.drider_not_meat").formatted(Formatting.DARK_RED);
                list.add(preventText);
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "use", cancellable = true)
    public void use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> info) {
        if(user != null) {
            ItemStack stackInHand = user.getStackInHand(hand);
            if(ToTUtil.isDrider(user) && stackInHand.isFood() && (!stackInHand.isIn(ToTObjects.MEAT) && !stackInHand.getItem().getFoodComponent().isMeat())) {
                info.setReturnValue(TypedActionResult.fail(stackInHand));
            }
        }
    }
}
