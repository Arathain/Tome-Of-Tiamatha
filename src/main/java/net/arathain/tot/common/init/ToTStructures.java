package net.arathain.tot.common.init;

import net.arathain.tot.TomeOfTiamatha;
import net.arathain.tot.common.worldgen.structure.drider.DriderDenStructure;
import net.arathain.tot.common.worldgen.structure.drider.NoWaterProcessor;
import net.arathain.tot.mixin.StructureFeatureAccessor;
import net.minecraft.item.Item;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;

import java.util.LinkedHashMap;
import java.util.Map;

public class ToTStructures {
    private static final Map<StructureFeature<StructurePoolFeatureConfig>, Identifier> STRUCTURES = new LinkedHashMap<>();
    public static final StructureProcessorType<NoWaterProcessor> NO_WATER_PROCESSOR = () -> NoWaterProcessor.CODEC;
    public static final StructureFeature<StructurePoolFeatureConfig> DRIDER_DEN = createStructure("drider_den", new DriderDenStructure(StructurePoolFeatureConfig.CODEC));

    private static <T extends StructureFeature<StructurePoolFeatureConfig>> T createStructure(String name, T struct) {
        STRUCTURES.put(struct, new Identifier(TomeOfTiamatha.MODID, name));
        return struct;
    }
    public static void init() {
        Registry.register(Registry.STRUCTURE_PROCESSOR, new Identifier(TomeOfTiamatha.MODID, "no_water_processor"), NO_WATER_PROCESSOR);
        STRUCTURES.keySet().forEach(structure -> StructureFeatureAccessor.callRegister(STRUCTURES.get(structure).toString(), structure, GenerationStep.Feature.SURFACE_STRUCTURES));
    }
}
