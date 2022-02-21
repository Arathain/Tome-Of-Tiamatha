package net.arathain.tot.common.entity.living.raven;

import net.arathain.tot.common.init.ToTObjects;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.ServerConfigHandler;
import net.minecraft.server.command.GiveCommand;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.IAnimationTickable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.Optional;
import java.util.UUID;

public class RavenEntity extends TameableEntity implements IAnimatable, IAnimationTickable {
    private static final TrackedData<Optional<UUID>> RECEIVER_UUID = DataTracker.registerData(RavenEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
    private final AnimationFactory factory = new AnimationFactory(this);
    private static final TrackedData<Boolean> SITTING = DataTracker.registerData(RavenEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    public RavenEntity(EntityType<? extends TameableEntity> type, World world) {
        super(type, world);
        moveControl = new FlightMoveControl(this, 90, false);
    }
    @Override
    protected void initGoals() {
        goalSelector.add(1, new SwimGoal(this));
        goalSelector.add(2, new SitGoal(this));
        goalSelector.add(3, new MeleeAttackGoal(this, 1, true));
        goalSelector.add(4, new FollowOwnerGoal(this, 1, 10, 2, false));
        goalSelector.add(5, new AnimalMateGoal(this, 1));
        goalSelector.add(6, new WanderAroundFarGoal(this, 1));
        goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 8));
        goalSelector.add(7, new LookAroundGoal(this));
        targetSelector.add(0, new TrackOwnerAttackerGoal(this));
        targetSelector.add(1, new AttackWithOwnerGoal(this));
        targetSelector.add(2, new RevengeGoal(this).setGroupRevenge());
    }
    public static DefaultAttributeContainer.Builder createRavenAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 20).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3).add(EntityAttributes.GENERIC_FLYING_SPEED, 0.7);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("sitting", isSitting());
        if (this.getReceiverUuid() != null) {
            nbt.putUuid("Receiver", this.getReceiverUuid());
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        setSitting(nbt.getBoolean("sitting"));
        if (nbt.containsUuid("Receiver")) {
            setReceiverUuid(nbt.getUuid("Receiver"));
        } else {
            String string = nbt.getString("Receiver");
            setReceiverUuid(ServerConfigHandler.getPlayerUuidByName(this.getServer(), string));
        }
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        BirdNavigation birdNavigation = new BirdNavigation(this, world);
        birdNavigation.setCanPathThroughDoors(false);
        birdNavigation.setCanSwim(true);
        birdNavigation.setCanEnterOpenDoors(true);
        return birdNavigation;
    }
    protected void initDataTracker() {
        super.initDataTracker();

        this.dataTracker.startTracking(SITTING, false);
        this.dataTracker.startTracking(RECEIVER_UUID, Optional.empty());
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return (stack.isIn(ToTObjects.MEAT) || (stack.getItem().getFoodComponent() != null && stack.getItem().getFoodComponent().isMeat()));
    }

    @Override
    protected void mobTick() {
        super.mobTick();
        ItemStack stack = this.getStackInHand(Hand.MAIN_HAND);
        if(!stack.isEmpty() && stack.hasCustomName()) {
            PlayerEntity entity = getServer().getPlayerManager().getPlayer(stack.getName().asString());
            if(entity != null && entity.getUuid() != null) {
                this.setReceiverUuid(entity.getUuid());
            }
        }
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if(stack.getItem().equals(Items.BUNDLE) && stack.hasCustomName()) {
            if (!this.world.isClient) {
                this.setStackInHand(Hand.MAIN_HAND, stack);
                player.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
            }
            return ActionResult.success(this.world.isClient);
        }
        if(stack.isEmpty() && this.getStackInHand(Hand.MAIN_HAND).getItem().equals(Items.BUNDLE)) {
            if (!this.world.isClient) {
                player.setStackInHand(Hand.MAIN_HAND, this.getStackInHand(Hand.MAIN_HAND));
                this.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
            }
            return ActionResult.success(this.world.isClient);
        }
        if (this.isOnGround() && this.isTamed() && this.isOwner(player) && stack.isEmpty()) {
            if (!this.world.isClient) {
                this.setSitting(!this.isSitting());
            }

            return ActionResult.success(this.world.isClient);
        }
        if (!isTamed()) {
            if (isBreedingItem(stack)) {
                if (!world.isClient()) {
                    eat(player, hand, stack);
                    if (random.nextInt(4) == 0) {
                        setOwner(player);
                        setSitting(true);
                        setTarget(null);
                        navigation.stop();
                        world.sendEntityStatus(this, (byte) 7);
                    } else {
                        world.sendEntityStatus(this, (byte) 6);
                    }
                }
                return ActionResult.success(world.isClient());
            }
        } else if (isBreedingItem(stack)) {
            if (getHealth() < getMaxHealth()) {
                if (!world.isClient()) {
                    eat(player, hand, stack);
                    heal(4);
                }
                return ActionResult.success(world.isClient());
            }
        }
        return super.interactMob(player, hand);
    }

    public UUID getReceiverUuid() {
        return (UUID) ((Optional) this.dataTracker.get(RECEIVER_UUID)).orElse(null);
    }

    public void setReceiverUuid(@Nullable UUID uuid) {
        this.dataTracker.set(RECEIVER_UUID, Optional.ofNullable(uuid));
    }
    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(SoundEvents.ENTITY_PARROT_STEP, 0.15f, 1);
    }

    @Override
    protected void addFlapEffects() {
        playSound(SoundEvents.ENTITY_PARROT_FLY, 0.15f, 1);
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition) {
    }

    @Override
    protected boolean hasWings() {
        return true;
    }


    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController<>(this, "controller", 3, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        AnimationBuilder animationBuilder = new AnimationBuilder();
        if(!this.isOnGround()) {
            animationBuilder.addAnimation(Math.abs(getVelocity().y) > 0.05f ? "flyFast" : "fly", true);
            event.getController().setAnimation(animationBuilder);
        } else if(!dataTracker.get(SITTING)) {
            animationBuilder.addAnimation("idle", true);
            event.getController().setAnimation(animationBuilder);
        } else {
            animationBuilder.addAnimation("sitIdle", true);
            event.getController().setAnimation(animationBuilder);
        }
        return PlayState.CONTINUE;
    }

    @Override
    public boolean canAttackWithOwner(LivingEntity target, LivingEntity owner) {
        if (target instanceof TameableEntity && ((TameableEntity) target).isTamed()) {
            return false;
        }
        if (target instanceof HorseBaseEntity && ((HorseBaseEntity) target).isTame()) {
            return false;
        }
        if (target instanceof PlayerEntity player && owner instanceof PlayerEntity playerOwner && !playerOwner.shouldDamagePlayer(player)) {
            return false;
        }
        return !(target instanceof CreeperEntity) && !(target instanceof GhastEntity);
    }

    @Override
    public boolean isSitting() {
        return this.dataTracker.get(SITTING);
    }

    @Override
    public void setSitting(boolean sitting) {
        this.dataTracker.set(SITTING, sitting);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (isInvulnerableTo(source)) {
            return false;
        } else {
            Entity entity = source.getAttacker();
            setSitting(false);
            if (entity != null && !(entity instanceof PlayerEntity) && !(entity instanceof PersistentProjectileEntity)) {
                amount = (amount + 1) / 2f;
            }
            return super.damage(source, amount);
        }
    }

    @Override
    public int tickTimer() {
        return age;
    }
}