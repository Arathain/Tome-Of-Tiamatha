package net.arathain.tot.common.entity.living.drider.arachne.waves;

import net.arathain.tot.common.entity.living.entityinterface.Broodchild;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;

public class WaveSpawnEntry {
    public EntityType<? extends MobEntity> entityType;
    public int count;

    public WaveSpawnEntry(EntityType<? extends MobEntity> entityType, EntityType<? extends MobEntity> secondaryEntityType, int count, boolean useSecond) {
        this.entityType = useSecond ? secondaryEntityType : entityType;
        this.count = count;
    }
    public WaveSpawnEntry(EntityType<? extends MobEntity> entityType, int count) {
        this.entityType = entityType;
        this.count = count;
    }
}
