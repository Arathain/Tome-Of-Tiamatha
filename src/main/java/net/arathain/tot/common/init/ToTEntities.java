package net.arathain.tot.common.init;

import net.arathain.tot.TomeOfTiamatha;
import net.arathain.tot.common.entity.living.DriderEntity;
import net.arathain.tot.common.entity.string.StringCollisionEntity;
import net.arathain.tot.common.entity.string.StringKnotEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.LinkedHashMap;
import java.util.Map;

public class ToTEntities {
    private static final Map<EntityType<?>, Identifier> ENTITY_TYPES = new LinkedHashMap<>();
    //entities
    public static final EntityType<DriderEntity> DRIDER = createEntity("drider", DriderEntity.createDriderAttributes(), FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, DriderEntity::new).dimensions(EntityDimensions.fixed(0.9F, 1.5F)).build());
        //string hell
        public static final EntityType<StringKnotEntity> STRING_KNOT = createEntity("string_knot", FabricEntityTypeBuilder.create(SpawnGroup.MISC, (EntityType.EntityFactory<StringKnotEntity>) StringKnotEntity::new).trackRangeBlocks(10).trackedUpdateRate(Integer.MAX_VALUE).forceTrackedVelocityUpdates(false).dimensions(EntityDimensions.fixed(0.6f, 0.6F)).spawnableFarFromPlayer().fireImmune().build());
        public static final EntityType<StringCollisionEntity> STRING_COLLISION = createEntity("string_collision", FabricEntityTypeBuilder.create(SpawnGroup.MISC, (EntityType.EntityFactory<StringCollisionEntity>) StringCollisionEntity::new).trackRangeBlocks(10).trackedUpdateRate(Integer.MAX_VALUE).forceTrackedVelocityUpdates(false).dimensions(EntityDimensions.fixed(0.25f, 0.25f)).disableSaving().disableSummon().fireImmune().build());

    private static <T extends Entity> EntityType<T> createEntity(String name, EntityType<T> type) {
        ENTITY_TYPES.put(type, new Identifier(TomeOfTiamatha.MODID, name));
        return type;
    }

    private static <T extends LivingEntity> EntityType<T> createEntity(String name, DefaultAttributeContainer.Builder attributes, EntityType<T> type) {
        FabricDefaultAttributeRegistry.register(type, attributes);
        ENTITY_TYPES.put(type, new Identifier(TomeOfTiamatha.MODID, name));
        return type;
    }

    public static void init() {
        ENTITY_TYPES.keySet().forEach(entityType -> Registry.register(Registry.ENTITY_TYPE, ENTITY_TYPES.get(entityType), entityType));
    }
}
