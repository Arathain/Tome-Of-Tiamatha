package net.arathain.tot.common.entity.movement;

import net.arathain.tot.common.entity.spider.IClimberEntity;
import net.minecraft.client.util.math.Vector3d;
import net.minecraft.entity.ai.control.JumpControl;
import net.minecraft.entity.mob.MobEntity;

import javax.annotation.Nullable;

public class ClimberJumpControl<T extends MobEntity & IClimberEntity> extends JumpControl {
    protected final T climber;

    @Nullable
    protected Vector3d dir;


    public ClimberJumpControl(MobEntity entity) {
        super(entity);
        this.climber = (T) entity;
    }

    @Override
    public void setActive() {
        this.setJumping(null);
    }

    public void setJumping(Vector3d dir) {
        super.setActive();
        this.dir = dir;
    }

    @Override
    public void tick() {
        this.climber.setJumping(this.active);
        if(this.active) {
            this.climber.setJumpDirection(this.dir);
        } else if(this.dir == null) {
            this.climber.setJumpDirection(null);
        }
        this.active = false;
    }
}