package net.arathain.tot.common.entity;

import net.arathain.tot.common.init.ToTObjects;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Hand;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import javax.annotation.Nullable;

public class DriderEntity extends SpiderEntity implements IAnimatable {
    private final AnimationFactory factory = new AnimationFactory(this);
    public static final TrackedData<String> TYPE = DataTracker.registerData(DriderEntity.class, TrackedDataHandlerRegistry.STRING);


    public static DefaultAttributeContainer.Builder createDriderAttributes() {
        return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_FOLLOW_RANGE, 35.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.4f).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.0).add(EntityAttributes.GENERIC_ARMOR, 6.0);
    }
    public DriderEntity(EntityType<? extends SpiderEntity> entityType, World world) {
        super(entityType, world);
        this.ignoreCameraFrustum = false;
        this.stepHeight = 2f;
    }

    @Override
    protected void initGoals() {
        super.initGoals();
    }

    @Override
    protected void initEquipment(LocalDifficulty difficulty) {
        this.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.DIAMOND_SWORD));
        this.setStackInHand(Hand.OFF_HAND, new ItemStack(Items.DIAMOND_SWORD));
        this.equipStack(EquipmentSlot.HEAD, ToTObjects.SILKSTEEL_HELMET.getDefaultStack());
        this.equipStack(EquipmentSlot.CHEST, ToTObjects.SILKSTEEL_CHESTPLATE.getDefaultStack());
    }
    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityTag) {
        this.initEquipment(difficulty);

        return super.initialize(world, difficulty, spawnReason, entityData, entityTag);
    }

    protected void initDataTracker() {
        super.initDataTracker();

        if (this.random.nextInt(3) == 0) {
            this.dataTracker.startTracking(TYPE, Type.ALBINO.toString());
        } else {
            this.dataTracker.startTracking(TYPE, Type.DARK.toString());
        }
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        return PlayState.STOP;
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound tag) {
        super.readCustomDataFromNbt(tag);

        if (tag.contains("Type")) {
            this.setDriderType(Type.valueOf(tag.getString("Type")));
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound tag) {
        super.writeCustomDataToNbt(tag);

        tag.putString("Type", this.getDriderType().toString());
    }

    public Type getDriderType() {
        return Type.valueOf(this.dataTracker.get(TYPE));
    }

    public void setDriderType(Type type) {
        this.dataTracker.set(TYPE, type.toString());
    }


    @Override
    public AnimationFactory getFactory() {
        return factory;
    }

    @Override
    public void onDeath(DamageSource source) {
        if (this.getRandom().nextInt(20) == 3) {
            this.dropStack(this.getMainHandStack());
        }

        super.onDeath(source);
    }

    public enum Type {
        DARK,
        ALBINO
    }
}
