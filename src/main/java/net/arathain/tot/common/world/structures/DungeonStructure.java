package net.arathain.tot.common.world.structures;

import com.mojang.serialization.Codec;
import net.arathain.tot.TomeOfTiamatha;
import net.minecraft.structure.PostPlacementProcessor;
import net.minecraft.structure.StructurePiecesGenerator;
import net.minecraft.structure.StructurePiecesGeneratorFactory;
import net.minecraft.structure.piece.PoolStructurePiece;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;

import java.util.Optional;

public class DungeonStructure extends StructureFeature<StructurePoolFeatureConfig> {

    public static final Identifier START_POOL = new Identifier(TomeOfTiamatha.MODID, "start_pool");

    public DungeonStructure(Codec<StructurePoolFeatureConfig> codec) {
        super(codec, DungeonStructure::createPiecesGenerator, PostPlacementProcessor.EMPTY);
    }

    public static Optional<StructurePiecesGenerator<StructurePoolFeatureConfig>> createPiecesGenerator(StructurePiecesGeneratorFactory.Context<StructurePoolFeatureConfig> context) {
        // Turns the chunk coordinates into actual coordinates.
        int x = context.chunkPos().x << 4;
        int z = context.chunkPos().z << 4;

        ChunkGenerator chunkGenerator = context.chunkGenerator();
        int y = chunkGenerator.getHeightOnGround(x, z, Heightmap.Type.WORLD_SURFACE, context.heightLimitView());

        BlockPos blockPos = new BlockPos(x, y - 6, z);

        return DungeonGenerator.generate(context, PoolStructurePiece::new, blockPos);
    }
}
