package net.arathain.tot.common.entity.movement;

import net.arathain.tot.common.entity.spider.IClimberEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.ai.pathing.*;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class AdvancedGroundPathNavigation<T extends MobEntity & IClimberEntity> extends SpiderNavigation {
    protected AdvancedPathNodeNavigator pathNodeNavigator;
    protected long lastTimeUpdated;
    protected BlockPos targetPos;

    protected final T advancedPathFindingEntity;
    protected final boolean checkObstructions;

    protected int stuckCheckTicks = 0;

    protected int checkpointRange;

    public AdvancedGroundPathNavigation(T entity, World worldIn) {
        this(entity, worldIn, true);
    }

    public AdvancedGroundPathNavigation(T entity, World worldIn, boolean checkObstructions) {
        super(entity, worldIn);
        this.advancedPathFindingEntity = entity;
        this.checkObstructions = checkObstructions;

        if(this.nodeMaker instanceof AdvancedLandPathNodeMaker processor) {
            processor.setCheckObstructions(checkObstructions);
        }
    }


    public AdvancedPathNodeNavigator getAssignedPathNodeNavigator() {
        return this.pathNodeNavigator;
    }


    protected final PathNodeNavigator getPathNodeNavigator(float range) {
        this.pathNodeNavigator = this.createPathNodeNavigator(range);
        this.nodeMaker = this.pathNodeNavigator.getNodeMaker();
        return this.pathNodeNavigator;
    }


    protected AdvancedPathNodeNavigator createPathNodeNavigator(float maxExpansions) {
        AdvancedLandPathNodeMaker nodeMaker = new AdvancedLandPathNodeMaker();
        nodeMaker.setCanEnterOpenDoors(true);
        return new AdvancedPathNodeNavigator(nodeMaker, maxExpansions);
    }


    @Nullable
    @Override
    protected Path findPathTo(Set<BlockPos> positions, int range, boolean useHeadPos, int distance) {
        //Offset waypoints according to entity's size so that the lower AABB corner is at the offset waypoint and center is at the original waypoint
        Set<BlockPos> adjustedWaypoints = new HashSet<>();
        for(BlockPos pos : positions) {
            adjustedWaypoints.add(pos.add(-MathHelper.ceil(this.entity.getWidth()) + 1, -MathHelper.ceil(this.entity.getHeight()) + 1, -MathHelper.ceil(this.entity.getWidth()) + 1));
        }

        Path path = super.findPathTo(adjustedWaypoints, range, useHeadPos, checkpointRange);

        if(path != null && path.getTarget() != null) {
            this.checkpointRange = checkpointRange;
        }

        return path;
    }

    @Override
    protected void adjustPath() {
        if(this.world.getTime() - this.lastTimeUpdated > 20L) {
            if(this.targetPos != null) {
                this.currentPath = null;
                this.currentPath = this.findPathTo(this.targetPos, this.checkpointRange);
                this.lastTimeUpdated = this.world.getTime();
                this.shouldRecalculate = false;
            }
        } else {
            this.shouldRecalculate = true;
        }
    }

    public void updatePath() {
        if(this.world.getTime() - this.lastTimeUpdated > 20L) {
            if(this.targetPos != null) {
                this.currentPath = null;
                this.currentPath = this.findPathTo(this.targetPos, this.checkpointRange);
                this.lastTimeUpdated = this.world.getTime();
                this.shouldRecalculate = false;
            }
        } else {
            this.shouldRecalculate = true;
        }
    }

    @Override
    protected void checkTimeouts(Vec3d currentPos) {
        super.checkTimeouts(currentPos);
        if(this.checkObstructions && this.currentPath != null && !this.currentPath.isFinished()) {
            Vec3d target = this.currentPath.getNodePosition(this.advancedPathFindingEntity, Math.min(this.currentPath.getLength() - 1, this.currentPath.getCurrentNodeIndex()));
            Vec3d diff = target.subtract(currentPos);

            int axis = 0;
            double maxDiff = 0;
            for(int i = 0; i < 3; i++) {
                double d = switch (i) {
                    case 0 -> Math.abs(diff.x);
                    case 1 -> Math.abs(diff.y);
                    case 2 -> Math.abs(diff.z);
                    default -> throw new IllegalStateException("Unexpected value: " + i);
                };

                if(d > maxDiff) {
                    axis = i;
                    maxDiff = d;
                }
            }

            int height = MathHelper.floor(this.advancedPathFindingEntity.getHeight() + 1.0F);

            int ceilHalfWidth = MathHelper.ceil(this.advancedPathFindingEntity.getWidth() / 2.0f + 0.05F);

            Vec3d checkPos = switch (axis) {
                case 0 -> new Vec3d(currentPos.x + Math.signum(diff.x) * ceilHalfWidth, currentPos.y, target.z);
                case 1 -> new Vec3d(currentPos.x, currentPos.y + (diff.y > 0 ? (height + 1) : -1), target.z);
                case 2 -> new Vec3d(target.x, currentPos.y, currentPos.z + Math.signum(diff.z) * ceilHalfWidth);
                default -> throw new IllegalStateException("Unexpected value: " + axis);
            };

            Vec3d facingDiff = checkPos.subtract(currentPos.add(0, axis == 1 ? this.entity.getHeight() / 2 : 0, 0));
            Direction facing = Direction.getFacing((float)facingDiff.x, (float)facingDiff.y, (float)facingDiff.z);

            boolean blocked = false;

            Box checkBox = this.advancedPathFindingEntity.getBoundingBox().expand(Math.signum(diff.x) * 0.2D, Math.signum(diff.y) * 0.2D, Math.signum(diff.z) * 0.2D);

            loop: for(int yo = 0; yo < height; yo++) {
                for(int xzo = -ceilHalfWidth; xzo <= ceilHalfWidth; xzo++) {
                    BlockPos pos = new BlockPos(checkPos.x + (axis != 0 ? xzo : 0), checkPos.y + (axis != 1 ? yo : 0), checkPos.z + (axis != 2 ? xzo : 0));

                    BlockState state = this.advancedPathFindingEntity.world.getBlockState(pos);

                    PathNodeType nodeType = state.canPathfindThrough(this.advancedPathFindingEntity.world, pos, NavigationType.LAND) ? PathNodeType.OPEN : PathNodeType.BLOCKED;

                    if(nodeType == PathNodeType.BLOCKED) {
                        VoxelShape collisionShape = state.getCollisionShape(this.advancedPathFindingEntity.world, pos, ShapeContext.of(this.advancedPathFindingEntity)).offset(pos.getX(), pos.getY(), pos.getZ());

                        //TODO Use ILineConsumer
                        if(collisionShape != null && collisionShape.getBoundingBoxes().stream().anyMatch(aabb -> aabb.intersects(checkBox))) {
                            blocked = true;
                            break loop;
                        }
                    }
                }
            }

            if(blocked) {
                this.stuckCheckTicks++;

                if(this.stuckCheckTicks > this.advancedPathFindingEntity.getMaxStuckCheckTicks()) {
                    this.advancedPathFindingEntity.onPathingObstructed(facing);
                    this.stuckCheckTicks = 0;
                }
            } else {
                this.stuckCheckTicks = Math.max(this.stuckCheckTicks - 2, 0);
            }
        } else {
            this.stuckCheckTicks = Math.max(this.stuckCheckTicks - 4, 0);
        }
    }


}
