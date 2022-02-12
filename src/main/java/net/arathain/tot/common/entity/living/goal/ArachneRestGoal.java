package net.arathain.tot.common.entity.living.goal;

import net.arathain.tot.common.entity.living.drider.arachne.ArachneEntity;
import net.minecraft.entity.ai.FuzzyTargeting;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;

public class ArachneRestGoal extends Goal {
    private int time;
    private final ArachneEntity arachne;
    public ArachneRestGoal(ArachneEntity arachneEntity) {
        arachne = arachneEntity;
        setControls(EnumSet.of(Control.MOVE));
    }
    @Override
    public boolean canStart() {
        return !arachne.getRestingPos().isWithinDistance(arachne.getBlockPos(), 20) && !(arachne.getTarget() == null);
    }
    @Override
    public void stop()
    {
        this.time = 0;
    }

    @Override
    public void start() {
        arachne.getNavigation().stop();
    }
    @Override
    public void tick()
    {
        int sqDistTo = 16 * 16;
        Vec3d restingPos = Vec3d.ofBottomCenter(arachne.getRestingPos());

        time++;
        if (arachne.squaredDistanceTo(restingPos) > sqDistTo + 35 || time >= 600)
            arachne.refreshPositionAndAngles(arachne.getRestingPos().up(), arachne.getYaw(), arachne.getPitch());
        else
        {
            Vec3d movePos;
            if (arachne.getNavigation().isIdle() && (movePos = FuzzyTargeting.findTo(arachne, 8, 2, restingPos)) != null)
                arachne.getNavigation().startMovingTo(movePos.x, movePos.y, movePos.y, 1.1);
        }
    }
}
