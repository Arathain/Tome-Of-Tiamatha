package net.arathain.tot.common.entity.spider;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class PathingTarget {
    public final BlockPos pos;
    public final Direction side;

    public PathingTarget(BlockPos pos, Direction side) {
        this.pos = pos;
        this.side = side;
    }
}
