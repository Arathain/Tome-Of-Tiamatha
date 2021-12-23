package net.arathain.tot.common.entity.spider;

import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public interface IMobEntityHook {
    void onLivingTick();
    public void onTick();
    public void onInitGoals();
    @Nullable
    public EntityNavigation onCreateNavigation(World world);
}
