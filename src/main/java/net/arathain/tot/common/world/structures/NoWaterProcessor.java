package net.arathain.tot.common.world.structures;

import com.mojang.serialization.Codec;
import net.arathain.tot.common.init.ToTStructures;
import net.minecraft.state.property.Properties;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nullable;

public class NoWaterProcessor extends StructureProcessor {
    public static final Codec<NoWaterProcessor> CODEC = Codec.unit(NoWaterProcessor::new);

    @Nullable
    @Override
    public Structure.StructureBlockInfo process(WorldView world, BlockPos pos, BlockPos pivot, Structure.StructureBlockInfo structureBlockInfoLocal, Structure.StructureBlockInfo structureBlockInfoWorld, StructurePlacementData data) {
        Chunk chunk = world.getChunk(structureBlockInfoWorld.pos);

        if (structureBlockInfoWorld.state.contains(Properties.WATERLOGGED) && !chunk.getFluidState(structureBlockInfoWorld.pos).isEmpty()) {
            chunk.setBlockState(structureBlockInfoWorld.pos, structureBlockInfoWorld.state.rotate(data.getRotation()).with(Properties.WATERLOGGED, false), false);
        }
        return structureBlockInfoWorld;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return ToTStructures.NOWATER_PROCESSOR;
    }
}
