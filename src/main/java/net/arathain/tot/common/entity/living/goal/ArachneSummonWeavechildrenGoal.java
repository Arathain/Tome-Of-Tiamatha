package net.arathain.tot.common.entity.living.goal;

import net.arathain.tot.common.entity.living.drider.DriderEntity;
import net.arathain.tot.common.entity.living.drider.arachne.ArachneEntity;
import net.arathain.tot.common.init.ToTEntities;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.SpawnHelper;

import java.util.EnumSet;

public class ArachneSummonWeavechildrenGoal extends Goal {
    private final ArachneEntity arachne;

    private int spawned;
    public ArachneSummonWeavechildrenGoal(ArachneEntity entity) {
        this.arachne = entity;
        this.spawned = 0;
        this.setControls(EnumSet.of(Control.LOOK));
    }
    @Override
    public boolean canStart() {
        return arachne.getTarget() != null && !arachne.isResting() && spawned <= 10;
    }

    @Override
    public void stop() {
        spawned = 0;
    }

    @Override
    public void start() {
        if(spawned <= 10 && arachne.age % 10 == 0 && arachne.getTarget() != null) {
            if(!arachne.getWorld().isClient) {
                int i = MathHelper.floor(arachne.getX());
                int j = MathHelper.floor(arachne.getY());
                int k = MathHelper.floor(arachne.getZ());
                DriderEntity drider = new DriderEntity(ToTEntities.DRIDER, arachne.world);
                for (int l = 0; l < 50; ++l) {
                    int m = i + MathHelper.nextInt(arachne.getRandom(), 7, 40) * MathHelper.nextInt(arachne.getRandom(), -1, 1);
                    int n = j + MathHelper.nextInt(arachne.getRandom(), 7, 40) * MathHelper.nextInt(arachne.getRandom(), -1, 1);
                    int o = k + MathHelper.nextInt(arachne.getRandom(), 7, 40) * MathHelper.nextInt(arachne.getRandom(), -1, 1);
                    BlockPos blockPos = new BlockPos(m, n, o);
                    EntityType<?> entityType = drider.getType();
                    SpawnRestriction.Location location = SpawnRestriction.getLocation(entityType);
                    if (!SpawnHelper.canSpawn(location, arachne.world, blockPos, entityType) || !SpawnRestriction.canSpawn(entityType, arachne.getWorld().getServer().getWorld(arachne.getWorld().getRegistryKey()), SpawnReason.REINFORCEMENT, blockPos, arachne.world.random)) continue;
                    drider.setPosition(m, n, o);
                    if (arachne.world.isPlayerInRange(m, n, o, 20.0) || !arachne.world.intersectsEntities(drider) || !arachne.world.isSpaceEmpty(drider) || arachne.world.containsFluid(drider.getBoundingBox())) continue;
                    drider.setTarget(arachne.getTarget());
                    drider.initialize(arachne.getWorld().getServer().getWorld(arachne.getWorld().getRegistryKey()), arachne.world.getLocalDifficulty(drider.getBlockPos()), SpawnReason.REINFORCEMENT, null, null);
                    arachne.getWorld().getServer().getWorld(arachne.getWorld().getRegistryKey()).spawnEntityAndPassengers(drider);
                    break;
                }
            }
        }
    }
}
