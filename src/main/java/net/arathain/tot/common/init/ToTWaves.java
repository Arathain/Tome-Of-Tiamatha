package net.arathain.tot.common.init;

import net.arathain.tot.common.entity.living.drider.arachne.waves.WaveSpawnEntry;
import net.minecraft.entity.EntityType;

import java.util.ArrayList;
import java.util.Random;

public class ToTWaves {
    public static final ArrayList<ArrayList<WaveSpawnEntry>> ARACHNE_WAVES = new ArrayList<>();

    public static void updateArachneWaves(Random random) {
        ARACHNE_WAVES.clear();
        for (int i = 1; i <= 10; i++) {
            ARACHNE_WAVES.add(new ArrayList<>());
        }
        ARACHNE_WAVES.get(0).add(new WaveSpawnEntry(EntityType.SPIDER, EntityType.CAVE_SPIDER, 6, random.nextInt(5) == 2));
        ARACHNE_WAVES.get(0).add(new WaveSpawnEntry(ToTEntities.DRIDER, 2));

        ARACHNE_WAVES.get(1).add(new WaveSpawnEntry(EntityType.CAVE_SPIDER, ToTEntities.WEAVETHRALL, 10, random.nextInt(5) != 2));
        ARACHNE_WAVES.get(1).add(new WaveSpawnEntry(ToTEntities.DRIDER, 4));

        ARACHNE_WAVES.get(2).add(new WaveSpawnEntry(ToTEntities.WEAVECHILD, ToTEntities.WEAVETHRALL, 10, random.nextInt(3) == 2));
        ARACHNE_WAVES.get(2).add(new WaveSpawnEntry(ToTEntities.DRIDER, 6));

        ARACHNE_WAVES.get(3).add(new WaveSpawnEntry(ToTEntities.WEAVECHILD, 100));
    }
}
