package net.arathain.tot.common.init;

import net.arathain.tot.TomeOfTiamatha;
import net.arathain.tot.common.world.structures.NoWaterProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ToTStructures {

    public static final StructureProcessorType<NoWaterProcessor> NOWATER_PROCESSOR = () -> NoWaterProcessor.CODEC;

    public static final Identifier DUNGEON_IDENTIFIER = new Identifier(TomeOfTiamatha.MODID, "drider_den");
    //public static final StructureFeature<StructurePoolFeatureConfig> DUNGEON = new DriderDenStructure(StructurePoolFeatureConfig.CODEC);

    public static void init() {
        // Create structure config using config values.
        //StructureFeatureAccessor.callRegister(DUNGEON_IDENTIFIER.toString(), DUNGEON, GenerationStep.Feature.SURFACE_STRUCTURES);

        Registry.register(Registry.STRUCTURE_PROCESSOR, new Identifier(TomeOfTiamatha.MODID, "nowater_processor"), NOWATER_PROCESSOR);
    }

}
