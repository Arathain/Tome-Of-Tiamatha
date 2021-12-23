package net.arathain.tot.common.entity.movement;

import net.minecraft.entity.ai.pathing.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import javax.annotation.Nullable;
import java.util.*;

public class AdvancedPathNodeNavigator extends CustomPathNodeNavigator {

    private static class Node {
        private final Node previous;
        private final DirectionalPathNode PathNode;
        private final Direction side;
        private final int depth;

        private Node(@Nullable Node previous, DirectionalPathNode PathNode) {
            this.previous = previous;
            this.depth = previous != null ? previous.depth + 1 : 0;
            this.PathNode = PathNode;
            this.side = PathNode.getPathSide();
        }

        private Node(Node previous, int depth, DirectionalPathNode PathNode) {
            this.previous = previous;
            this.depth = depth;
            this.PathNode = PathNode;
            this.side = PathNode.getPathSide();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((this.PathNode == null) ? 0 : this.PathNode.hashCode());
            result = prime * result + ((this.side == null) ? 0 : this.side.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if(this == obj) {
                return true;
            }
            if(obj == null) {
                return false;
            }
            if(this.getClass() != obj.getClass()) {
                return false;
            }
            Node other = (Node) obj;
            if(this.PathNode == null) {
                if(other.PathNode != null) {
                    return false;
                }
            } else if(!this.PathNode.equals(other.PathNode)) {
                return false;
            }
            return this.side == other.side;
        }
    }

    private static final Direction[] DOWN = new Direction[] { Direction.DOWN };

    public AdvancedPathNodeNavigator(PathNodeMaker pathNodeMaker, float range) {
        super(pathNodeMaker, (int) range);
    }


    @Override
    protected Path createPath(PathNode _targetPoint, BlockPos target, boolean isTargetReached) {
        List<PathNode> points = new ArrayList<>();

        //Backtrack path from target point back to entity
        this.backtrackPath(points, _targetPoint);

        //Retrace path with valid side transitions
        Node end = this.retraceSidedPath(points, true);

        if(end == null) {
            return new Path(Collections.emptyList(), target, isTargetReached);
        }

        points.clear();

        //Backtrack retraced path
        this.backtrackPath(points, end);

        return new Path(points, target, isTargetReached);
    }

    private void backtrackPath(List<PathNode> points, PathNode start) {
        PathNode currentPathNode = start;
        points.add(start);

        while(currentPathNode.previous != null) {
            currentPathNode = currentPathNode.previous;
            points.add(currentPathNode);
        }
    }

    private void backtrackPath(List<PathNode> points, Node start) {
        Node currentNode = start;
        points.add(start.PathNode);

        while(currentNode.previous != null) {
            currentNode = currentNode.previous;
            points.add(currentNode.PathNode);
        }
    }

    private static Direction[] getPathableSidesWithFallback(DirectionalPathNode point) {
        if(point.getPathableSides().length == 0) {
            return DOWN;
        } else {
            return point.getPathableSides();
        }
    }

    private static boolean isOmnidirectionalPoint(DirectionalPathNode point) {
        return point.type == PathNodeType.WATER || point.type == PathNodeType.LAVA;
    }

    private Node retraceSidedPath(List<PathNode> points, boolean isReversed) {
        if(points.isEmpty()) {
            return null;
        }

        final Deque<Node> queue = new LinkedList<>();

        final DirectionalPathNode targetPoint = this.ensureDirectional(points.get(0));

        for(Direction direction : getPathableSidesWithFallback(targetPoint)) {
            queue.add(new Node(null, targetPoint.assignPathSide(direction)));
        }

        Node end = null;

        final int maxExpansions = 200;
        final Set<Node> checkedSet = new HashSet<>();

        int expansions = 0;
        while(!queue.isEmpty()) {
            if(expansions++ > maxExpansions) {
                break;
            }

            Node current = queue.removeFirst();

            if(current.depth == points.size() - 1) {
                end = current;
                break;
            }

            Direction currentSide = current.side;

            DirectionalPathNode next = this.ensureDirectional(points.get(current.depth + 1));

            for(Direction nextSide : getPathableSidesWithFallback(next)) {
                Node nextNode = null;

                if((isReversed && current.PathNode.isDrop()) || (!isReversed && next.isDrop())) {

                    //Side doesn't matter if node represents a drop
                    nextNode = new Node(current, next.assignPathSide(nextSide));

                } else {
                    int dx = (int)Math.signum(next.x - current.PathNode.x);
                    int dy = (int)Math.signum(next.y - current.PathNode.y);
                    int dz = (int)Math.signum(next.z - current.PathNode.z);

                    int adx = Math.abs(dx);
                    int ady = Math.abs(dy);
                    int adz = Math.abs(dz);

                    int d = adx + ady + adz;

                    if(d == 1) {
                        //Path is straight line

                        if(nextSide == currentSide) {

                            //Allow movement on the same side
                            nextNode = new Node(current, next.assignPathSide(nextSide));

                        } else if(nextSide.getAxis() != currentSide.getAxis()) {

                            //Allow movement around corners, but insert new point with transitional side inbetween

                            Node intermediary;
                            if(Math.abs(currentSide.getOffsetX()) == adx && Math.abs(currentSide.getOffsetY()) == ady && Math.abs(currentSide.getOffsetZ()) == adz) {
                                intermediary = new Node(current, current.PathNode.assignPathSide(nextSide));
                            } else {
                                intermediary = new Node(current, next.assignPathSide(currentSide));
                            }

                            nextNode = new Node(intermediary, intermediary.depth, next.assignPathSide(nextSide));

                        }
                    } else if(d == 2) {
                        //Diagonal

                        int currentSidePlaneMatch = (currentSide.getOffsetX() == -dx ? 1 : 0) + (currentSide.getOffsetY() == -dy ? 1 : 0) + (currentSide.getOffsetZ() == -dz ? 1 : 0);

                        if(currentSide == nextSide && currentSidePlaneMatch == 0) {

                            //Allow diagonal movement, no need to insert transitional side since the diagonal's plane's normal is the same as the path's side
                            nextNode = new Node(current, next.assignPathSide(nextSide));

                        } else {
                            //Allow movement, but insert new point with transitional side inbetween

                            Node intermediary = null;
                            if(currentSidePlaneMatch == 2) {
                                for(Direction intermediarySide : getPathableSidesWithFallback(current.PathNode)) {
                                    if(intermediarySide != currentSide && (intermediarySide.getOffsetX() == dx ? 1 : 0) + (intermediarySide.getOffsetY() == dy ? 1 : 0) + (intermediarySide.getOffsetZ() == dz ? 1 : 0) == 2) {
                                        intermediary = new Node(current, current.PathNode.assignPathSide(intermediarySide));
                                        break;
                                    }
                                }
                            } else {
                                for(Direction intermediarySide : getPathableSidesWithFallback(next)) {
                                    if(intermediarySide != nextSide && (intermediarySide.getOffsetX() == -dx ? 1 : 0) + (intermediarySide.getOffsetY() == -dy ? 1 : 0) + (intermediarySide.getOffsetZ() == -dz ? 1 : 0) == 2) {
                                        intermediary = new Node(current, next.assignPathSide(intermediarySide));
                                        break;
                                    }
                                }
                            }

                            if(intermediary != null) {
                                nextNode = new Node(intermediary, intermediary.depth, next.assignPathSide(nextSide));
                            } else {
                                nextNode = new Node(current, next.assignPathSide(nextSide));
                            }
                        }
                    }
                }

                if(nextNode != null && checkedSet.add(nextNode)) {
                    queue.addLast(nextNode);
                }
            }
        }

        return end;
    }

    private DirectionalPathNode ensureDirectional(PathNode point) {
        if(point instanceof DirectionalPathNode) {
            return (DirectionalPathNode) point;
        } else {
            return new DirectionalPathNode(point);
        }
    }
}
