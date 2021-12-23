package net.arathain.tot.common.entity.movement;

import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.util.math.Direction;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class DirectionalPathNode extends PathNode {
    protected static final long ALL_DIRECTIONS = AdvancedLandPathNodeMaker.packDirection(Direction.UP, AdvancedLandPathNodeMaker.packDirection(Direction.DOWN, AdvancedLandPathNodeMaker.packDirection(Direction.NORTH, AdvancedLandPathNodeMaker.packDirection(Direction.EAST, AdvancedLandPathNodeMaker.packDirection(Direction.SOUTH, AdvancedLandPathNodeMaker.packDirection(Direction.WEST, 0L))))));

    protected static final Direction[] DIRECTIONS = Direction.values();

    private final Direction[] pathableSides;
    private final Direction pathSide;

    private final boolean isDrop;

    public DirectionalPathNode(int x, int y, int z, long packed, boolean isDrop) {
        super(x, y, z);

        EnumSet<Direction> directionsSet = EnumSet.noneOf(Direction.class);
        for (int i = 0; i < DIRECTIONS.length; i++) {
            Direction dir = DIRECTIONS[i];

            if (AdvancedLandPathNodeMaker.unpackDirection(dir, packed)) {
                directionsSet.add(dir);
            }
        }

        this.pathableSides = directionsSet.toArray(new Direction[0]);
        this.pathSide = null;

        this.isDrop = isDrop;
    }

    public DirectionalPathNode(PathNode node, long packed, boolean isDrop) {
        this(node.x, node.y, node.z, packed, isDrop);

        this.heapIndex = node.heapIndex;
        this.penalizedPathLength = node.penalizedPathLength;
        this.distanceToNearestTarget = node.distanceToNearestTarget;
        this.heapWeight = node.heapWeight;
        this.previous = node.previous;
        this.visited = node.visited;
        this.pathLength = node.pathLength;
        this.penalty = node.penalty;
        this.type = node.type;
    }

    public DirectionalPathNode(PathNode point) {
        this(point, ALL_DIRECTIONS, false);
    }

    private DirectionalPathNode(int x, int y, int z, Direction[] pathableSides, Direction pathSide, boolean isDrop) {
        super(x, y, z);

        this.pathableSides = new Direction[pathableSides.length];
        System.arraycopy(pathableSides, 0, this.pathableSides, 0, pathableSides.length);

        this.pathSide = pathSide;

        this.isDrop = isDrop;
    }

    public DirectionalPathNode(PathNode node, Direction pathSide) {
        super(node.x, node.y, node.z);

        this.heapIndex = node.heapIndex;
        this.penalizedPathLength = node.penalizedPathLength;
        this.distanceToNearestTarget = node.distanceToNearestTarget;
        this.heapWeight = node.heapWeight;
        this.previous = node.previous;
        this.visited = node.visited;
        this.pathLength = node.pathLength;
        this.penalty = node.penalty;
        this.type = node.type;

        if (node instanceof DirectionalPathNode) {
            DirectionalPathNode other = (DirectionalPathNode) node;

            this.pathableSides = new Direction[other.pathableSides.length];
            System.arraycopy(other.pathableSides, 0, this.pathableSides, 0, other.pathableSides.length);

            this.isDrop = other.isDrop;
        } else {
            this.pathableSides = Direction.values();

            this.isDrop = false;
        }

        this.pathSide = pathSide;
    }

    public DirectionalPathNode assignPathSide(Direction pathDirection) {
        return new DirectionalPathNode(this, pathDirection);
    }

    @Override
    public PathNode copyWithNewPosition(int x, int y, int z) {
        PathNode node = new DirectionalPathNode(x, y, z, this.pathableSides, this.pathSide, this.isDrop);
        this.heapIndex = node.heapIndex;
        this.penalizedPathLength = node.penalizedPathLength;
        this.distanceToNearestTarget = node.distanceToNearestTarget;
        this.heapWeight = node.heapWeight;
        this.previous = node.previous;
        this.visited = node.visited;
        this.pathLength = node.pathLength;
        this.penalty = node.penalty;
        this.type = node.type;
        return node;
    }

    /**
     * Returns all pathable sides of this node, i.e. all sides the entity could potentially walk on
     *
     * @return
     */
    public Direction[] getPathableSides() {
        return this.pathableSides;
    }

    /**
     * Returns the side assigned to this node by the path this node is part of, or null if this node has not been assigned to a path
     *
     * @return
     */
    @Nullable
    public Direction getPathSide() {
        return this.pathSide;
    }

    /**
     * Returns whether this node represents a drop
     *
     * @return
     */
    public boolean isDrop() {
        return this.isDrop;
    }
}
