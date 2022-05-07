package net.arathain.tot.common.entity.living.drider.weaver;

import net.arathain.tot.common.entity.living.drider.DriderEntity;
import net.arathain.tot.common.entity.living.drider.arachne.ArachneEntity;
import net.arathain.tot.common.entity.living.goal.*;
import net.arathain.tot.common.init.ToTEffects;
import net.arathain.tot.common.util.ToTUtil;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

import java.util.Optional;

public class WeaverEntity extends DriderEntity implements RangedAttackMob {
    public static final TrackedData<Integer> ACTION_STATE = DataTracker.registerData(WeaverEntity.class, TrackedDataHandlerRegistry.INTEGER);
    public WeaverEntity(EntityType<? extends SpiderEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createWeaverAttributes() {
        return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_FOLLOW_RANGE, 128.0).add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.35f).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.0).add(EntityAttributes.GENERIC_ARMOR, 28.0);
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController<>(this, "controller", 5, this::predicate));
        animationData.addAnimationController(new AnimationController<>(this, "attackController", 3, this::attackPredicate));
    }
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(ACTION_STATE, 0);
    }
    public int getActionState() {
        return this.dataTracker.get(ACTION_STATE);
    }

    public void setActionState(int state) {
        this.dataTracker.set(ACTION_STATE, state);
    }

    protected void copyEntityData(Entity entity) {
        entity.setBodyYaw(this.getYaw());
        float f = MathHelper.wrapDegrees(entity.getYaw() - this.getYaw());
        float g = MathHelper.clamp(f, -105.0f, 105.0f);
        entity.prevYaw += g - f;
        entity.setYaw(entity.getYaw() + g - f);
        entity.setHeadYaw(entity.getYaw());
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(4, new BowAttackGoal<>(this, 1, 20, 20) {
            @Override
            protected boolean isHoldingBow() {
                return super.isHoldingBow() && this.actor.getTarget() != null && !this.actor.getTarget().hasStatusEffect(ToTEffects.BROODS_CURSE) && !this.actor.hasPassengers();
            }

            @Override
            public boolean shouldContinue() {
                return super.shouldContinue() && this.actor.getTarget() != null && !this.actor.getTarget().hasStatusEffect(ToTEffects.BROODS_CURSE) && !this.actor.hasPassengers();
            }
        });
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(1, new WeaverPickUpWebbingGoal(this));
        this.goalSelector.add(1, new WeaverImprisonTargetGoal(this));
        this.goalSelector.add(0 , new WeaverDepositWebbingGoal(this, Blocks.CHISELED_DEEPSLATE));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(6, new LookAroundGoal(this));
        this.targetSelector.add(1, new RevengeGoal(this, SpiderEntity.class).setGroupRevenge());
        this.targetSelector.add(2, new TargetGoal<>(this, PlayerEntity.class, 10, true, false, player -> !ToTUtil.isDrider(player)));
        this.targetSelector.add(2, new TargetGoal<>(this, IronGolemEntity.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        updatePassengerPosition(this.getFirstPassenger());
    }

    @Override
    public boolean canBeControlledByRider() {
        return false;
    }
    @Override
    public void updatePassengerPosition(Entity passenger) {
        if (!this.hasPassenger(passenger)) {
            return;
        }
        float g = (float)((this.isRemoved() ? (double)0.01f : this.getMountedHeightOffset()) + passenger.getHeightOffset());
        Vec3d i = new Vec3d(-1.2f, 0.0, 0.0).rotateY(-this.bodyYaw * ((float)Math.PI / 180) - 1.5707964f);
        passenger.setPosition(this.getX() - i.x, this.getY() + (double)g - 0.4f, this.getZ() - i.z);
        passenger.setYaw(this.getYaw());
        passenger.setHeadYaw(this.getHeadYaw());
        this.copyEntityData(passenger);
        if (passenger instanceof AnimalEntity && this.getPassengerList().size() > 1) {
            int j = passenger.getId() % 2 == 0 ? 90 : 270;
            passenger.setBodyYaw(((AnimalEntity)passenger).bodyYaw + (float)j);
            passenger.setHeadYaw(passenger.getHeadYaw() + (float)j);
        }
    }

    @Override
    protected void initEquipment(LocalDifficulty difficulty) {
        this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        AnimationBuilder animationBuilder = new AnimationBuilder();
        if(!this.hasVehicle() && event.isMoving()) {
            animationBuilder.addAnimation("walkForward", true);
        } else if(!this.hasVehicle()) {
            animationBuilder.addAnimation("idle", true);
        }

        if(!animationBuilder.getRawAnimationList().isEmpty()) {
            event.getController().setAnimation(animationBuilder);
        }
        return PlayState.CONTINUE;
    }

    private <E extends IAnimatable> PlayState attackPredicate(AnimationEvent<E> event) {
        AnimationBuilder animationBuilder = new AnimationBuilder();

        if(this.hasPassengers()) {
            animationBuilder.addAnimation("hold", true);
        }

        if(!animationBuilder.getRawAnimationList().isEmpty()) {
            event.getController().setAnimation(animationBuilder);
            return PlayState.CONTINUE;
        }
        return PlayState.STOP;
    }
    //TODO: replace with custom projectile
    @Override
    public void attack(LivingEntity target, float pullProgress) {
        FallingBlockEntity thrown = new FallingBlockEntity(this.world, this.getX(), this.getY(), this.getZ(), Blocks.COBWEB.getDefaultState());
        double d = target.getX() - this.getX();
        double e = target.getBodyY(0.3333333333333333) - thrown.getY();
        double f = target.getZ() - this.getZ();
        double g = Math.sqrt(d * d + f * f);
        thrown.setVelocity(d, e + g * 0.1F, f);
        thrown.setHurtEntities(2.0F, 10);
        this.playSound(SoundEvents.ENTITY_SNOWBALL_THROW, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.world.spawnEntity(thrown);
    }
}
