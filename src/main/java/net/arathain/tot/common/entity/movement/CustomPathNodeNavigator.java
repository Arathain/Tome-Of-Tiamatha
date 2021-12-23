package net.arathain.tot.common.entity.movement;

import com.google.common.collect.Lists;
import net.minecraft.entity.ai.pathing.*;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkCache;
import org.spongepowered.include.com.google.common.collect.Sets;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CustomPathNodeNavigator extends PathNodeNavigator {
    private final PathMinHeap path = new PathMinHeap();
    private final PathNode[] pathOptions = new PathNode[32];
    private final PathNodeMaker nodeMaker;

    private int range = 200;

    public static interface Heuristic {
        public float compute(PathNode start, PathNode end, boolean isTargetHeuristic);
    }

    public static final Heuristic DEFAULT_HEURISTIC = (start, end, isTargetHeuristic) -> start.getManhattanDistance(end); //distanceManhattan

    private Heuristic heuristic = DEFAULT_HEURISTIC;
    public CustomPathNodeNavigator(PathNodeMaker pathNodeMaker, int range) {
        super(pathNodeMaker, range);
        this.nodeMaker = pathNodeMaker;
        this.range = range;
    }


    public PathNodeMaker getNodeMaker() {
        return this.nodeMaker;
    }

    public CustomPathNodeNavigator setRange(int range) {
        this.range = range;
        return this;
    }

    public CustomPathNodeNavigator setHeuristic(Heuristic heuristic) {
        this.heuristic = heuristic;
        return this;
    }

    @Nullable
    @Override
    public Path findPathToAny(ChunkCache world, MobEntity mob, Set<BlockPos> positions, float followRange, int distance, float rangeMultiplier) {
        this.path.clear();

        this.nodeMaker.init(world, mob);

        PathNode node = this.nodeMaker.getStart();

        //Create a checkpoint for each block pos in the checkpoints set
        Map<TargetPathNode, BlockPos> checkpointsMap = positions.stream().collect(Collectors.toMap((pos) -> {
            return this.nodeMaker.getNode(pos.getX(), pos.getY(), pos.getZ());
        }, Function.identity()));

        Path path = this.findPath(node, checkpointsMap, distance, followRange, rangeMultiplier);
        this.nodeMaker.clear();

        return path;
    }

    //TODO Re-implement custom heuristics

    @Nullable
    private Path findPath(PathNode start, Map<TargetPathNode, BlockPos> checkpointsMap, float maxDistance, float checkpointRange, float maxExpansionsMultiplier) {
        Set<TargetPathNode> checkpoints = checkpointsMap.keySet();

        start.penalizedPathLength = 0.0F;
        start.distanceToNearestTarget = this.computeHeuristic(start, checkpoints);

        this.path.clear();
        this.path.push(start);

        Set<TargetPathNode> reachedCheckpoints = Sets.newHashSetWithExpectedSize(checkpoints.size());

        int expansions = 0;
        int maxExpansions = (int) (this.range * maxExpansionsMultiplier);

        while(!this.path.isEmpty() && ++expansions < maxExpansions) {
            PathNode openPathNode = this.path.pop();
            openPathNode.visited = true;

            for(TargetPathNode checkpoint : checkpoints) {
                if(openPathNode.getManhattanDistance(checkpoint) <= checkpointRange) {
                    checkpoint.markReached();
                    reachedCheckpoints.add(checkpoint);
                }
            }

            if(!reachedCheckpoints.isEmpty()) {
                break;
            }

            if(openPathNode.getDistance(start) < maxDistance) {
                int numOptions = this.nodeMaker.getSuccessors(this.pathOptions, openPathNode);

                for(int i = 0; i < numOptions; ++i) {
                    PathNode successorPathNode = this.pathOptions[i];

                    float costHeuristic = openPathNode.getDistance(successorPathNode); //TODO Replace with cost heuristic

                    //field_222861_j corresponds to the total path cost of the evaluation function
                    successorPathNode.penalizedPathLength = openPathNode.penalizedPathLength + costHeuristic;

                    float totalSuccessorPathCost = openPathNode.pathLength + costHeuristic + successorPathNode.penalty;

                    if(successorPathNode.penalizedPathLength < maxDistance && (!successorPathNode.isInHeap() || totalSuccessorPathCost < successorPathNode.pathLength)) {
                        successorPathNode.previous = openPathNode;
                        successorPathNode.pathLength = totalSuccessorPathCost;

                        //distanceToNext corresponds to the heuristic part of the evaluation function
                        successorPathNode.distanceToNearestTarget = this.computeHeuristic(successorPathNode, checkpoints) * 1.0f; //TODO Vanilla's 1.5 multiplier is too greedy :( Move to custom heuristic stuff

                        if(successorPathNode.isInHeap()) {
                            this.path.setNodeWeight(successorPathNode, successorPathNode.pathLength + successorPathNode.distanceToNearestTarget);
                        } else {
                            //distanceToTarget corresponds to the evaluation function, i.e. total path cost + heuristic
                            successorPathNode.distanceToNearestTarget = successorPathNode.pathLength + successorPathNode.distanceToNearestTarget;
                            this.path.push(successorPathNode);
                        }
                    }
                }
            }
        }

        Optional<Path> path;

        if(!reachedCheckpoints.isEmpty()) {
            //Use shortest path towards next reached checkpoint
            path = reachedCheckpoints.stream().map((checkpoint) -> {
                return this.createPath(checkpoint.getNearestNode(), checkpointsMap.get(checkpoint), true);
            }).min(Comparator.comparingInt(Path::getLength));
        } else {
            //Use lowest cost path towards any checkpoint
            path = checkpoints.stream().map((checkpoint) -> {
                return this.createPath(checkpoint.getNearestNode(), checkpointsMap.get(checkpoint), false);
            }).min(Comparator.comparingDouble(Path::getLength /*TODO Replace calculation with cost heuristic*/).thenComparingInt(Path::getLength));
        }

        return !path.isPresent() ? null : path.get();
    }

    private float computeHeuristic(PathNode PathNode, Set<TargetPathNode> checkpoints) {
        float minDst = Float.MAX_VALUE;

        for(TargetPathNode checkpoint : checkpoints) {
            float dst = PathNode.getDistance(checkpoint); //TODO Replace with target heuristic
            checkpoint.updateNearestNode(dst, PathNode);
            minDst = Math.min(dst, minDst);
        }

        return minDst;
    }

    protected Path createPath(PathNode start, BlockPos target, boolean isTargetReached) {
        List<PathNode> points = Lists.newArrayList();

        PathNode currentPathNode = start;
        points.add(0, start);

        while(currentPathNode.previous != null) {
            currentPathNode = currentPathNode.previous;
            points.add(0, currentPathNode);
        }

        return new Path(points, target, isTargetReached);
    }
}
