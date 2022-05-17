package net.arathain.tot.common.worldgen.structure.drider;

import com.mojang.serialization.Codec;
import net.arathain.tot.TomeOfTiamatha;
import net.minecraft.structure.PostPlacementProcessor;
import net.minecraft.structure.StructurePiecesGenerator;
import net.minecraft.structure.StructurePiecesGeneratorFactory;
import net.minecraft.structure.piece.PoolStructurePiece;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;

import java.util.Optional;

public class DriderDenStructure extends StructureFeature<StructurePoolFeatureConfig> {
    public static final Identifier START_POOL = new Identifier(TomeOfTiamatha.MODID, "den_start_pool");

    public DriderDenStructure(Codec<StructurePoolFeatureConfig> codec) {
        super(codec, DriderDenStructure::createPiecesGenerator, PostPlacementProcessor.EMPTY);
    }

    public static Optional<StructurePiecesGenerator<StructurePoolFeatureConfig>> createPiecesGenerator(StructurePiecesGeneratorFactory.Context<StructurePoolFeatureConfig> context) {
        // Turns the chunk coordinates into actual coordinates.
        int x = context.chunkPos().x << 4;
        int z = context.chunkPos().z << 4;

        // Position, set Y to 1 to offset height up.
        BlockPos blockPos = new BlockPos(x, 1, z);

        return DriderDenGenerator.generate(context, PoolStructurePiece::new, blockPos);
    }
}
