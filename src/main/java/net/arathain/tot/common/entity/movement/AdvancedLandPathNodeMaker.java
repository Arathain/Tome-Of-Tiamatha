package net.arathain.tot.common.entity.movement;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.*;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.chunk.ChunkCache;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.function.BiPredicate;

public class AdvancedLandPathNodeMaker extends LandPathNodeMaker {
    protected static final PathNodeType[] PATH_NODE_TYPES = PathNodeType.values();
    protected static final Direction[] DIRECTIONS = Direction.values();

    protected static final Vec3i PX = new Vec3i(1, 0, 0);
    protected static final Vec3i NX = new Vec3i(-1, 0, 0);
    protected static final Vec3i PY = new Vec3i(0, 1, 0);
    protected static final Vec3i NY = new Vec3i(0, -1, 0);
    protected static final Vec3i PZ = new Vec3i(0, 0, 1);
    protected static final Vec3i NZ = new Vec3i(0, 0, -1);

    protected static final Vec3i PXPY = new Vec3i(1, 1, 0);
    protected static final Vec3i NXPY = new Vec3i(-1, 1, 0);
    protected static final Vec3i PXNY = new Vec3i(1, -1, 0);
    protected static final Vec3i NXNY = new Vec3i(-1, -1, 0);

    protected static final Vec3i PXPZ = new Vec3i(1, 0, 1);
    protected static final Vec3i NXPZ = new Vec3i(-1, 0, 1);
    protected static final Vec3i PXNZ = new Vec3i(1, 0, -1);
    protected static final Vec3i NXNZ = new Vec3i(-1, 0, -1);

    protected static final Vec3i PYPZ = new Vec3i(0, 1, 1);
    protected static final Vec3i NYPZ = new Vec3i(0, -1, 1);
    protected static final Vec3i PYNZ = new Vec3i(0, 1, -1);
    protected static final Vec3i NYNZ = new Vec3i(0, -1, -1);

    protected IAdvancedPathfindingEntity advancedPathFindingEntity;
    protected boolean startFromGround = true;
    protected boolean checkObstructions;
    protected int pathingSizeOffsetX, pathingSizeOffsetY, pathingSizeOffsetZ;
    protected EnumSet<Direction> pathableFacings = EnumSet.of(Direction.DOWN);
    protected Direction[] pathableFacingsArray;

    private final Long2LongMap pathNodeTypeCache = new Long2LongOpenHashMap();
    private final Long2ObjectMap<PathNodeType> rawPathNodeTypeCache = new Long2ObjectOpenHashMap<>();
    private final Object2BooleanMap<EntityDimensions> boxCollisionCache = new Object2BooleanOpenHashMap<>();

    protected boolean alwaysAllowDiagonals = true;

    public void setStartPathOnGround(boolean startFromGround) {
        this.startFromGround = startFromGround;
    }

    public void setCheckObstructions(boolean checkObstructions) {
        this.checkObstructions = checkObstructions;
    }

    public void setCanPathWalls(boolean canPathWalls) {
        if(canPathWalls) {
            this.pathableFacings.add(Direction.NORTH);
            this.pathableFacings.add(Direction.EAST);
            this.pathableFacings.add(Direction.SOUTH);
            this.pathableFacings.add(Direction.WEST);
        } else {
            this.pathableFacings.remove(Direction.NORTH);
            this.pathableFacings.remove(Direction.EAST);
            this.pathableFacings.remove(Direction.SOUTH);
            this.pathableFacings.remove(Direction.WEST);
        }
    }

    public void setCanPathCeiling(boolean canPathCeiling) {
        if(canPathCeiling) {
            this.pathableFacings.add(Direction.UP);
        } else {
            this.pathableFacings.remove(Direction.UP);
        }
    }

    @Override
    public void init(ChunkCache sourceIn, MobEntity mob) {
        super.init(sourceIn, mob);

        if(mob instanceof IAdvancedPathfindingEntity) {
            this.advancedPathFindingEntity = (IAdvancedPathfindingEntity) mob;
        } else {
            throw new IllegalArgumentException("Only mobs that extend " + IAdvancedPathfindingEntity.class.getSimpleName() + " are supported. Received: " + mob.getClass().getName());
        }

        this.pathingSizeOffsetX = Math.max(1, MathHelper.floor(this.entity.getWidth() / 2.0f + 1));
        this.pathingSizeOffsetY = Math.max(1, MathHelper.floor(this.entity.getHeight() + 1));
        this.pathingSizeOffsetZ = Math.max(1, MathHelper.floor(this.entity.getWidth() / 2.0f + 1));

        this.pathableFacingsArray = this.pathableFacings.toArray(new Direction[0]);
    }

    @Override
    public void clear() {
        super.clear();
        this.pathNodeTypeCache.clear();
        this.rawPathNodeTypeCache.clear();
        this.boxCollisionCache.clear();
        this.advancedPathFindingEntity.pathFinderCleanup();
    }

    private boolean checkBoxCollision(Box box) {
        return this.boxCollisionCache.computeIfAbsent(this.entity.getDimensions(this.entity.getPose()), (p_237237_2_) -> !this.cachedWorld.canCollide(this.entity, box));
    }

    @Override
    public PathNode getStart() {
        double x = this.entity.getX();
        double y = this.entity.getY();
        double z = this.entity.getZ();

        BlockPos.Mutable checkPos = new BlockPos.Mutable();

        int by = MathHelper.floor(y);

        BlockState state = this.cachedWorld.getBlockState(checkPos.set(x, by, z));

        if(!this.entity.canWalkOnFluid(state.getFluidState().getFluid())) {
            if(this.canSwim() && this.entity.isInsideWaterOrBubbleColumn()) {
                while(true) {
                    if(state.getBlock() != Blocks.WATER && state.getFluidState() != Fluids.WATER.getStill(false)) {
                        --by;
                        break;
                    }

                    ++by;
                    state = this.cachedWorld.getBlockState(checkPos.set(x, by, z));
                }
            } else if(this.entity.isOnGround() || !this.startFromGround) {
                by = MathHelper.floor(y + Math.min(0.5D, Math.max(this.entity.getHeight() - 0.1f, 0.0D)));
            } else {
                BlockPos blockpos;
                for(blockpos = this.entity.getBlockPos(); (this.cachedWorld.getBlockState(blockpos).isAir() || this.cachedWorld.getBlockState(blockpos).canPathfindThrough(this.cachedWorld, blockpos, NavigationType.LAND)) && blockpos.getY() > 0; blockpos = blockpos.down()) { }

                by = blockpos.up().getY();
            }
        } else {
            while(this.entity.canWalkOnFluid(state.getFluidState().getFluid())) {
                ++by;
                state = this.cachedWorld.getBlockState(checkPos.set(x, by, z));
            }

            --by;
        }

        final BlockPos initialStartPos = new BlockPos(x, by, z);
        BlockPos startPos = initialStartPos;

        long packed = this.removeNonStartingSides(this.getDirectionalPathNodeTypeCached(this.entity, startPos.getX(), startPos.getY(), startPos.getZ()));
        DirectionalPathNode startPathNode = this.openPoint(startPos.getX(), startPos.getY(), startPos.getZ(), packed, false);
        startPathNode.type = unpackNodeType(packed);
        startPathNode.penalty = this.entity.getPathfindingPenalty(startPathNode.type);

        startPos = this.findSuitableStartingPosition(startPos, startPathNode);

        if(!initialStartPos.equals(startPos)) {
            packed = this.removeNonStartingSides(this.getDirectionalPathNodeTypeCached(this.entity, startPos.getX(), startPos.getY(), startPos.getZ()));
            startPathNode = this.openPoint(startPos.getX(), startPos.getY(), startPos.getZ(), packed, false);
            startPathNode.type = unpackNodeType(packed);
            startPathNode.penalty = this.entity.getPathfindingPenalty(startPathNode.type);
        }

        if(this.entity.getPathfindingPenalty(startPathNode.type) < 0.0F) {
            Box box = this.entity.getBoundingBox();

            if(this.isSafeStartingPosition(checkPos.set(box.minX, by, box.minZ)) || this.isSafeStartingPosition(checkPos.set(box.minX, by, box.maxZ)) || this.isSafeStartingPosition(checkPos.set(box.maxX, by, box.minZ)) || this.isSafeStartingPosition(checkPos.set(box.maxX, by, box.maxZ))) {
                packed = this.removeNonStartingSides(this.getDirectionalPathNodeTypeCached(this.entity, checkPos.getX(), checkPos.getY(), checkPos.getZ()));
                startPathNode = this.openPoint(checkPos.getX(), checkPos.getY(), checkPos.getZ(), packed, false);
                startPathNode.type = unpackNodeType(packed);
                startPathNode.penalty = this.entity.getPathfindingPenalty(startPathNode.type);
            }
        }

        return startPathNode;
    }

    private long removeNonStartingSides(long packed) {
        long newPacked = packed & ~0xFFFFFFFFL;

        for(Direction side : DIRECTIONS) {
            if(unpackDirection(side, packed) && this.isValidStartingSide(side)) {
                newPacked = packDirection(side, newPacked);
            }
        }

        return newPacked;
    }

    protected boolean isValidStartingSide(Direction side) {
        Direction groundSide = this.advancedPathFindingEntity.getGroundSide();
        return side == groundSide || side.getAxis() != groundSide.getAxis();
    }

    protected BlockPos findSuitableStartingPosition(BlockPos pos, DirectionalPathNode startPathNode) {
        if(startPathNode.getPathableSides().length == 0) {
            Direction avoidedOffset = this.advancedPathFindingEntity.getGroundSide().getOpposite();

            for(int xo = -1; xo <= 1; xo++) {
                for(int yo = -1; yo <= 1; yo++) {
                    for(int zo = -1; zo <= 1; zo++) {
                        if(xo != avoidedOffset.getOffsetX() && yo != avoidedOffset.getOffsetY() && zo != avoidedOffset.getOffsetZ()) {
                            BlockPos offsetPos = pos.add(xo, yo, zo);

                            long packed = this.getDirectionalPathNodeTypeCached(this.entity, offsetPos.getX(), offsetPos.getY(), offsetPos.getZ());
                            PathNodeType nodeType = unpackNodeType(packed);

                            if(nodeType == PathNodeType.WALKABLE && unpackDirection(packed)) {
                                return offsetPos;
                            }
                        }
                    }
                }
            }
        }

        return pos;
    }

    private boolean isSafeStartingPosition(BlockPos pos) {
        PathNodeType pathnodetype = unpackNodeType(this.getDirectionalPathNodeTypeCached(this.entity, pos.getX(), pos.getY(), pos.getZ()));
        return this.entity.getPathfindingPenalty(pathnodetype) >= 0.0F;
    }

    private boolean allowDiagonalPathOptions(PathNode[] options) {
        return this.alwaysAllowDiagonals || options == null || options.length == 0 || ((options[0] == null || options[0].type == PathNodeType.OPEN || options[0].penalty != 0.0F) && (options.length <= 1 || (options[1] == null || options[1].type == PathNodeType.OPEN || options[1].penalty != 0.0F)));
    }

    @Override
    public int getSuccessors(PathNode[] pathOptions, PathNode currentPointIn) {
        DirectionalPathNode currentPoint;
        if(currentPointIn instanceof DirectionalPathNode) {
            currentPoint = (DirectionalPathNode) currentPointIn;
        } else {
            currentPoint = new DirectionalPathNode(currentPointIn);
        }

        int openedNodeCount = 0;
        int stepHeight = 0;

        PathNodeType nodeTypeAbove = unpackNodeType(this.getDirectionalPathNodeTypeCached(this.entity, currentPoint.x, currentPoint.y + 1, currentPoint.z));

        if(this.entity.getPathfindingPenalty(nodeTypeAbove) >= 0.0F) {
            stepHeight = MathHelper.floor(Math.max(1.0F, this.entity.stepHeight));
        }

        double height = currentPoint.y - getFeetY(this.cachedWorld, new BlockPos(currentPoint.x, currentPoint.y, currentPoint.z));

        DirectionalPathNode[] pathsPZ = this.getSafePoints(currentPoint.x, currentPoint.y, currentPoint.z + 1, stepHeight, height, PZ, this.checkObstructions);
        DirectionalPathNode[] pathsNX = this.getSafePoints(currentPoint.x - 1, currentPoint.y, currentPoint.z, stepHeight, height, NX, this.checkObstructions);
        DirectionalPathNode[] pathsPX = this.getSafePoints(currentPoint.x + 1, currentPoint.y, currentPoint.z, stepHeight, height, PX, this.checkObstructions);
        DirectionalPathNode[] pathsNZ = this.getSafePoints(currentPoint.x, currentPoint.y, currentPoint.z - 1, stepHeight, height, NZ, this.checkObstructions);

        for (DirectionalPathNode directionalPathNode : pathsPZ) {
            if (this.isSuitablePoint(directionalPathNode, currentPoint, this.checkObstructions)) {
                pathOptions[openedNodeCount++] = directionalPathNode;
            }
        }

        for (DirectionalPathNode nx : pathsNX) {
            if (this.isSuitablePoint(nx, currentPoint, this.checkObstructions)) {
                pathOptions[openedNodeCount++] = nx;
            }
        }

        for (DirectionalPathNode px : pathsPX) {
            if (this.isSuitablePoint(px, currentPoint, this.checkObstructions)) {
                pathOptions[openedNodeCount++] = px;
            }
        }

        for (DirectionalPathNode directionalPathNode : pathsNZ) {
            if (this.isSuitablePoint(directionalPathNode, currentPoint, this.checkObstructions)) {
                pathOptions[openedNodeCount++] = directionalPathNode;
            }
        }

        DirectionalPathNode[] pathsNY = null;
        if(this.checkObstructions || this.pathableFacings.size() > 1) {
            pathsNY = this.getSafePoints(currentPoint.x, currentPoint.y - 1, currentPoint.z, stepHeight, height, NY, this.checkObstructions);

            for (DirectionalPathNode directionalPathNode : pathsNY) {
                if (this.isSuitablePoint(directionalPathNode, currentPoint, this.checkObstructions)) {
                    pathOptions[openedNodeCount++] = directionalPathNode;
                }
            }
        }

        DirectionalPathNode[] pathsPY = null;
        if(this.pathableFacings.size() > 1) {
            pathsPY = this.getSafePoints(currentPoint.x, currentPoint.y + 1, currentPoint.z, stepHeight, height, PY, this.checkObstructions);

            for (DirectionalPathNode directionalPathNode : pathsPY) {
                if (this.isSuitablePoint(directionalPathNode, currentPoint, this.checkObstructions)) {
                    pathOptions[openedNodeCount++] = directionalPathNode;
                }
            }
        }

        boolean allowDiagonalNZ = this.allowDiagonalPathOptions(pathsNZ);
        boolean allowDiagonalPZ = this.allowDiagonalPathOptions(pathsPZ);
        boolean allowDiagonalPX = this.allowDiagonalPathOptions(pathsPX);
        boolean allowDiagonalNX = this.allowDiagonalPathOptions(pathsNX);

        boolean fitsThroughPoles = this.entity.getWidth() < 0.5f;

        boolean is3DPathing = this.pathableFacings.size() >= 3;

        if(allowDiagonalNZ && allowDiagonalNX) {
            DirectionalPathNode[] pathsNXNZ = this.getSafePoints(currentPoint.x - this.entityBlockXSize, currentPoint.y, currentPoint.z - 1, stepHeight, height, NXNZ, this.checkObstructions);

            boolean foundDiagonal = false;

            for (DirectionalPathNode directionalPathNode : pathsNXNZ) {
                if (this.isSuitablePoint(pathsNX, currentPoint.x - 1, currentPoint.y, currentPoint.z, pathsNZ, currentPoint.x, currentPoint.y, currentPoint.z - 1, directionalPathNode, currentPoint, this.checkObstructions, fitsThroughPoles, is3DPathing)) {
                    pathOptions[openedNodeCount++] = directionalPathNode;
                    foundDiagonal = true;
                }
            }

            if(!foundDiagonal && (this.entityBlockXSize != 1 || this.entityBlockZSize != 1)) {
                pathsNXNZ = this.getSafePoints(currentPoint.x - 1, currentPoint.y, currentPoint.z - this.entityBlockZSize, stepHeight, height, NXNZ, this.checkObstructions);

                for (DirectionalPathNode directionalPathNode : pathsNXNZ) {
                    if (this.isSuitablePoint(pathsNX, currentPoint.x - 1, currentPoint.y, currentPoint.z, pathsNZ, currentPoint.x, currentPoint.y, currentPoint.z - 1, directionalPathNode, currentPoint, this.checkObstructions, fitsThroughPoles, is3DPathing)) {
                        pathOptions[openedNodeCount++] = directionalPathNode;
                    }
                }
            }
        }

        if(allowDiagonalNZ && allowDiagonalPX) {
            DirectionalPathNode[] pathsPXNZ = this.getSafePoints(currentPoint.x + 1, currentPoint.y, currentPoint.z - 1, stepHeight, height, PXNZ, this.checkObstructions);

            for (DirectionalPathNode directionalPathNode : pathsPXNZ) {
                if (this.isSuitablePoint(pathsPX, currentPoint.x + 1, currentPoint.y, currentPoint.z, pathsNZ, currentPoint.x, currentPoint.y, currentPoint.z - 1, directionalPathNode, currentPoint, this.checkObstructions, fitsThroughPoles, is3DPathing)) {
                    pathOptions[openedNodeCount++] = directionalPathNode;
                }
            }
        }

        if(allowDiagonalPZ && allowDiagonalNX) {
            DirectionalPathNode[] pathsNXPZ = this.getSafePoints(currentPoint.x - 1, currentPoint.y, currentPoint.z + 1, stepHeight, height, NXPZ, this.checkObstructions);

            for (DirectionalPathNode directionalPathNode : pathsNXPZ) {
                if (this.isSuitablePoint(pathsNX, currentPoint.x - 1, currentPoint.y, currentPoint.z, pathsPZ, currentPoint.x, currentPoint.y, currentPoint.z + 1, directionalPathNode, currentPoint, this.checkObstructions, fitsThroughPoles, is3DPathing)) {
                    pathOptions[openedNodeCount++] = directionalPathNode;
                }
            }
        }

        if(allowDiagonalPZ && allowDiagonalPX) {
            DirectionalPathNode[] pathsPXPZ = this.getSafePoints(currentPoint.x + this.entityBlockXSize, currentPoint.y, currentPoint.z + 1, stepHeight, height, PXPZ, this.checkObstructions);

            boolean foundDiagonal = false;

            for (DirectionalPathNode directionalPathNode : pathsPXPZ) {
                if (this.isSuitablePoint(pathsPX, currentPoint.x + 1, currentPoint.y, currentPoint.z, pathsPZ, currentPoint.x, currentPoint.y, currentPoint.z + 1, directionalPathNode, currentPoint, this.checkObstructions, fitsThroughPoles, is3DPathing)) {
                    pathOptions[openedNodeCount++] = directionalPathNode;
                    foundDiagonal = true;
                }
            }

            if(!foundDiagonal && (this.entityBlockXSize != 1 || this.entityBlockZSize != 1)) {
                pathsPXPZ = this.getSafePoints(currentPoint.x + 1, currentPoint.y, currentPoint.z + this.entityBlockZSize, stepHeight, height, PXPZ, this.checkObstructions);

                for (DirectionalPathNode directionalPathNode : pathsPXPZ) {
                    if (this.isSuitablePoint(pathsPX, currentPoint.x + 1, currentPoint.y, currentPoint.z, pathsPZ, currentPoint.x, currentPoint.y, currentPoint.z + 1, directionalPathNode, currentPoint, this.checkObstructions, fitsThroughPoles, is3DPathing)) {
                        pathOptions[openedNodeCount++] = directionalPathNode;
                    }
                }
            }
        }

        if(this.pathableFacings.size() > 1) {
            boolean allowDiagonalPY = this.allowDiagonalPathOptions(pathsPY);
            boolean allowDiagonalNY = this.allowDiagonalPathOptions(pathsNY);

            if(allowDiagonalNY && allowDiagonalNX) {
                DirectionalPathNode[] pathsNYNX = this.getSafePoints(currentPoint.x - this.entityBlockXSize, currentPoint.y - 1, currentPoint.z, stepHeight, height, NXNY, this.checkObstructions);

                boolean foundDiagonal = false;

                for (DirectionalPathNode nynx : pathsNYNX) {
                    if (this.isSuitablePoint(pathsNY, currentPoint.x, currentPoint.y - 1, currentPoint.z, pathsNX, currentPoint.x - 1, currentPoint.y, currentPoint.z, nynx, currentPoint, this.checkObstructions, fitsThroughPoles, is3DPathing)) {
                        pathOptions[openedNodeCount++] = nynx;
                        foundDiagonal = true;
                    }
                }

                if(!foundDiagonal && (this.entityBlockXSize != 1 || this.entityBlockYSize != 1)) {
                    pathsNYNX = this.getSafePoints(currentPoint.x - 1, currentPoint.y - this.entityBlockYSize, currentPoint.z, stepHeight, height, NXNY, this.checkObstructions);

                    for (DirectionalPathNode nynx : pathsNYNX) {
                        if (this.isSuitablePoint(pathsNY, currentPoint.x, currentPoint.y - 1, currentPoint.z, pathsNX, currentPoint.x - 1, currentPoint.y, currentPoint.z, nynx, currentPoint, this.checkObstructions, fitsThroughPoles, is3DPathing)) {
                            pathOptions[openedNodeCount++] = nynx;
                        }
                    }
                }
            }

            if(allowDiagonalNY && allowDiagonalPX) {
                DirectionalPathNode[] pathsNYPX = this.getSafePoints(currentPoint.x + 1, currentPoint.y - 1, currentPoint.z, stepHeight, height, PXNY, this.checkObstructions);

                for(int k = 0; k < pathsNYPX.length; k++) {
                    if(this.isSuitablePoint(pathsNY, currentPoint.x, currentPoint.y - 1, currentPoint.z, pathsPX, currentPoint.x + 1, currentPoint.y, currentPoint.z, pathsNYPX[k], currentPoint, this.checkObstructions, fitsThroughPoles, is3DPathing)) {
                        pathOptions[openedNodeCount++] = pathsNYPX[k];
                    }
                }
            }

            if(allowDiagonalNY && allowDiagonalNZ) {
                DirectionalPathNode[] pathsNYNZ = this.getSafePoints(currentPoint.x, currentPoint.y - this.entityBlockYSize, currentPoint.z - 1, stepHeight, height, NYNZ, this.checkObstructions);

                boolean foundDiagonal = false;

                for(int k = 0; k < pathsNYNZ.length; k++) {
                    if(this.isSuitablePoint(pathsNY, currentPoint.x, currentPoint.y - 1, currentPoint.z, pathsNZ, currentPoint.x, currentPoint.y, currentPoint.z - 1, pathsNYNZ[k], currentPoint, this.checkObstructions, fitsThroughPoles, is3DPathing)) {
                        pathOptions[openedNodeCount++] = pathsNYNZ[k];
                        foundDiagonal = true;
                    }
                }

                if(!foundDiagonal && (this.entityBlockYSize != 1 || this.entityBlockZSize != 1)) {
                    pathsNYNZ = this.getSafePoints(currentPoint.x, currentPoint.y - 1, currentPoint.z - this.entityBlockZSize, stepHeight, height, NYNZ, this.checkObstructions);

                    for(int k = 0; k < pathsNYNZ.length; k++) {
                        if(this.isSuitablePoint(pathsNY, currentPoint.x, currentPoint.y - 1, currentPoint.z, pathsNZ, currentPoint.x, currentPoint.y, currentPoint.z - 1, pathsNYNZ[k], currentPoint, this.checkObstructions, fitsThroughPoles, is3DPathing)) {
                            pathOptions[openedNodeCount++] = pathsNYNZ[k];
                        }
                    }
                }
            }

            if(allowDiagonalNY && allowDiagonalPZ) {
                DirectionalPathNode[] pathsNYPZ = this.getSafePoints(currentPoint.x, currentPoint.y - 1, currentPoint.z + 1, stepHeight, height, NYPZ, this.checkObstructions);

                for(int k = 0; k < pathsNYPZ.length; k++) {
                    if(this.isSuitablePoint(pathsNY, currentPoint.x, currentPoint.y - 1, currentPoint.z, pathsPZ, currentPoint.x, currentPoint.y, currentPoint.z + 1, pathsNYPZ[k], currentPoint, this.checkObstructions, fitsThroughPoles, is3DPathing)) {
                        pathOptions[openedNodeCount++] = pathsNYPZ[k];
                    }
                }
            }

            if(allowDiagonalPY && allowDiagonalNX) {
                DirectionalPathNode[] pathsPYNX = this.getSafePoints(currentPoint.x - 1, currentPoint.y + 1, currentPoint.z, stepHeight, height, NXPY, this.checkObstructions);

                for(int k = 0; k < pathsPYNX.length; k++) {
                    if(this.isSuitablePoint(pathsPY, currentPoint.x, currentPoint.y + 1, currentPoint.z, pathsNZ, currentPoint.x - 1, currentPoint.y, currentPoint.z, pathsPYNX[k], currentPoint, this.checkObstructions, fitsThroughPoles, is3DPathing)) {
                        pathOptions[openedNodeCount++] = pathsPYNX[k];
                    }
                }
            }

            if(allowDiagonalPY && allowDiagonalPX) {
                DirectionalPathNode[] pathsPYPX = this.getSafePoints(currentPoint.x + this.entityBlockXSize, currentPoint.y + 1, currentPoint.z, stepHeight, height, PXPY, this.checkObstructions);

                boolean foundDiagonal = false;

                for(int k = 0; k < pathsPYPX.length; k++) {
                    if(this.isSuitablePoint(pathsPY, currentPoint.x, currentPoint.y + 1, currentPoint.z, pathsPX, currentPoint.x + 1, currentPoint.y, currentPoint.z, pathsPYPX[k], currentPoint, this.checkObstructions, fitsThroughPoles, is3DPathing)) {
                        pathOptions[openedNodeCount++] = pathsPYPX[k];
                        foundDiagonal = true;
                    }
                }

                if(!foundDiagonal && (this.entityBlockXSize != 1 || this.entityBlockYSize != 1)) {
                    pathsPYPX = this.getSafePoints(currentPoint.x + 1, currentPoint.y + this.entityBlockYSize, currentPoint.z, stepHeight, height, PXPY, this.checkObstructions);

                    for(int k = 0; k < pathsPYPX.length; k++) {
                        if(this.isSuitablePoint(pathsPY, currentPoint.x, currentPoint.y + 1, currentPoint.z, pathsPX, currentPoint.x + 1, currentPoint.y, currentPoint.z, pathsPYPX[k], currentPoint, this.checkObstructions, fitsThroughPoles, is3DPathing)) {
                            pathOptions[openedNodeCount++] = pathsPYPX[k];
                        }
                    }
                }
            }

            if(allowDiagonalPY && allowDiagonalNZ) {
                DirectionalPathNode[] pathsPYNZ = this.getSafePoints(currentPoint.x, currentPoint.y + 1, currentPoint.z - 1, stepHeight, height, PYNZ, this.checkObstructions);

                for(int k = 0; k < pathsPYNZ.length; k++) {
                    if(this.isSuitablePoint(pathsPY, currentPoint.x, currentPoint.y + 1, currentPoint.z, pathsNZ, currentPoint.x, currentPoint.y, currentPoint.z - 1, pathsPYNZ[k], currentPoint, this.checkObstructions, fitsThroughPoles, is3DPathing)) {
                        pathOptions[openedNodeCount++] = pathsPYNZ[k];
                    }
                }
            }

            if(allowDiagonalPY && allowDiagonalPZ) {
                DirectionalPathNode[] pathsPYPZ = this.getSafePoints(currentPoint.x, currentPoint.y + this.entityBlockYSize, currentPoint.z + 1, stepHeight, height, PYPZ, this.checkObstructions);

                boolean foundDiagonal = false;

                for(int k = 0; k < pathsPYPZ.length; k++) {
                    if(this.isSuitablePoint(pathsPY, currentPoint.x, currentPoint.y + 1, currentPoint.z, pathsPZ, currentPoint.x, currentPoint.y, currentPoint.z + 1, pathsPYPZ[k], currentPoint, this.checkObstructions, fitsThroughPoles, is3DPathing)) {
                        pathOptions[openedNodeCount++] = pathsPYPZ[k];
                        foundDiagonal = true;
                    }
                }

                if(!foundDiagonal && (this.entityBlockYSize != 1 || this.entityBlockZSize!= 1)) {
                    pathsPYPZ = this.getSafePoints(currentPoint.x, currentPoint.y + 1, currentPoint.z + this.entityBlockZSize, stepHeight, height, PYPZ, this.checkObstructions);

                    for(int k = 0; k < pathsPYPZ.length; k++) {
                        if(this.isSuitablePoint(pathsPY, currentPoint.x, currentPoint.y + 1, currentPoint.z, pathsPZ, currentPoint.x, currentPoint.y, currentPoint.z + 1, pathsPYPZ[k], currentPoint, this.checkObstructions, fitsThroughPoles, is3DPathing)) {
                            pathOptions[openedNodeCount++] = pathsPYPZ[k];
                        }
                    }
                }
            }
        }

        return openedNodeCount;
    }

    protected boolean isTraversible(DirectionalPathNode from, DirectionalPathNode to) {
        if(this.canSwim() && (from.type == PathNodeType.WATER || from.type == PathNodeType.WATER_BORDER || from.type == PathNodeType.LAVA || to.type == PathNodeType.WATER || to.type == PathNodeType.WATER_BORDER || to.type == PathNodeType.LAVA)) {
            //When swimming it can always reach any side
            return true;
        }

        boolean dx = (to.x - from.x) != 0;
        boolean dy = (to.y - from.y) != 0;
        boolean dz = (to.z - from.z) != 0;

        boolean isDiagonal = (dx ? 1 : 0) + (dy ? 1 : 0) + (dz ? 1 : 0) > 1;

        Direction[] fromDirections = from.getPathableSides();
        Direction[] toDirections = to.getPathableSides();

        for(int i = 0; i < fromDirections.length; i++) {
            Direction d1 = fromDirections[i];

            for(int j = 0; j < toDirections.length; j++) {
                Direction d2 = toDirections[j];

                if(d1 == d2) {
                    return true;
                } else if(isDiagonal) {
                    Direction.Axis a1 = d1.getAxis();
                    Direction.Axis a2 = d2.getAxis();

                    if((a1 == Direction.Axis.X && a2 == Direction.Axis.Y) || (a1 == Direction.Axis.Y && a2 == Direction.Axis.X)) {
                        return !dz;
                    } else if((a1 == Direction.Axis.X && a2 == Direction.Axis.Z) || (a1 == Direction.Axis.Z && a2 == Direction.Axis.X)) {
                        return !dy;
                    } else if((a1 == Direction.Axis.Z && a2 == Direction.Axis.Y) || (a1 == Direction.Axis.Y && a2 == Direction.Axis.Z)) {
                        return !dx;
                    }
                }
            }
        }

        return false;
    }

    protected static boolean isSharingDirection(DirectionalPathNode from, DirectionalPathNode to) {
        Direction[] fromDirections = from.getPathableSides();
        Direction[] toDirections = to.getPathableSides();

        for(int i = 0; i < fromDirections.length; i++) {
            Direction d1 = fromDirections[i];

            for(int j = 0; j < toDirections.length; j++) {
                Direction d2 = toDirections[j];

                if(d1 == d2) {
                    return true;
                }
            }
        }

        return false;
    }

    protected boolean isSuitablePoint(@Nullable DirectionalPathNode newPoint, DirectionalPathNode currentPoint, boolean allowObstructions) {
        return newPoint != null && !newPoint.visited && (allowObstructions || newPoint.penalty >= 0.0F || currentPoint.penalty < 0.0F) && this.isTraversible(currentPoint, newPoint);
    }

    protected boolean isSuitablePoint(@Nullable DirectionalPathNode[] newPoints1, int np1x, int np1y, int np1z, @Nullable DirectionalPathNode[] newPoints2, int np2x, int np2y, int np2z, @Nullable DirectionalPathNode newPointDiagonal, DirectionalPathNode currentPoint, boolean allowObstructions, boolean fitsThroughPoles, boolean is3DPathing) {
        if(!is3DPathing) {
            if(newPointDiagonal != null && !newPointDiagonal.visited && newPoints2 != null && newPoints2.length > 0 && (newPoints2[0] != null || (newPoints2.length > 1 && newPoints2[1] != null)) && newPoints1 != null && newPoints1.length > 0 && (newPoints1[0] != null || (newPoints1.length > 1 && newPoints1[1] != null))) {
                if((newPoints1[0] == null || newPoints1[0].type != PathNodeType.WALKABLE_DOOR) && (newPoints2[0] == null || newPoints2[0].type != PathNodeType.WALKABLE_DOOR) && newPointDiagonal.type != PathNodeType.WALKABLE_DOOR) {
                    boolean canPassPoleDiagonally = newPoints2[0] != null && newPoints2[0].type == PathNodeType.FENCE && newPoints1[0] != null && newPoints1[0].type == PathNodeType.FENCE && fitsThroughPoles;
                    return (allowObstructions || newPointDiagonal.penalty >= 0.0F) &&
                            (canPassPoleDiagonally || (
                                    ((newPoints2[0] != null && (allowObstructions || newPoints2[0].penalty >= 0.0F)) || (newPoints2.length > 1 && newPoints2[1] != null && (allowObstructions || newPoints2[1].penalty >= 0.0F))) &&
                                            ((newPoints1[0] != null && (allowObstructions || newPoints1[0].penalty >= 0.0F)) || (newPoints1.length > 1 && newPoints1[1] != null && (allowObstructions || newPoints1[1].penalty >= 0.0F)))
                            ));
                }
            }
        } else {
            if(newPointDiagonal != null && !newPointDiagonal.visited && this.isTraversible(currentPoint, newPointDiagonal)) {
                long packed2 = this.getDirectionalPathNodeTypeCached(this.entity, np2x, np2y, np2z);
                PathNodeType pathNodeType2 = unpackNodeType(packed2);
                boolean open2 = (pathNodeType2 == PathNodeType.OPEN || pathNodeType2 == PathNodeType.WALKABLE);

                long packed1 = this.getDirectionalPathNodeTypeCached(this.entity, np1x, np1y, np1z);
                PathNodeType pathNodeType1 = unpackNodeType(packed1);
                boolean open1 = (pathNodeType1 == PathNodeType.OPEN || pathNodeType1 == PathNodeType.WALKABLE);

                return (open1 != open2) || (open1 == true && open2 == true && isSharingDirection(newPointDiagonal, currentPoint));
            }
        }

        return false;

    }

    protected DirectionalPathNode openPoint(int x, int y, int z, long packed, boolean isDrop) {
        int hash = PathNode.hash(x, y, z);

        PathNode point = this.pathNodeCache.computeIfAbsent(hash, (key) -> {
            return new DirectionalPathNode(x, y, z, packed, isDrop);
        });

        if(point instanceof DirectionalPathNode == false) {
            point = new DirectionalPathNode(point);
            this.pathNodeCache.put(hash, point);
        }

        return (DirectionalPathNode) point;
    }

    @Nullable
    private DirectionalPathNode[] getSafePoints(int x, int y, int z, int stepHeight, double height, Vec3i direction, boolean allowBlocked) {
        DirectionalPathNode directPathNode = null;

        BlockPos pos = new BlockPos(x, y, z);

        double blockHeight = y - getFeetY(this.cachedWorld, new BlockPos(x, y, z));

        if (blockHeight - height > 1.125D) {
            return new DirectionalPathNode[0];
        } else {
            final long initialPacked = this.getDirectionalPathNodeTypeCached(this.entity, x, y, z);
            long packed = initialPacked;
            PathNodeType nodeType = unpackNodeType(packed);

            float malus = this.advancedPathFindingEntity.getPathingPenalty((WorldAccess) this.cachedWorld, this.entity, nodeType, pos, direction, dir -> unpackDirection(dir, initialPacked)); //Replaces EntityLiving#getPathfindingPenalty

            double halfWidth = (double)this.entity.getWidth() / 2.0D;

            DirectionalPathNode[] result = new DirectionalPathNode[1];

            if(malus >= 0.0F && (allowBlocked || nodeType != PathNodeType.BLOCKED)) {
                directPathNode = this.openPoint(x, y, z, packed, false);
                directPathNode.type = nodeType;
                directPathNode.penalty = Math.max(directPathNode.penalty, malus);

                //Allow other nodes than this obstructed node to also be considered, otherwise jumping/pathing up steps does no longer work
                if(directPathNode.type == PathNodeType.BLOCKED) {
                    result = new DirectionalPathNode[2];
                    result[1] = directPathNode;
                    directPathNode = null;
                }
            }

            if(nodeType == PathNodeType.WALKABLE) {
                result[0] = directPathNode;
                return result;
            } else {
                if (directPathNode == null && stepHeight > 0 && nodeType != PathNodeType.FENCE && nodeType != PathNodeType.UNPASSABLE_RAIL && nodeType != PathNodeType.TRAPDOOR && direction.getY() == 0 && Math.abs(direction.getX()) + Math.abs(direction.getY()) + Math.abs(direction.getZ()) == 1) {
                    DirectionalPathNode[] pointsAbove = this.getSafePoints(x, y + 1, z, stepHeight - 1, height, direction, false);
                    directPathNode = pointsAbove.length > 0 ? pointsAbove[0] : null;

                    if(directPathNode != null && (directPathNode.type == PathNodeType.OPEN || directPathNode.type == PathNodeType.WALKABLE) && this.entity.getWidth() < 1.0F) {
                        double offsetX = (x - direction.getX()) + 0.5D;
                        double offsetZ = (z - direction.getY()) + 0.5D;

                        Box enclosingbox = new Box(
                                offsetX - halfWidth,
                                getFeetY(this.cachedWorld, new BlockPos(offsetX, (double)(y + 1), offsetZ)) + 0.001D,
                                offsetZ - halfWidth,
                                offsetX + halfWidth,
                                (double)this.entity.getHeight() + getFeetY(this.cachedWorld, new BlockPos(directPathNode.x, directPathNode.y, directPathNode.z)) - 0.002D,
                                offsetZ + halfWidth);
                        if (this.checkBoxCollision(enclosingbox)) {
                            directPathNode = null;
                        }
                    }
                }

                if(nodeType == PathNodeType.OPEN) {
                    directPathNode = null;

                    Box checkBox = new Box((double)x - halfWidth + 0.5D, (double)y + 0.001D, (double)z - halfWidth + 0.5D, (double)x + halfWidth + 0.5D, (double)((float)y + this.entity.getHeight()), (double)z + halfWidth + 0.5D);

                    if(this.checkBoxCollision(checkBox)) {
                        result[0] = null;
                        return result;
                    }

                    if(this.entity.getWidth() >= 1.0F) {
                        for(int i = 0; i < this.pathableFacingsArray.length; i++) {
                            Direction pathableFacing = this.pathableFacingsArray[i];

                            long packedAtFacing = this.getDirectionalPathNodeTypeCached(this.entity, x + pathableFacing.getOffsetX() * this.pathingSizeOffsetX, y + (pathableFacing == Direction.DOWN ? -1 : pathableFacing == Direction.UP ? this.pathingSizeOffsetY : 0), z + pathableFacing.getOffsetZ() * this.pathingSizeOffsetZ);
                            PathNodeType nodeTypeAtFacing = unpackNodeType(packedAtFacing);

                            if(nodeTypeAtFacing == PathNodeType.BLOCKED) {
                                directPathNode = this.openPoint(x, y, z, packedAtFacing, false);
                                directPathNode.type = PathNodeType.WALKABLE;
                                directPathNode.penalty = Math.max(directPathNode.penalty, malus);
                                result[0] = directPathNode;
                                return result;
                            }
                        }
                    }


                    boolean cancelFallDown = false;
                    DirectionalPathNode fallPathNode = null;

                    int fallDistance = 0;
                    int preFallY = y;

                    while(y > 0 && nodeType == PathNodeType.OPEN) {
                        --y;

                        if(fallDistance++ >= Math.max(1, this.entity.getSafeFallDistance()) /*at least one chance is required for swimming*/ || y == 0) {
                            cancelFallDown = true;
                            break;
                        }

                        packed = this.getDirectionalPathNodeTypeCached(this.entity, x, y, z);
                        nodeType = unpackNodeType(packed);

                        malus = this.entity.getPathfindingPenalty(nodeType);

                        if(((this.entity.getSafeFallDistance() > 0 && nodeType != PathNodeType.OPEN) || nodeType == PathNodeType.WATER || nodeType == PathNodeType.LAVA) && malus >= 0.0F) {
                            fallPathNode = this.openPoint(x, y, z, packed, true);
                            fallPathNode.type = nodeType;
                            fallPathNode.penalty = Math.max(fallPathNode.penalty, malus);
                            break;
                        }

                        if(malus < 0.0F) {
                            cancelFallDown = true;
                        }
                    }

                    boolean hasPathUp = false;

                    if(this.pathableFacings.size() > 1) {
                        packed = this.getDirectionalPathNodeTypeCached(this.entity, x, preFallY, z);
                        nodeType = unpackNodeType(packed);

                        malus = this.entity.getPathfindingPenalty(nodeType);

                        if(nodeType != PathNodeType.OPEN && malus >= 0.0F) {
                            if(fallPathNode != null) {
                                result = new DirectionalPathNode[2];
                                result[1] = fallPathNode;
                            }

                            result[0] = directPathNode = this.openPoint(x, preFallY, z, packed, false);
                            directPathNode.type = nodeType;
                            directPathNode.penalty = Math.max(directPathNode.penalty, malus);
                            hasPathUp = true;
                        }
                    }

                    if(fallPathNode != null) {
                        if(!hasPathUp) {
                            result[0] = directPathNode = fallPathNode;
                        } else {
                            result = new DirectionalPathNode[2];
                            result[0] = directPathNode;
                            result[1] = fallPathNode;
                        }
                    }

                    if(fallPathNode != null) {
                        float bridingMalus = this.advancedPathFindingEntity.getBridgePathingPenalty(this.entity, new BlockPos(x, preFallY, z), fallPathNode);

                        if(bridingMalus >= 0.0f) {
                            result = new DirectionalPathNode[2];
                            result[0] = directPathNode;

                            DirectionalPathNode bridgePathNode = this.openPoint(x, preFallY, z, packed, false);
                            bridgePathNode.type = PathNodeType.WALKABLE;
                            bridgePathNode.penalty = Math.max(bridgePathNode.penalty, bridingMalus);
                            result[1] = bridgePathNode;
                        }
                    }

                    if(cancelFallDown && !hasPathUp) {
                        result[0] = null;
                        if(result.length == 2) {
                            result[1] = null;
                        }
                        return result;
                    }
                }

                if(nodeType == PathNodeType.FENCE) {
                    directPathNode = this.openPoint(x, y, z, packed, false);
                    directPathNode.visited = true;
                    directPathNode.type = nodeType;
                    directPathNode.penalty = nodeType.getDefaultPenalty();
                }

                result[0] = directPathNode;
                return result;
            }
        }
    }

    protected long getDirectionalPathNodeTypeCached(MobEntity entitylivingIn, int x, int y, int z) {
        return this.pathNodeTypeCache.computeIfAbsent(BlockPos.asLong(x, y, z), (key) -> {
            return getDirectionalPathNodeType(this.rawPathNodeTypeCache, (WorldAccess) this.cachedWorld, x, y, z, this.pathingSizeOffsetX, this.pathingSizeOffsetY, this.pathingSizeOffsetZ, this.pathableFacingsArray);        });
    }

    static long packDirection(Direction facing, long packed) {
        return packed | (1L << facing.ordinal());
    }

    static long packDirection(long packed1, long packed2) {
        return (packed1 & ~0xFFFFFFFFL) | (packed1 & 0xFFFFFFFFL) | (packed2 & 0xFFFFFFFFL);
    }

    static boolean unpackDirection(Direction facing, long packed) {
        return (packed & (1L << facing.ordinal())) != 0;
    }

    static boolean unpackDirection(long packed) {
        return (packed & 0xFFFFFFFFL) != 0;
    }

    static long packNodeType(PathNodeType type, long packed) {
        return ((long) type.ordinal() << 32) | (packed & 0xFFFFFFFFL);
    }

    static PathNodeType unpackNodeType(long packed) {
        return PATH_NODE_TYPES[(int) (packed >> 32)];
    }

    @Override
    public PathNodeType getDefaultNodeType(BlockView world, int x, int y, int z) {
        return unpackNodeType(this.getDirectionalPathNodeType((WorldAccess) world, x, y, z, entity, entityBlockXSize, entityBlockYSize, entityBlockZSize, canOpenDoors, canEnterOpenDoors));
    }

    protected long getDirectionalPathNodeType(WorldAccess cachedWorldIn, int x, int y, int z, MobEntity entity, int xSize, int ySize, int zSize, boolean canBreakDoorsIn, boolean canEnterDoorsIn) {
        BlockPos pos = new BlockPos(entity.getPos());

        EnumSet<PathNodeType> applicablePathNodeTypes = EnumSet.noneOf(PathNodeType.class);

        long centerPacked = this.getDirectionalPathNodeType(cachedWorldIn, x, y, z, xSize, ySize, zSize, canBreakDoorsIn, canEnterDoorsIn, applicablePathNodeTypes, PathNodeType.BLOCKED, pos);
        PathNodeType centerPathNodeType = unpackNodeType(centerPacked);

        if(applicablePathNodeTypes.contains(PathNodeType.FENCE)) {
            return packNodeType(PathNodeType.FENCE, centerPacked);
        } else if(applicablePathNodeTypes.contains(PathNodeType.UNPASSABLE_RAIL)) {
            return packNodeType(PathNodeType.UNPASSABLE_RAIL, centerPacked);
        } else {
            PathNodeType selectedPathNodeType = PathNodeType.BLOCKED;

            for(PathNodeType applicablePathNodeType : applicablePathNodeTypes) {
                if(entity.getPathfindingPenalty(applicablePathNodeType) < 0.0F) {
                    return packNodeType(applicablePathNodeType, centerPacked);
                }

                float p1 = entity.getPathfindingPenalty(applicablePathNodeType);
                float p2 = entity.getPathfindingPenalty(selectedPathNodeType);
                if(p1 > p2 || (p1 == p2 && !(selectedPathNodeType == PathNodeType.WALKABLE && applicablePathNodeType == PathNodeType.OPEN)) || (p1 == p2 && selectedPathNodeType == PathNodeType.OPEN && applicablePathNodeType == PathNodeType.WALKABLE)) {
                    selectedPathNodeType = applicablePathNodeType;
                }
            }

            if(centerPathNodeType == PathNodeType.OPEN && entity.getPathfindingPenalty(selectedPathNodeType) == 0.0F) {
                return packNodeType(PathNodeType.OPEN, 0L);
            } else {
                return packNodeType(selectedPathNodeType, centerPacked);
            }
        }
    }

    protected long getDirectionalPathNodeType(WorldAccess cachedWorldIn, int x, int y, int z, int xSize, int ySize, int zSize, boolean canOpenDoorsIn, boolean canEnterDoorsIn, EnumSet<PathNodeType> nodeTypeEnum, PathNodeType nodeType, BlockPos pos) {
        long packed = 0L;

        for(int ox = 0; ox < xSize; ++ox) {
            for(int oy = 0; oy < ySize; ++oy) {
                for(int oz = 0; oz < zSize; ++oz) {
                    int bx = ox + x;
                    int by = oy + y;
                    int bz = oz + z;

                    long packedAdjusted = this.getDirectionalPathNodeType(cachedWorldIn, bx, by, bz);
                    PathNodeType adjustedNodeType = unpackNodeType(packedAdjusted);

                    adjustedNodeType = this.adjustNodeType(cachedWorldIn, canOpenDoorsIn, canEnterDoorsIn, pos, adjustedNodeType);

                    if (ox == 0 && oy == 0 && oz == 0) {
                        packed = packNodeType(adjustedNodeType, packedAdjusted);
                    }

                    nodeTypeEnum.add(adjustedNodeType);
                }
            }
        }

        return packed;
    }

    @Override
    public PathNodeType getNodeType(BlockView world, int x, int y, int z, MobEntity mob, int sizeX, int sizeY, int sizeZ, boolean canOpenDoors, boolean canEnterOpenDoors) {
        return unpackNodeType(this.getDirectionalPathNodeType((WorldAccess) world, x, y, z, entity, sizeX, sizeY, sizeZ, canOpenDoors, canEnterOpenDoors));
    }


    protected long getDirectionalPathNodeType(WorldAccess cachedWorldIn, int x, int y, int z) {
        return getDirectionalPathNodeType(this.rawPathNodeTypeCache, cachedWorldIn, x, y, z, this.pathingSizeOffsetX, this.pathingSizeOffsetY, this.pathingSizeOffsetZ, this.pathableFacingsArray);
    }

    protected static PathNodeType getRawPathNodeTypeCached(Long2ObjectMap<PathNodeType> cache, WorldAccess cachedWorldIn, BlockPos.Mutable pos) {
        return cache.computeIfAbsent(BlockPos.asLong(pos.getX(), pos.getY(), pos.getZ()), (key) -> {
            return getLandNodeType(cachedWorldIn, pos); //getPathNodeTypeRaw
        });
    }

    protected static long getDirectionalPathNodeType(Long2ObjectMap<PathNodeType> rawPathNodeTypeCache, WorldAccess cachedWorldIn, int x, int y, int z, int pathingSizeOffsetX, int pathingSizeOffsetY, int pathingSizeOffsetZ, Direction[] pathableFacings) {
        long packed = 0L;

        BlockPos.Mutable pos = new BlockPos.Mutable();

        PathNodeType nodeType = getRawPathNodeTypeCached(rawPathNodeTypeCache, cachedWorldIn, pos.set(x, y, z));
        boolean isWalkable = false;

        if(nodeType == PathNodeType.OPEN && y >= 1) {
            for(int i = 0; i < pathableFacings.length; i++) {
                Direction pathableFacing = pathableFacings[i];

                int checkHeight = pathableFacing.getAxis() != Direction.Axis.Y ? Math.min(4, pathingSizeOffsetY - 1) : 0;

                int cx = x + pathableFacing.getOffsetX() * pathingSizeOffsetX;
                int cy = y + (pathableFacing == Direction.DOWN ? -1 : pathableFacing == Direction.UP ? pathingSizeOffsetY : 0);
                int cz = z + pathableFacing.getOffsetZ() * pathingSizeOffsetZ;

                for(int yo = 0; yo <= checkHeight; yo++) {
                    pos.set(cx, cy + yo, cz);

                    PathNodeType offsetNodeType = getRawPathNodeTypeCached(rawPathNodeTypeCache, cachedWorldIn, pos);
                    nodeType = offsetNodeType != PathNodeType.WALKABLE && offsetNodeType != PathNodeType.OPEN && offsetNodeType != PathNodeType.WATER && offsetNodeType != PathNodeType.LAVA ? PathNodeType.WALKABLE : PathNodeType.OPEN;

                    if(offsetNodeType == PathNodeType.DAMAGE_FIRE) {
                        nodeType = PathNodeType.DAMAGE_FIRE;
                    }

                    if(offsetNodeType == PathNodeType.DAMAGE_CACTUS) {
                        nodeType = PathNodeType.DAMAGE_CACTUS;
                    }

                    if(offsetNodeType == PathNodeType.DAMAGE_OTHER) {
                        nodeType = PathNodeType.DAMAGE_OTHER;
                    }

                    if(offsetNodeType == PathNodeType.STICKY_HONEY) {
                        nodeType = PathNodeType.STICKY_HONEY;
                    }

                    if(nodeType == PathNodeType.WALKABLE) {
                        if(isColliderNodeType(offsetNodeType)) {
                            packed = packDirection(pathableFacing, packed);
                        }
                        isWalkable = true;
                    }
                }
            }
        }

        if(isWalkable) {
            nodeType = getNodeTypeFromNeighbors(cachedWorldIn, pos.set(x, y, z), PathNodeType.WALKABLE); //checkNeighborBlocks
        }

        return packNodeType(nodeType, packed);
    }

    protected static boolean isColliderNodeType(PathNodeType type) {
        return type == PathNodeType.BLOCKED || type == PathNodeType.TRAPDOOR || type == PathNodeType.FENCE ||
                type == PathNodeType.DOOR_WOOD_CLOSED || type == PathNodeType.DOOR_IRON_CLOSED || type == PathNodeType.LEAVES ||
                type == PathNodeType.STICKY_HONEY || type == PathNodeType.COCOA;
    }
}
