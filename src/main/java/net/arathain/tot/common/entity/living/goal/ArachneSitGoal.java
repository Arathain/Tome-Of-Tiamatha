package net.arathain.tot.common.entity.living.goal;

import net.arathain.tot.common.entity.living.drider.arachne.ArachneEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.TameableEntity;

import java.util.EnumSet;

public class ArachneSitGoal extends Goal {
    private final ArachneEntity arachne;

    public ArachneSitGoal(ArachneEntity tameable) {
        this.arachne = tameable;
        this.setControls(EnumSet.of(Control.JUMP, Control.MOVE));
    }

    public boolean shouldContinue() {
        return this.arachne.isSitting();
    }

    public boolean canStart() {
        if (this.arachne.getTarget() != null) {
            return false;
        } else if (this.arachne.isInsideWaterOrBubbleColumn()) {
            return false;
        } else return this.arachne.isOnGround();
    }

    public void start() {
        this.arachne.getNavigation().stop();
        this.arachne.setInSittingPose(true);
    }

    public void stop() {
        this.arachne.setInSittingPose(false);
    }
}
