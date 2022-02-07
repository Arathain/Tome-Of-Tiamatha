package net.arathain.tot.common.entity.living.goal;

import net.arathain.tot.common.entity.living.drider.arachne.ArachneEntity;
import net.minecraft.entity.ai.goal.Goal;

import java.util.EnumSet;

public class ArachneSitGoal extends Goal {
    private final ArachneEntity arachne;
    public ArachneSitGoal(ArachneEntity arachneEntity) {
        arachne = arachneEntity;
        setControls(EnumSet.of(Control.MOVE));
    }
    @Override
    public boolean canStart() {
        return !(arachne.getTarget() == null) && arachne.getBlockPos().isWithinDistance(arachne.getRestingPos(), 2);
    }

    @Override
    public void start() {
        arachne.getDataTracker().set(ArachneEntity.RESTING, true);
        super.start();
    }

    @Override
    public boolean shouldContinue() {
        return canStart();
    }

    @Override
    public void stop() {
        arachne.getDataTracker().set(ArachneEntity.RESTING, false);
        super.stop();
    }
}
