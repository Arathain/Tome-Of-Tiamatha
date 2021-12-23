package net.arathain.tot.common.entity.spider;

import net.minecraft.nbt.NbtCompound;

public interface IEntityReadWriteHook {
    public void onRead(NbtCompound nbt);

    public void onWrite(NbtCompound nbt);
}
