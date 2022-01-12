package net.arathain.tot.common.entity;

import net.arathain.tot.common.entity.goal.DriderAttackGoal;
import net.arathain.tot.common.init.ToTObjects;
import net.minecraft.client.render.entity.model.SpiderEntityModel;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Hand;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.IAnimationTickable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import javax.annotation.Nullable;

public class DriderEntity extends SpiderEntity implements IAnimatable, IAnimationTickable {
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
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(3, new PounceAtTargetGoal(this, 0.4f));
        this.goalSelector.add(4, new DriderAttackGoal(this, 2.0, false));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(6, new LookAroundGoal(this));
        this.targetSelector.add(1, new RevengeGoal(this, new Class[0]));
        this.targetSelector.add(2, new TargetGoal<PlayerEntity>(this, PlayerEntity.class));
        this.targetSelector.add(3, new TargetGoal<IronGolemEntity>(this, IronGolemEntity.class));
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

    @Override
    protected float turnHead(float bodyRotation, float headRotation) {
        return super.turnHead(bodyRotation, headRotation);
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
        AnimationBuilder animationBuilder = new AnimationBuilder();
        boolean isMoving = event.isMoving() || this.forwardSpeed != 0;

        if(this.hasVehicle() && this.getVehicle() instanceof HorseEntity || this.getVehicle() instanceof PigEntity) {
            animationBuilder.addAnimation("horse", false);
        }
        if(!this.hasVehicle() && event.isMoving() || isMoving) {
            if(this.isSneaking()) {
                animationBuilder.addAnimation("sneakForward", true);
            } else {
                animationBuilder.addAnimation("walkForward", true);
            }
        } else if(!this.hasVehicle()) {
            if(this.isSneaking()) {
                animationBuilder.addAnimation("idleSneak", true);
            } else {
                animationBuilder.addAnimation("idle", true);
            }
        }
        if(this.isAttacking()) {
            animationBuilder.addAnimation("punch", true);
        }
        if(!animationBuilder.getRawAnimationList().isEmpty()) {
            event.getController().setAnimation(animationBuilder);
        }
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController<>(this, "controller", 3, this::predicate));
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
        this.dataTracker.startTracking(TYPE, type.toString());
    }


    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    @Override
    public void onDeath(DamageSource source) {
        if (this.getRandom().nextInt(20) == 3) {
            this.dropStack(this.getMainHandStack());
        }

        super.onDeath(source);
    }

    @Override
    public int tickTimer() {
        return age;
    }

    static class TargetGoal<T extends LivingEntity>
            extends ActiveTargetGoal<T> {
        public TargetGoal(SpiderEntity spider, Class<T> targetEntityClass) {
            super((MobEntity)spider, targetEntityClass, true);
        }

        @Override
        public boolean canStart() {
            float f = this.mob.getBrightnessAtEyes();
            if (f >= 0.5f) {
                return false;
            }
            return super.canStart();
        }
    }

    public enum Type {
        DARK,
        ALBINO,
        ARATHAIN
    }
}
