package net.arathain.tot.common.init;

import net.arathain.tot.TomeOfTiamatha;
import net.arathain.tot.common.entity.DriderEntity;
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
    public static final EntityType<DriderEntity> DRIDER = createEntity("drider", DriderEntity.createDriderAttributes(), FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, DriderEntity::new).dimensions(EntityDimensions.fixed(0.9F, 1.4F)).build());

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
