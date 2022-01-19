package net.arathain.tot.common.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class GazingLilyItem extends Item implements MagicModifierItem {
    public GazingLilyItem(Settings settings) {
        super(settings);
    }
    public static void summonLilyFangs(Vec3d pos, LivingEntity user) {
        double d = Math.min(pos.getY(), user.getY());
        double e = Math.max(pos.getY(), user.getY()) + 1.0;
        float f = (float) MathHelper.atan2(pos.getZ() - user.getZ(), pos.getX() - user.getX());
        if (user.isSneaking()) {
            float g;
            int i;
            for (i = 0; i < 5; ++i) {
                g = f + (float)i * (float)Math.PI * 0.4f;
                SynthesisScepterItem.conjureFangs(user.getX() + (double) MathHelper.cos(g) * 1.5, user.getZ() + (double)MathHelper.sin(g) * 1.5, d, e, g, 0, user);
            }
            for (i = 0; i < 8; ++i) {
                g = f + (float)i * (float)Math.PI * 2.0f / 8.0f + 1.2566371f;
                SynthesisScepterItem.conjureFangs(user.getX() + (double)MathHelper.cos(g) * 2.5, user.getZ() + (double)MathHelper.sin(g) * 2.5, d, e, g, 3, user);
            }
            for (i = 0; i < 13; ++i) {
                g = f + (float)i * (float)Math.PI * 2.0f / 13.0f;
                SynthesisScepterItem.conjureFangs(user.getX() + (double)MathHelper.cos(g) * 3.7, user.getZ() + (double)MathHelper.sin(g) * 3.7, d, e, g, 6, user);
            }
        } else {
            float g;
            int i;
            g = f;
            SynthesisScepterItem.conjureFangs(pos.getX(), pos.getZ(), d, e, g, 0, user);
            for (i = 0; i < 5; ++i) {
                g = f + (float)i * (float)Math.PI * 0.4f;
                SynthesisScepterItem.conjureFangs(pos.getX() + (double) MathHelper.cos(g) * 1.5, pos.getZ() + (double)MathHelper.sin(g) * 1.5, d, e, g, 1, user);
            }
            for (i = 0; i < 8; ++i) {
                g = f + (float)i * (float)Math.PI * 2.0f / 8.0f + 1.2566371f;
                SynthesisScepterItem.conjureFangs(pos.getX() + (double)MathHelper.cos(g) * 2.5, pos.getZ() + (double)MathHelper.sin(g) * 2.5, d, e, g, 4, user);
            }
            for (i = 0; i < 13; ++i) {
                g = f + (float)i * (float)Math.PI * 2.0f / 13.0f;
                SynthesisScepterItem.conjureFangs(pos.getX() + (double)MathHelper.cos(g) * 3.7, pos.getZ() + (double)MathHelper.sin(g) * 3.7, d, e, g, 7, user);
            }
        }
    }
}
