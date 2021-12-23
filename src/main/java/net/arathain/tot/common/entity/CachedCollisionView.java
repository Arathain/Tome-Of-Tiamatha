package net.arathain.tot.common.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.CollisionView;
import net.minecraft.world.border.WorldBorder;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class CachedCollisionView implements CollisionView {
    private final CollisionView collisionReader;
    private final BlockView[] blockReaderCache;
    private final int minChunkX, minChunkZ, width;

    public CachedCollisionView(CollisionView collisionReader, Box box) {
        this.collisionReader = collisionReader;

        this.minChunkX = ((MathHelper.floor(box.minX - 1.0E-7D) - 1) >> 4);
        int maxChunkX = ((MathHelper.floor(box.maxX + 1.0E-7D) + 1) >> 4);
        this.minChunkZ = ((MathHelper.floor(box.minZ - 1.0E-7D) - 1) >> 4);
        int maxChunkZ = ((MathHelper.floor(box.maxZ + 1.0E-7D) + 1) >> 4);

        this.width = maxChunkX - this.minChunkX + 1;
        int depth = maxChunkZ - this.minChunkZ + 1;

        BlockView[] blockReaderCache = new BlockView[width * depth];

        for(int cx = minChunkX; cx <= maxChunkX; cx++) {
            for(int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                blockReaderCache[(cx - minChunkX) + (cz - minChunkZ) * width] = collisionReader.getChunkAsView(cx, cz);
            }
        }

        this.blockReaderCache = blockReaderCache;
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        return this.collisionReader.getBlockEntity(pos);
    }

    @Override
    public <T extends BlockEntity> Optional<T> getBlockEntity(BlockPos pos, BlockEntityType<T> type) {
        return (Optional<T>) Optional.of(this.collisionReader.getBlockEntity(pos));
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return this.collisionReader.getBlockState(pos);
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return this.collisionReader.getFluidState(pos);
    }

    @Override
    public WorldBorder getWorldBorder() {
        return this.collisionReader.getWorldBorder();
    }

    @Nullable
    @Override
    public BlockView getChunkAsView(int chunkX, int chunkZ) {
        return this.blockReaderCache[(chunkX - minChunkX) + (chunkZ - minChunkZ) * width];
    }

    @Override
    public List<VoxelShape> getEntityCollisions(@Nullable Entity entity, Box box) {
        return this.collisionReader.getEntityCollisions(entity, box);
    }

    @Override
    public Iterable<VoxelShape> getCollisions(@Nullable Entity entity, Box box) {
        return this.collisionReader.getCollisions(entity, box);
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public int getBottomY() {
        return 0;
    }
}
