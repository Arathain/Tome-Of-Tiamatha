package net.arathain.tot.common.entity.movement;

import net.arathain.tot.common.entity.spider.IClimberEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BetterSpiderPathNavigation<T extends MobEntity & IClimberEntity> extends AdvancedClimberPathNavigation<T> {
    private boolean useVanillaBehaviour;
    private BlockPos targetPosition;

    public BetterSpiderPathNavigation(T entity, World worldIn, boolean useVanillaBehaviour) {
        super(entity, worldIn, true, true);
        this.useVanillaBehaviour = useVanillaBehaviour;
    }

    @Override
    public Path findPathTo(BlockPos target, int distance) {
        this.targetPosition = target;
        return super.findPathTo(target, distance);
    }

    @Override
    public Path findPathTo(Entity entity, int distance) {
        this.targetPosition = entity.getBlockPos();
        return super.findPathTo(entity, distance);
    }

    @Override
    public boolean startMovingTo(Entity entity, double speed) {
        Path path = this.findPathTo(entity, 0);
        if(path != null) {
            return this.startMovingAlong(path, speed);
        } else {
            this.targetPosition = entity.getBlockPos();
            this.speed = speed;
            return true;
        }
    }

    @Override
    public void tick() {
        if(this.isFollowingPath()) {
            super.tick();
        } else {
            if(this.targetPosition != null && this.useVanillaBehaviour) {
                if(!this.targetPosition.isWithinDistance(this.entity.getPos(), Math.max((double) this.entity.getWidth(), 1.0D)) && (!(this.entity.getY() > (double) this.targetPosition.getY()) || !(new BlockPos((double) this.targetPosition.getX(), this.entity.getY(), (double) this.targetPosition.getZ())).isWithinDistance(this.entity.getPos(), Math.max((double) this.entity.getWidth(), 1.0D)))) {
                    this.entity.getMoveControl().moveTo((double) this.targetPosition.getX(), (double) this.targetPosition.getY(), (double) this.targetPosition.getZ(), this.speed);
                } else {
                    this.targetPosition = null;
                }
            }

        }
    }
}
