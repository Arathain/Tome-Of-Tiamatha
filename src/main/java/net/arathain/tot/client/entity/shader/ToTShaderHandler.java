package net.arathain.tot.client.entity.shader;

import ladysnake.satin.api.event.ShaderEffectRenderCallback;
import ladysnake.satin.api.managed.ManagedShaderEffect;
import ladysnake.satin.api.managed.ShaderEffectManager;
import net.arathain.tot.TomeOfTiamatha;
import net.arathain.tot.common.init.ToTComponents;
import net.arathain.tot.common.util.ToTUtil;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Identifier;

public class ToTShaderHandler {
    public static final ManagedShaderEffect DRIDER_VISION = ShaderEffectManager.getInstance()
            .manage(new Identifier(TomeOfTiamatha.MODID, "shaders/post/drider_vision.json"));
    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(ToTShaderHandler::onEndTick);
        ShaderEffectRenderCallback.EVENT.register(ToTShaderHandler::renderShaderEffects);
    }
    private static void renderShaderEffects(float v) {
        if (MinecraftClient.getInstance().player != null) {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (ToTUtil.isDrider(player) || ToTComponents.DRIDER_COMPONENT.get(player).getStage() > 0) {
                DRIDER_VISION.render(v);
            }
        }
    }

    private static void onEndTick(MinecraftClient client) {
        if (client.player != null && client.cameraEntity != null) {
            if (ToTUtil.isDrider(client.player) || ToTComponents.DRIDER_COMPONENT.get(client.player).getStage() > 0) {
                DRIDER_VISION.findUniform1f("BrightnessAdjust").set((float) (Math.pow(ToTComponents.DRIDER_COMPONENT.get(client.player).getStage(), 2) / 8f));
            }
        }
    }
}
