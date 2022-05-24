package net.arathain.tot.common.world.structures;

import net.arathain.tot.TomeOfTiamatha;
import net.arathain.tot.mixin.StructureFeatureAccessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;

public class StructureInit {

    public static final StructureProcessorType<NoWaterProcessor> NOWATER_PROCESSOR = () -> NoWaterProcessor.CODEC;

    public static final Identifier DUNGEON_IDENTIFIER = new Identifier(TomeOfTiamatha.MODID, "boss_dungeon");
    public static final StructureFeature<StructurePoolFeatureConfig> DUNGEON = new DungeonStructure(StructurePoolFeatureConfig.CODEC);

    public static void registerStructureFeatures() {
        // Create structure config using config values.
        StructureFeatureAccessor.callRegister(DUNGEON_IDENTIFIER.toString(), DUNGEON, GenerationStep.Feature.SURFACE_STRUCTURES);

        Registry.register(Registry.STRUCTURE_PROCESSOR, new Identifier(TomeOfTiamatha.MODID, "nowater_processor"), NOWATER_PROCESSOR);
    }

}
