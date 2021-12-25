package net.arathain.tot.common.entity.spider;

public interface ILivingEntityRotationHook {
    public float getTargetYaw(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport);

    public float getTargetPitch(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport);

    public float getTargetHeadYaw(float yaw, int rotationIncrements);
}
