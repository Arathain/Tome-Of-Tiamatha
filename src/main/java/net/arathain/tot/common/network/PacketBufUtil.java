package net.arathain.tot.common.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * @author legoatoom
 * **/
public final class PacketBufUtil {

    /**
     * Packs a floating-point angle into a {@code byte}.
     *
     * @param angle angle
     * @return packed angle
     */
    public static byte packAngle(float angle) {
        return (byte) MathHelper.floor(angle * 256 / 360);
    }

    /**
     * Unpacks a floating-point angle from a {@code byte}.
     *
     * @param angleByte packed angle
     * @return angle
     */
    public static float unpackAngle(byte angleByte) {
        return (angleByte * 360) / 256f;
    }

    /**
     * Writes an angle to a {@link PacketByteBuf}.
     *
     * @param byteBuf destination buffer
     * @param angle   angle
     */
    public static void writeAngle(PacketByteBuf byteBuf, float angle) {
        byteBuf.writeByte(packAngle(angle));
    }

    /**
     * Reads an angle from a {@link PacketByteBuf}.
     *
     * @param byteBuf source buffer
     * @return angle
     */
    public static float readAngle(PacketByteBuf byteBuf) {
        return unpackAngle(byteBuf.readByte());
    }

    /**
     * Writes a {@link Vec3d} to a {@link PacketByteBuf}.
     *
     * @param byteBuf destination buffer
     * @param vec3d   vector
     */
    public static void writeVec3d(PacketByteBuf byteBuf, Vec3d vec3d) {
        byteBuf.writeDouble(vec3d.x);
        byteBuf.writeDouble(vec3d.y);
        byteBuf.writeDouble(vec3d.z);
    }

    /**
     * Writes a {@link BlockPos} to a {@link PacketByteBuf}.
     *
     * @param byteBuf destination buffer
     * @param blockPos blockPos
     */
    public static void writeBlockPos(PacketByteBuf byteBuf, BlockPos blockPos) {
        byteBuf.writeDouble(blockPos.getX());
        byteBuf.writeDouble(blockPos.getY());
        byteBuf.writeDouble(blockPos.getZ());
    }

    /**
     * Reads a {@link Vec3d} from a {@link PacketByteBuf}.
     *
     * @param byteBuf source buffer
     * @return vector
     */
    public static Vec3d readVec3d(PacketByteBuf byteBuf) {
        double x = byteBuf.readDouble();
        double y = byteBuf.readDouble();
        double z = byteBuf.readDouble();
        return new Vec3d(x, y, z);
    }

    /**
     * Reads a {@link BlockPos} from a {@link PacketByteBuf}.
     *
     * @param byteBuf source buffer
     * @return blockPos
     */
    public static BlockPos readBlockPos(PacketByteBuf byteBuf) {
        double x = byteBuf.readDouble();
        double y = byteBuf.readDouble();
        double z = byteBuf.readDouble();
        return new BlockPos(x, y, z);
    }
}
