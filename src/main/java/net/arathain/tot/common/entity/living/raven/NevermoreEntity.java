package net.arathain.tot.common.entity.living.raven;

import net.arathain.tot.common.init.ToTComponents;
import net.arathain.tot.common.init.ToTObjects;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TimeHelper;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.UUID;

public class NevermoreEntity extends MerchantEntity implements IAnimatable, Angerable {
    private int angerTime;
    private @Nullable UUID angryAt;
    private static final UniformIntProvider ANGER_TIME_RANGE = TimeHelper.betweenSeconds(60, 790);
    public static final TrackedData<Integer> ATTACK_STATE = DataTracker.registerData(NevermoreEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private final AnimationFactory factory = new AnimationFactory(this);
    public static final TradeOffers.Factory[] TRADES = new TradeOffers.Factory[]{
            (entity, random) -> new TradeOffer(new ItemStack(Items.BONE_BLOCK, 60), ToTObjects.REMORSE.getDefaultStack(), 2, 1, 0.05f),
            (entity, random) -> new TradeOffer(new ItemStack(Items.BONE_BLOCK, 40), ToTObjects.REMEMBRANCE_TOKEN.getDefaultStack(), 2, 1, 0.05f)
    };

    protected NevermoreEntity(EntityType<? extends MerchantEntity> entityType, World world) {
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
        if (this.isAlive() && !this.hasCustomer() && !this.isBaby() && itemStack.isEmpty()) {
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
    public void registerControllers(AnimationData animationData) {

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
}