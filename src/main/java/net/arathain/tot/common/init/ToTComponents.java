package net.arathain.tot.common.init;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import net.arathain.tot.TomeOfTiamatha;
import net.arathain.tot.common.component.DriderPlayerComponent;
import net.arathain.tot.common.component.PlayerAlignmentComponent;
import net.minecraft.util.Identifier;

public class ToTComponents implements EntityComponentInitializer {
    public static final ComponentKey<DriderPlayerComponent> DRIDER_COMPONENT = ComponentRegistry.getOrCreate(new Identifier(TomeOfTiamatha.MODID, "drider"), DriderPlayerComponent.class);
    public static final ComponentKey<PlayerAlignmentComponent> ALIGNMENT_COMPONENT = ComponentRegistry.getOrCreate(new Identifier(TomeOfTiamatha.MODID, "vi_alignment"), PlayerAlignmentComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(DRIDER_COMPONENT, DriderPlayerComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
        registry.registerForPlayers(ALIGNMENT_COMPONENT, PlayerAlignmentComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
    }
}
