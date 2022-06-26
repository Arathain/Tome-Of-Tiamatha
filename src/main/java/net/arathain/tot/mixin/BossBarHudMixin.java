package net.arathain.tot.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.arathain.tot.TomeOfTiamatha;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(BossBarHud.class)
public class BossBarHudMixin {
    @ModifyArgs(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/BossBarHud;renderBossBar(Lnet/minecraft/client/util/math/MatrixStack;IILnet/minecraft/entity/boss/BossBar;)V"))
    private void mixin(Args args) {
        if(((ClientBossBar)args.get(3)).getName().toString().contains("entity.tot.arachne")) {
            RenderSystem.setShaderTexture(0, new Identifier(TomeOfTiamatha.MODID, "textures/gui/bars.png"));
        }
    }
}
