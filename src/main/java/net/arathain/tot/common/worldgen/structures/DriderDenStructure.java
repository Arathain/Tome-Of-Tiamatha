package net.arathain.tot.common.worldgen.structures;

import com.mojang.serialization.Codec;
import net.arathain.tot.TomeOfTiamatha;
import net.minecraft.structure.StructureGeneratorFactory;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;

public class DriderDenStructure extends StructureFeature<StructurePoolFeatureConfig> {
    public DriderDenStructure(Codec<StructurePoolFeatureConfig> configCodec, StructureGeneratorFactory<StructurePoolFeatureConfig> piecesGenerator) {
        super(configCodec, piecesGenerator);
    }
   
}
