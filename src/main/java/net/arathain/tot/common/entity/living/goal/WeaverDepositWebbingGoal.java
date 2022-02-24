package net.arathain.tot.common.entity.living.goal;

import net.arathain.tot.common.entity.living.drider.weaver.WeaverEntity;
import net.arathain.tot.common.entity.living.drider.weaver.WebbingEntity;
import net.minecraft.block.Block;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.jetbrains.annotations.Nullable;

public class WeaverDepositWebbingGoal extends MoveToTargetPosGoal {
    private final WeaverEntity obj;
    private final Block targetBlock;
    public WeaverDepositWebbingGoal(WeaverEntity entity, Block block) {
        super(entity, 1, 96, 16);
        this.obj = entity;
        this.targetBlock = block;
    }
    @Override
    public boolean canStart() {
        return obj.hasPassengers() && this.hasAvailableTarget();
    }

    private boolean hasAvailableTarget() {
        if (this.targetPos != null && this.isTargetPos(this.mob.world, this.targetPos)) {
            return true;
        }
        return this.findTargetPos();
    }


    @Override
    public void tick() {
        if(obj.hasPassengers()) {
            BlockPos blockPos = this.obj.getBlockPos();
            BlockPos blockPos2 = this.tweakToProperPos(blockPos, obj.getWorld());
            if (this.hasReached() && blockPos2 != null) {
                obj.getFirstPassenger().setPos(getTargetPos().getX() + 0.5f, getTargetPos().getY() + 1, getTargetPos().getZ() + 0.5f);
                ((WebbingEntity) obj.getFirstPassenger()).setDeposited(true);
                obj.getFirstPassenger().dismountVehicle();
            }
            super.tick();
        }
    }
    private BlockPos tweakToProperPos(BlockPos pos, BlockView world) {
        BlockPos[] blockPoss;
        if (world.getBlockState(pos).isOf(this.targetBlock)) {
            return pos;
        }
        for (BlockPos blockPos : blockPoss = new BlockPos[]{pos.down(), pos.west(), pos.east(), pos.north(), pos.south(), pos.down().down()}) {
            if (!world.getBlockState(blockPos).isOf(this.targetBlock)) continue;
            return blockPos;
        }
        return null;
    }

    @Override
    public double getDesiredSquaredDistanceToTarget() {
        return 1;
    }

    @Override
    protected boolean isTargetPos(WorldView world, BlockPos pos) {
        Chunk chunk = world.getChunk(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()), ChunkStatus.FULL, false);
        if (chunk != null) {
            return chunk.getBlockState(pos).isOf(this.targetBlock) && chunk.getBlockState(pos.up()).isAir() && chunk.getBlockState(pos.up(2)).isAir();
        }
        return false;
    }
}
