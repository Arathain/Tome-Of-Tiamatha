package net.arathain.tot.common.entity.spider;

import net.arathain.tot.common.entity.movement.IAdvancedPathfindingEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.Vector3d;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.List;


public interface IClimberEntity extends IAdvancedPathfindingEntity {
    public float getAttachmentOffset(Direction.Axis axis, float partialTicks);

    public float getVerticalOffset(float partialTicks);

    public Orientation getOrientation();

    public Orientation calculateOrientation(float partialTicks);

    public void setRenderOrientation(Orientation orientation);

    @Nullable
    public Orientation getRenderOrientation();

    public float getMovementSpeed();

    public Pair<Direction, Vec3d> getGroundDirection();

    public boolean shouldTrackPathingTargets();

    @Nullable
    public Vector3d getTrackedMovementTarget();

    @Nullable
    public List<PathingTarget> getTrackedPathingTargets();

    public boolean canClimbOnBlock(BlockState state, BlockPos pos);

    public boolean canAttachToSide(Direction side);

    public float getBlockSlipperiness(BlockPos pos);

    public boolean canClimberTriggerWalking();

    public boolean canClimbInWater();

    public void setCanClimbInWater(boolean value);

    public boolean canClimbInLava();

    public void setCanClimbInLava(boolean value);

    public float getCollisionsInclusionRange();

    public void setCollisionsInclusionRange(float range);

    public float getCollisionsSmoothingRange();

    public void setCollisionsSmoothingRange(float range);

    public void setJumpDirection(@Nullable Vector3d dir);
}
