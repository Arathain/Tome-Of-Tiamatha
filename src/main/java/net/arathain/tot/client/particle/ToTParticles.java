package net.arathain.tot.client.particle;

import net.arathain.tot.TomeOfTiamatha;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ToTParticles {
    public static final DefaultParticleType RAVEN_FEATHER = add("raven_feather");
    public static final DefaultParticleType RAVEN_FEATHER_ALBINO = add("raven_feather_albino");
    public static final DefaultParticleType RAVEN_FEATHER_GREEN = add("raven_feather_green");
    public static void init() {
        ParticleFactoryRegistry registry = ParticleFactoryRegistry.getInstance();
        registry.register(RAVEN_FEATHER, RavenFeatherParticle.Factory::new);
        registry.register(RAVEN_FEATHER_ALBINO, RavenFeatherParticle.Factory::new);
        registry.register(RAVEN_FEATHER_GREEN, RavenFeatherParticle.Factory::new);
    }

    private static DefaultParticleType add(String name) {
        return Registry.register(Registry.PARTICLE_TYPE, new Identifier(TomeOfTiamatha.MODID, name), FabricParticleTypes.simple());
    }
}
