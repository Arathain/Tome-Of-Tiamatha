package net.arathain.tot.common.util;

import net.arathain.tot.TomeOfTiamatha;
import net.arathain.tot.common.util.config.ToTConfig;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

/**
* courtesy of legoatoom
**/
public class StringUtils {
    public static Identifier identifier(String name) {
        return new Identifier(TomeOfTiamatha.MODID, name);
    }

    @Deprecated
    public static double drip(double x, double d) {
        double c = TomeOfTiamatha.CONFIG.stringHangAmount;
        double b = -c / d;
        double a = c / (d * d);
        return (a * (x * x) + b * x);
    }

    /**
     * For geogebra:
     * a = 9
     * h = 0
     * d = 5
     * p1 = a * asinh( (h / (2*a)) * 1 / sinh(d / (2*a)) )
     * p2 = -a * cosh( (2*p1 - d) / (2*a) )
     * f(x) = p2 + a * cosh( (2*x + 2*p1 - d) / (2*a) )
     * @param x from 0 to d
     * @param d length of the chain
     * @param h height at x=d
     * @return y
     */
    public static double drip2(double x, double d, double h) {
        double a = TomeOfTiamatha.CONFIG.stringHangAmount;
        double p1 = a * asinh((h / (2D * a)) * (1D / Math.sinh(d / (2D * a))));
        double p2 = -a * Math.cosh((2D * p1 - d) / (2D * a));
        return p2 + a * Math.cosh((((2D * x) + (2D * p1)) - d) / (2D * a));
    }

    /**
     * Derivative of drip2
     * For geogebra:
     * f'(x) = sinh( (2*x + 2*p1 - d) / (2*a) )
     * @param x from 0 to d
     * @param d length of the chain
     * @param h height at x=d
     * @return gradient at x
     */
    public static double drip2prime(double x, double d, double h) {
        double a = TomeOfTiamatha.CONFIG.stringHangAmount;
        double p1 = a * asinh((h / (2D * a)) * (1D / Math.sinh(d / (2D * a))));
        return Math.sinh( (2*x + 2*p1 - d) / (2*a) );
    }

    private static double asinh(double x) {
        return Math.log(x + Math.sqrt(x * x + 1.0));
    }

    public static Vec3d middleOf(Vec3d a, Vec3d b) {
        double x = (a.getX() - b.getX()) / 2d + b.getX();
        double y = (a.getY() - b.getY()) / 2d + b.getY();
        double z = (a.getZ() - b.getZ()) / 2d + b.getZ();
        return new Vec3d(x, y, z);
    }

    public static float distanceBetween(Vec3f a, Vec3f b) {
        float dx = a.getX() - b.getX();
        float dy = a.getY() - b.getY();
        float dz = a.getZ() - b.getZ();
        return (float) Math.sqrt(dx*dx + dy*dy + dz*dz);
    }

    public static float lengthOf(Vec3f v) {
        float x = v.getX();
        float y = v.getY();
        float z = v.getZ();
        return (float) Math.sqrt(x*x + y*y + z*z);
    }

    /**
     * Get the x/z offset from a string to a block
     * @param start fence pos
     * @param end fence pos
     * @return the x/z offset
     */
    public static Vec3f getStringOffset(Vec3d start, Vec3d end) {
        Vec3f offset = new Vec3f(end.subtract(start));
        offset.set(offset.getX(), 0, offset.getZ());
        offset.normalize();
        offset.scale(2/16f);
        return offset;
    }
}
