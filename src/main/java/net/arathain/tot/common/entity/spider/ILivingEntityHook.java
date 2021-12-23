package net.arathain.tot.common.entity.spider;

import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.util.math.Vec3d;

public interface ILivingEntityHook {
    public boolean onJump();
    public boolean onTravel(Vec3d relative, boolean pre);
    public Vec3d onLookAt(EntityAnchorArgumentType.EntityAnchor anchor, Vec3d vec);
}
