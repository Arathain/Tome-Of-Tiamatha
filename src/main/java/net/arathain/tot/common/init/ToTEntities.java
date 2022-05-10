package net.arathain.tot.common.init;

import net.arathain.tot.TomeOfTiamatha;
import net.arathain.tot.common.block.WeaverkinEggBlock;
import net.arathain.tot.common.block.entity.WeaverkinEggBlockEntity;
import net.arathain.tot.common.entity.living.drider.DriderEntity;
import net.arathain.tot.common.entity.living.drider.arachne.ArachneEntity;
import net.arathain.tot.common.entity.living.drider.weavekin.WeavechildEntity;
import net.arathain.tot.common.entity.living.drider.weavekin.WeavethrallEntity;
import net.arathain.tot.common.entity.living.drider.weaver.WeaverEntity;
import net.arathain.tot.common.entity.living.drider.weaver.WebbingEntity;
import net.arathain.tot.common.entity.living.raven.NevermoreEntity;
import net.arathain.tot.common.entity.living.raven.RavenEntity;
import net.arathain.tot.common.entity.string.StringCollisionEntity;
import net.arathain.tot.common.entity.string.StringKnotEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.LinkedHashMap;
import java.util.Map;

public class ToTEntities {
    private static final Map<EntityType<?>, Identifier> ENTITY_TYPES = new LinkedHashMap<>();
    private static final Map<BlockEntityType<?>, Identifier> BLOCK_ENTITY_TYPES = new LinkedHashMap<>();
    //entities
    //drider stuffs
    public static final EntityType<DriderEntity> DRIDER = createEntity("drider", DriderEntity.createDriderAttributes(), FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, DriderEntity::new).dimensions(EntityDimensions.fixed(0.9F, 1.5F)).build());
    public static final EntityType<WeavechildEntity> WEAVECHILD = createEntity("weavechild", WeavechildEntity.createWeavechildAttributes(), FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, WeavechildEntity::new).dimensions(EntityDimensions.fixed(0.4F, 0.6F)).build());
    public static final EntityType<WeavethrallEntity> WEAVETHRALL = createEntity("weavethrall", WeavethrallEntity.createWeavethrallAttributes(), FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, WeavethrallEntity::new).dimensions(EntityDimensions.fixed(0.95F, 0.8F)).build());
    public static final EntityType<ArachneEntity> ARACHNE = createEntity("arachne", ArachneEntity.createArachneAttributes(), FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, ArachneEntity::new).dimensions(EntityDimensions.fixed(1.4F, 1.6F)).build());
    public static final EntityType<WeaverEntity> WEAVER = createEntity("weaver", WeaverEntity.createWeaverAttributes(), FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, WeaverEntity::new).dimensions(EntityDimensions.fixed(0.9F, 1.5F)).build());
    public static final EntityType<WebbingEntity> WEBBING = createEntity("webbing", FabricEntityTypeBuilder.<WebbingEntity>create(SpawnGroup.MISC, WebbingEntity::new).trackRangeBlocks(10).dimensions(EntityDimensions.fixed(0.9f, 1.8F)).build());
    // ravens n shiz
    public static final EntityType<NevermoreEntity> NEVERMORE = createEntity("nevermore", NevermoreEntity.createNevermoreAttributes(), FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, NevermoreEntity::new).dimensions(EntityDimensions.fixed(0.8F, 1.85F)).build());
    public static final EntityType<RavenEntity> RAVEN = createEntity("raven", RavenEntity.createRavenAttributes(), FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, RavenEntity::new).dimensions(EntityDimensions.fixed(0.4F, 0.4F)).build());

    //string hell
    public static final EntityType<StringKnotEntity> STRING_KNOT = createEntity("string_knot", FabricEntityTypeBuilder.create(SpawnGroup.MISC, (EntityType.EntityFactory<StringKnotEntity>) StringKnotEntity::new).trackRangeBlocks(10).trackedUpdateRate(Integer.MAX_VALUE).forceTrackedVelocityUpdates(false).dimensions(EntityDimensions.fixed(0.6f, 0.6F)).spawnableFarFromPlayer().fireImmune().build());
    public static final EntityType<StringCollisionEntity> STRING_COLLISION = createEntity("string_collision", FabricEntityTypeBuilder.create(SpawnGroup.MISC, (EntityType.EntityFactory<StringCollisionEntity>) StringCollisionEntity::new).trackRangeBlocks(10).trackedUpdateRate(Integer.MAX_VALUE).forceTrackedVelocityUpdates(false).dimensions(EntityDimensions.fixed(0.25f, 0.25f)).disableSaving().disableSummon().fireImmune().build());


    //block entities
    public static final BlockEntityType<WeaverkinEggBlockEntity> WEAVERKIN_EGG = createBlockEntity("weaverkin_egg", FabricBlockEntityTypeBuilder.create(WeaverkinEggBlockEntity::new, ToTObjects.WEAVEKIN_EGG).build(null));

    private static <T extends Entity> EntityType<T> createEntity(String name, EntityType<T> type) {
        ENTITY_TYPES.put(type, new Identifier(TomeOfTiamatha.MODID, name));
        return type;
    }
    private static <T extends BlockEntity> BlockEntityType<T> createBlockEntity(String name, BlockEntityType<T> type) {
        BLOCK_ENTITY_TYPES.put(type, new Identifier(TomeOfTiamatha.MODID, name));
        return type;
    }

    private static <T extends LivingEntity> EntityType<T> createEntity(String name, DefaultAttributeContainer.Builder attributes, EntityType<T> type) {
        FabricDefaultAttributeRegistry.register(type, attributes);
        ENTITY_TYPES.put(type, new Identifier(TomeOfTiamatha.MODID, name));
        return type;
    }

    public static void init() {
        ENTITY_TYPES.keySet().forEach(entityType -> Registry.register(Registry.ENTITY_TYPE, ENTITY_TYPES.get(entityType), entityType));
        BLOCK_ENTITY_TYPES.keySet().forEach(entityType -> Registry.register(Registry.BLOCK_ENTITY_TYPE, BLOCK_ENTITY_TYPES.get(entityType), entityType));
    }
}
