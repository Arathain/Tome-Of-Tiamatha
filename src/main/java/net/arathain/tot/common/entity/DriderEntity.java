package net.arathain.tot.common.entity;

import net.arathain.tot.common.entity.goal.DriderAttackGoal;
import net.arathain.tot.common.entity.goal.DriderShieldGoal;
import net.arathain.tot.common.init.ToTObjects;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.MathHelper;
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
import java.util.UUID;

public class DriderEntity extends SpiderEntity implements IAnimatable, IAnimationTickable {
    private static final UUID SHIELD_UUID = UUID.fromString("b57b8070-1020-47f1-9429-e742793892df");
    private static final EntityAttributeModifier SHIELD_SPEED_PENALTY = new EntityAttributeModifier(SHIELD_UUID, "Use item speed penalty", -0.25D, EntityAttributeModifier.Operation.ADDITION);
    private final AnimationFactory factory = new AnimationFactory(this);
    public static final TrackedData<String> TYPE = DataTracker.registerData(DriderEntity.class, TrackedDataHandlerRegistry.STRING);


    public static DefaultAttributeContainer.Builder createDriderAttributes() {
        return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_FOLLOW_RANGE, 35.0).add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.2f).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.0).add(EntityAttributes.GENERIC_ARMOR, 6.0);
    }
    public int shieldCooldown;
    private boolean player;
    public DriderEntity(EntityType<? extends SpiderEntity> entityType, World world) {
        super(entityType, world);
        this.ignoreCameraFrustum = false;
        this.stepHeight = 2f;
        this.setPersistent();
    }

    public void setPlayer(boolean player) {
        this.player = player;
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new DriderAttackGoal(this, 2.0, false));
        this.goalSelector.add(0, new DriderShieldGoal(this));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(6, new LookAroundGoal(this));
        this.targetSelector.add(1, new RevengeGoal(this, new Class[0]));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, 10, true, false, player -> !ToTUtil.isDrider(player)));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, IronGolemEntity.class, true));
        this.targetSelector.add(3, new TargetGoal<>(this, IronGolemEntity.class));
    }

    @Override
    protected void initEquipment(LocalDifficulty difficulty) {
        this.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.DIAMOND_SWORD));
        this.setStackInHand(Hand.OFF_HAND, new ItemStack(Items.SHIELD));
        this.equipStack(EquipmentSlot.HEAD, ToTObjects.SILKSTEEL_HELMET.getDefaultStack());
        this.equipStack(EquipmentSlot.CHEST, ToTObjects.SILKSTEEL_CHESTPLATE.getDefaultStack());
    }
    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityTag) {
        this.initEquipment(difficulty);

        return super.initialize(world, difficulty, spawnReason, entityData, entityTag);
    }
    public void setUseTimeLeft(int timeLeft) {
        itemUseTimeLeft = timeLeft;
    }

    @Override
    public boolean isBlocking() {
        if(player) {
             if (this.activeItemStack.isEmpty()) {
                 return false;
             }
             Item item = this.activeItemStack.getItem();
             if (item.getUseAction(this.activeItemStack) != UseAction.BLOCK) {
                 return false;
             }
            return item.getMaxUseTime(this.activeItemStack) - this.itemUseTimeLeft >= 5;
        }
        return super.isBlocking();
    }

    protected void initDataTracker() {
        super.initDataTracker();

        if (this.random.nextInt(3) == 0) {
            this.dataTracker.startTracking(TYPE, Type.ALBINO.toString());
        } else {
            this.dataTracker.startTracking(TYPE, Type.DARK.toString());
        }
    }
    public void setActiveItemStack(ItemStack stack) {
        activeItemStack = stack;
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
            animationBuilder.addAnimation("punch", false);
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
        this.shieldCooldown = tag.getInt("shieldCooldown");
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound tag) {
        super.writeCustomDataToNbt(tag);

        tag.putString("Type", this.getDriderType().toString());
        tag.putInt("shieldCooldown", this.shieldCooldown);
    }

    public Type getDriderType() {
        return Type.valueOf(this.dataTracker.get(TYPE));
    }

    public void setDriderType(Type type) {
        this.dataTracker.startTracking(TYPE, type.toString());
    }

    @Override
    public void tickMovement() {
        if (this.shieldCooldown > 0) {
            --this.shieldCooldown;
        }
        super.tickMovement();
    }

    @Override
    protected void takeShieldHit(LivingEntity attacker) {
        super.takeShieldHit(attacker);
        System.out.println(this.isBlocking());
        if (attacker.getMainHandStack().getUseAction() == UseAction.BLOCK)
            this.disableShield(true);
    }
    @Override
    protected void damageShield(float damage) {
        if (this.activeItemStack.getUseAction() == UseAction.BLOCK) {
            if (damage >= 3.0F) {
                int i = 1 + MathHelper.floor(damage);
                Hand hand = this.getActiveHand();
                this.activeItemStack.damage(i, this, (entity) -> entity.sendToolBreakStatus(hand));
                if (this.activeItemStack.isEmpty()) {
                    if (hand == Hand.MAIN_HAND) {
                        this.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                    } else {
                        this.equipStack(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
                    }
                    this.activeItemStack = ItemStack.EMPTY;
                    this.playSound(SoundEvents.ITEM_SHIELD_BREAK, 0.8F, 0.8F + this.world.random.nextFloat() * 0.4F);
                }
            }
        }
    }
    @Override
    public void stopUsingItem() {
        if (this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).hasModifier(SHIELD_SPEED_PENALTY))
            this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).removeModifier(SHIELD_SPEED_PENALTY);
        super.stopUsingItem();
    }
    @Override
    public void setCurrentHand(Hand hand) {
        ItemStack itemstack = this.getStackInHand(hand);
        if (itemstack.getUseAction() == UseAction.BLOCK) {
            EntityAttributeInstance modifiableattributeinstance = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
            modifiableattributeinstance.removeModifier(SHIELD_SPEED_PENALTY);
            modifiableattributeinstance.addTemporaryModifier(SHIELD_SPEED_PENALTY);
        }
        super.setCurrentHand(hand);
    }

    public void disableShield(boolean increase) {
        float chance = 0.25F + (float) EnchantmentHelper.getEfficiency(this) * 0.05F;
        if (increase)
            chance += 0.75;
        if (this.random.nextFloat() < chance) {
            this.shieldCooldown = 100;
            this.stopUsingItem();
            this.world.sendEntityStatus(this, (byte) 30);
        }
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
