package net.arathain.tot.common.entity.living.raven;

import net.arathain.tot.common.entity.living.goal.NevermoreLookAtTargetGoal;
import net.arathain.tot.common.entity.living.goal.NevermoreSummonGoal;
import net.arathain.tot.common.entity.living.goal.NevermoreYeetGoal;
import net.arathain.tot.common.init.ToTComponents;
import net.arathain.tot.common.init.ToTObjects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TimeHelper;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.example.item.JackInTheBoxItem;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.IAnimationTickable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.UUID;

public class NevermoreEntity extends MerchantEntity implements IAnimatable, Angerable, IAnimationTickable {
    private int angerTime;
    private @Nullable UUID angryAt;
    private static final UniformIntProvider ANGER_TIME_RANGE = TimeHelper.betweenSeconds(300, 1790);
    public static final TrackedData<Integer> ATTACK_STATE = DataTracker.registerData(NevermoreEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private final AnimationFactory factory = new AnimationFactory(this);
    private RevengeGoal revengeGoal;
    public static final TradeOffers.Factory[] TRADES = new TradeOffers.Factory[]{
            (entity, random) -> new TradeOffer(new ItemStack(Items.BONE_BLOCK, 60), ToTObjects.REMORSE.getDefaultStack(), 2, 1, 0.05f),
            (entity, random) -> new TradeOffer(new ItemStack(Items.BONE_BLOCK, 40), ToTObjects.REMEMBRANCE_TOKEN.getDefaultStack(), 2, 1, 0.05f)
    };

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new StopFollowingCustomerGoal(this));
        this.goalSelector.add(1, new NevermoreLookAtTargetGoal(this));
        this.goalSelector.add(2, new LookAtCustomerGoal(this));
        this.goalSelector.add(3, new NevermoreYeetGoal(this));
        this.goalSelector.add(3, new NevermoreSummonGoal(this));
        this.goalSelector.add(5, new GoToWalkTargetGoal(this, 0.35D));
        this.goalSelector.add(8, new WanderAroundFarGoal(this, 0.35D));
        this.goalSelector.add(9, new StopAndLookAtEntityGoal(this, PlayerEntity.class, 3.0F, 1.0F));
        this.goalSelector.add(10, new LookAtEntityGoal(this, MobEntity.class, 8.0F));
        this.goalSelector.add(10, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.revengeGoal = new RevengeGoal(this).setGroupRevenge();
        this.targetSelector.add(1, revengeGoal);
        this.targetSelector.add(3, new TargetGoal<>(this, PlayerEntity.class, 10, true, false, this::shouldAngerAt));
    }

    @Override
    public boolean shouldAngerAt(LivingEntity livingEntity) {
        return Angerable.super.shouldAngerAt(livingEntity) || (livingEntity instanceof PlayerEntity player && ToTComponents.ALIGNMENT_COMPONENT.get(player).getRAlignment() < -80);
    }

    public static DefaultAttributeContainer.Builder createNevermoreAttributes() {
        return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_FOLLOW_RANGE, 128.0).add(EntityAttributes.GENERIC_MAX_HEALTH, 60.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.4f).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 10.0).add(EntityAttributes.GENERIC_ARMOR, 12.0);
    }

    public NevermoreEntity(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }
    @Override
    public boolean isLeveledMerchant() {
        return false;
    }

    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(ATTACK_STATE, 0);
    }

    @Override
    protected void afterUsing(TradeOffer offer) {

    }
    @Override
    public ActionResult interactMob(PlayerEntity customer, Hand hand) {
        ItemStack itemStack = customer.getStackInHand(hand);
        if (this.isAlive() && !this.hasCustomer() && !this.isBaby() && itemStack.isEmpty() && this.getTarget() == null) {
            if (hand == Hand.MAIN_HAND) {
                customer.incrementStat(Stats.TALKED_TO_VILLAGER);
            }

            if (!this.world.isClient && !this.getOffers().isEmpty()) {
                this.prepareOffersFor(customer);
                this.setCurrentCustomer(customer);
                this.sendOffers(customer, this.getDisplayName(), 1);
            }

            return ActionResult.success(this.world.isClient);
        } else {
            return super.interactMob(customer, hand);
        }
    }
    private void prepareOffersFor(PlayerEntity customer) {
        for (TradeOffer offer : this.getOffers()) {
            if(offer.getSellItem().getItem().equals(ToTObjects.REMEMBRANCE_TOKEN)) {
                if(ToTComponents.ALIGNMENT_COMPONENT.get(customer).getRAlignment() < 60) {

                } else {
                }
            }
        }
    }

    @Override
    public void stopAnger() {
        Angerable.super.stopAnger();
        this.revengeGoal.stop();
    }


    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("AttackState", getAttackState());
        this.writeAngerToNbt(nbt);
    }
    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setAttackState(nbt.getInt("AttackState"));
        this.readAngerFromNbt(this.world, nbt);
    }
    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }

    @Override
    protected void fillRecipes() {
        this.fillRecipesFromPool(this.getOffers(), TRADES, 7);
    }

    public int getAttackState() {
        return this.dataTracker.get(ATTACK_STATE);
    }

    public void setAttackState(int state) {
        this.dataTracker.set(ATTACK_STATE, state);
    }

    @Override
    public boolean startRiding(Entity entity, boolean force) {
        return false;
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController<>(this, "controller", 3, this::predicate));
    }
    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        AnimationBuilder animationBuilder = new AnimationBuilder();
        boolean isMoving = event.isMoving() || this.forwardSpeed != 0;
        if(this.getAttackState() == 1) {
            animationBuilder.addAnimation("yeet", true);
        }
        if(this.getAttackState() == 0) {
            if (isMoving) {
                animationBuilder.addAnimation("walk", true);
            } else if(!this.hasVehicle()) {
                animationBuilder.addAnimation("idle", true);
            }
        }

        if(!animationBuilder.getRawAnimationList().isEmpty()) {
            event.getController().setAnimation(animationBuilder);
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }

    @Override
    public int getAngerTime() {
        return angerTime;
    }

    @Override
    public void setAngerTime(int angerTime) {
        this.angerTime = angerTime;
    }

    @Override
    public @Nullable UUID getAngryAt() {
        return angryAt;
    }

    @Override
    public void forgive(PlayerEntity player) {
        // Never-nevermore
    }

    @Override
    public void setAngryAt(@Nullable UUID angryAt) {
        this.angryAt = angryAt;
    }

    @Override
    public void chooseRandomAngerTime() {
        this.setAngerTime(ANGER_TIME_RANGE.get(this.random));
    }
    public SoundEvent getAttackSound() {
        return SoundEvents.ENTITY_IRON_GOLEM_ATTACK;
    }

    @Override
    public int tickTimer() {
        return age;
    }
}
