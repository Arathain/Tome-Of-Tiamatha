package net.arathain.tot.common.init;

import net.arathain.tot.common.entity.living.drider.arachne.waves.WaveSpawnEntry;
import net.minecraft.entity.EntityType;

import java.util.ArrayList;
import java.util.Random;

public class ToTWaves {
    public static final ArrayList<ArrayList<WaveSpawnEntry>> ARACHNE_WAVES = new ArrayList<>();

    public static void updateArachneWaves(Random random, int dangerScale) {
        ARACHNE_WAVES.clear();
        for (int i = 1; i <= 9; i++) {
            ARACHNE_WAVES.add(new ArrayList<>());
        }
        ARACHNE_WAVES.get(0).add(new WaveSpawnEntry(EntityType.SPIDER, EntityType.CAVE_SPIDER, 4 * dangerScale, random.nextInt(5) == 2));
        ARACHNE_WAVES.get(0).add(new WaveSpawnEntry(ToTEntities.DRIDER, 2));

        ARACHNE_WAVES.get(1).add(new WaveSpawnEntry(EntityType.CAVE_SPIDER, ToTEntities.WEAVETHRALL, 6 * dangerScale, random.nextInt(5) != 2));
        ARACHNE_WAVES.get(1).add(new WaveSpawnEntry(ToTEntities.DRIDER, 2 * dangerScale));

        ARACHNE_WAVES.get(2).add(new WaveSpawnEntry(ToTEntities.WEAVECHILD, ToTEntities.WEAVETHRALL, 8 * dangerScale, random.nextInt(3) == 2));
        ARACHNE_WAVES.get(2).add(new WaveSpawnEntry(ToTEntities.DRIDER, 2 * dangerScale));

        ARACHNE_WAVES.get(3).add(new WaveSpawnEntry(ToTEntities.DRIDER, 2 * dangerScale));

        ARACHNE_WAVES.get(4).add(new WaveSpawnEntry(ToTEntities.WEAVECHILD, 5 * dangerScale));
        ARACHNE_WAVES.get(4).add(new WaveSpawnEntry(ToTEntities.WEAVETHRALL, 5 * dangerScale));

        ARACHNE_WAVES.get(5).add(new WaveSpawnEntry(EntityType.SPIDER, EntityType.CAVE_SPIDER, 12 * dangerScale, random.nextInt(5) == 2));

        ARACHNE_WAVES.get(6).add(new WaveSpawnEntry(ToTEntities.WEAVECHILD, 5 * dangerScale));
        ARACHNE_WAVES.get(6).add(new WaveSpawnEntry(ToTEntities.DRIDER, 4 * dangerScale));

        ARACHNE_WAVES.get(7).add(new WaveSpawnEntry(ToTEntities.WEAVECHILD, 30 * dangerScale));

        ARACHNE_WAVES.get(8).add(new WaveSpawnEntry(ToTEntities.WEAVETHRALL, 12 * dangerScale));
        ARACHNE_WAVES.get(8).add(new WaveSpawnEntry(ToTEntities.DRIDER, 8 * dangerScale));
    }
}
