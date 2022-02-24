package net.arathain.tot.common.entity.living.goal;

import net.arathain.tot.common.entity.living.drider.weaver.WeaverEntity;
import net.arathain.tot.common.entity.living.drider.weaver.WebbingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;

public class WeaverPickUpWebbingGoal extends Goal {
    private final WeaverEntity obj;
    private WebbingEntity web;
    public WeaverPickUpWebbingGoal(WeaverEntity entity) {
        this.obj = entity;
    }
    @Override
    public boolean canStart() {
        return !obj.world.getOtherEntities(obj, obj.getBoundingBox().expand(20), entity -> entity instanceof WebbingEntity web && !web.getDeposited() && (web.getVehicle() == null || (web.getVehicle() != null && !(web.getVehicle() instanceof WeaverEntity)))).isEmpty() && !obj.hasPassengers();
    }

    @Override
    public void tick() {
        if(web == null || web.getDeposited()) {
            double d = -1.0;
            WebbingEntity entity = null;
            for (Entity webbingEntity : obj.world.getOtherEntities(obj, obj.getBoundingBox().expand(20), bweb -> bweb instanceof WebbingEntity wbeb && wbeb.hasPassengers() && !wbeb.getDeposited())) {
                double e = webbingEntity.squaredDistanceTo(obj.getPos());
                if (d != -1.0 && !(e < d)) continue;
                d = e;
                entity = (WebbingEntity) webbingEntity;
            }
            web = entity;
        } else {
            obj.getNavigation().startMovingTo(web, 1.0);
            if(obj.squaredDistanceTo(web) < 4) {
                web.startRiding(obj);
            }
        }


    }
}
