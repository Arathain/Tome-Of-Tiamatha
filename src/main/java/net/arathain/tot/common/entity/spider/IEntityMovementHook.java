package net.arathain.tot.common.entity.spider;

import net.minecraft.entity.MovementType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

public interface IEntityMovementHook {
    void onTick();

    float getServerYaw(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport);

    float getTargetPitch(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport);

    float getTargetHeadYaw(float yaw, int rotationIncrements);

    boolean onJump();

    public default boolean onMove(MovementType type, Vec3d pos, boolean pre) {
        return false;
    }

    @Nullable
    public BlockPos getAdjustedOnPosition(BlockPos onPosition);

    public boolean getAdjustedCanTriggerWalking(boolean canTriggerWalking);
}
