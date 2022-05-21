package net.arathain.tot.common.entity.living.drider;

import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.SpiderNavigation;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class DriderEntityNavigation extends SpiderNavigation {
    public DriderEntityNavigation(MobEntity mobEntity, World world) {
        super(mobEntity, world);
    }

    @Override
    public void tick() {
        if(this.entity.getTarget() != null) {
            super.tick();
        } else {
            ++this.tickCount;
            if (this.shouldRecalculate) {
                this.recalculatePath();
            }

            if (!this.isIdle()) {
                if (this.isAtValidPosition()) {
                    this.continueFollowingPath();
                } else if (this.currentPath != null && !this.currentPath.isFinished()) {
                    Vec3d vec3d = this.getPos();
                    Vec3d vec3d2 = this.currentPath.getNodePosition(this.entity);
                    if (vec3d.y > vec3d2.y
                            && !this.entity.isOnGround()
                            && MathHelper.floor(vec3d.x) == MathHelper.floor(vec3d2.x)
                            && MathHelper.floor(vec3d.z) == MathHelper.floor(vec3d2.z)) {
                        this.currentPath.next();
                    }
                }

                DebugInfoSender.sendPathfindingData(this.world, this.entity, this.currentPath, this.nodeReachProximity);
                if (!this.isIdle()) {
                    Vec3d vec3d = this.currentPath.getNodePosition(this.entity);
                    this.entity.getMoveControl().moveTo(vec3d.x, this.adjustTargetY(vec3d), vec3d.z, this.speed);
                }
            }
        }
    }
}
