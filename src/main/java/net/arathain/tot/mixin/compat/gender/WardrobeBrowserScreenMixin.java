package net.arathain.tot.mixin.compat.gender;

import com.wildfire.gui.WildfireButton;
import com.wildfire.gui.screen.WardrobeBrowserScreen;
import com.wildfire.main.GenderPlayer;
import com.wildfire.main.WildfireGender;
import com.wildfire.mixins.PlayerEntityMixin;
import com.wildfire.mixins.PlayerRenderMixin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(value = WardrobeBrowserScreen.class, remap = false)
public abstract class WardrobeBrowserScreenMixin extends Screen {
    @Shadow private UUID playerUUID;

    protected WardrobeBrowserScreenMixin(Text title) {
        super(title);
    }

    //realism or something
    @Redirect(method = "init", at = @At(value = "INVOKE", target = "Lcom/wildfire/gui/screen/WardrobeBrowserScreen;addDrawableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;", ordinal = 0))
    private Element genderRestrictionRedirectWaaaaaa(WardrobeBrowserScreen screen, Element element) {
        MinecraftClient m = MinecraftClient.getInstance();
        int j = this.height / 2;
        GenderPlayer plr = WildfireGender.getPlayerByName(this.playerUUID.toString());
        LiteralText genderString = new LiteralText((new TranslatableText("wildfire_gender.label.gender")).getString() + " - ");

        return new WildfireButton(this.width / 2 - 42, j - 52, 158, 20, genderString, button -> {
            if (plr.gender == 0) {
                plr.gender = 2;
            } else if (plr.gender == 1) {
                plr.gender = 0;
            } else {
                plr.gender = 1;
            }

            LiteralText btnString = new LiteralText((new TranslatableText("wildfire_gender.label.gender")).getString() + " - ");
            if (plr.gender == 0) {
                btnString.append(Formatting.LIGHT_PURPLE + (new TranslatableText("wildfire_gender.label.female")).getString());
            } else if (plr.gender == 1) {
                btnString.append(Formatting.BLUE + (new TranslatableText("wildfire_gender.label.male")).getString());
            } else if (plr.gender == 2) {
                btnString.append(Formatting.GREEN + (new TranslatableText("wildfire_gender.label.other")).getString());
            }

            button.setMessage(btnString);
            GenderPlayer.saveGenderInfo(plr);
        });
    }
}
