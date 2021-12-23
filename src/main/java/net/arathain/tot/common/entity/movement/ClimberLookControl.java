package net.arathain.tot.common.entity.movement;

import net.arathain.tot.common.entity.spider.IClimberEntity;
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

public class ClimberLookControl<T extends MobEntity & IClimberEntity> extends LookControl {
    protected final IClimberEntity climber;

    public ClimberLookControl(T entity) {
        super(entity);
        this.climber = entity;
    }

    @Override
    protected Optional<Float> getTargetPitch() {
        Vec3d dir = new Vec3d(this.getLookX() - this.entity.getX(), this.getLookY() - this.entity.getEyeY(), this.getLookZ() - this.entity.getZ());
        return this.climber.getOrientation().getLocalRotation(dir).getRight().describeConstable();
    }

    @Override
    protected Optional<Float> getTargetYaw() {
        Vec3d dir = new Vec3d(this.getLookX() - this.entity.getX(), this.getLookY() - this.entity.getEyeY(), this.getLookZ() - this.entity.getZ());
        return this.climber.getOrientation().getLocalRotation(dir).getLeft().describeConstable();
    }
}
