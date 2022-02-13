package net.arathain.tot.mixin.compat.gender;

import com.mojang.blaze3d.systems.RenderSystem;
import com.wildfire.gui.WildfirePlayerList;
import net.arathain.tot.TomeOfTiamatha;
import net.arathain.tot.common.init.ToTComponents;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = WildfirePlayerList.Entry.class, remap = false)
public class WildfirePlayerListMixin {
    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;IIIIIIIZF)V", locals = LocalCapture.CAPTURE_FAILSOFT, at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/util/Identifier;)V", shift = At.Shift.AFTER, ordinal = 2))
    private void cursedDriderSkinFix(MatrixStack m, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean p_194999_5_, float partialTicks, CallbackInfo info, TextRenderer font, PlayerEntity player) {
        if(ToTComponents.DRIDER_COMPONENT.get(player).getStage() > 0) {
            RenderSystem.setShaderTexture(0, new Identifier(TomeOfTiamatha.MODID, "textures/entity/drider/tf/tf_" + ToTComponents.DRIDER_COMPONENT.get(player).getVariant().toString().toLowerCase() + ".png"));
        }
    }
}
