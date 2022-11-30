package net.arathain.tot.common.entity.living.drider.weavekin;

import net.arathain.tot.common.entity.living.drider.DriderEntity;
import net.arathain.tot.common.entity.living.entityinterface.Broodchild;
import net.arathain.tot.common.entity.living.entityinterface.TameableHostileEntity;
import net.arathain.tot.common.entity.living.goal.ObedientRevengeGoal;
import net.arathain.tot.common.entity.living.goal.TamedAttackWithOwnerGoal;
import net.arathain.tot.common.entity.living.goal.TamedFollowOwnerGoal;
import net.arathain.tot.common.entity.living.goal.TamedTrackAttackerGoal;
import net.arathain.tot.common.init.ToTObjects;
import net.arathain.tot.common.util.ToTUtil;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.ServerConfigHandler;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

import java.util.Optional;
import java.util.UUID;

public class WeavethrallEntity extends WeavechildEntity implements TameableHostileEntity, Broodchild {
    private static final TrackedData<Byte> TAMEABLE = DataTracker.registerData(WeavethrallEntity.class, TrackedDataHandlerRegistry.BYTE);
    private static final TrackedData<Optional<UUID>> BINDER_UUID = DataTracker.registerData(WeavethrallEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
    public WeavethrallEntity(EntityType<? extends WeavechildEntity> entityType, World world) {
        super(entityType, world);
    }
    public static DefaultAttributeContainer.Builder createWeavethrallAttributes() {
        return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64.0).add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3f).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0).add(EntityAttributes.GENERIC_ARMOR, 7.0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(3, new TamedFollowOwnerGoal<>( this, 1.0D, 10.0F, 2.0F, true));
        this.goalSelector.add(3, new PounceAtTargetGoal(this, 0.4f));
        this.goalSelector.add(4, new AttackGoal(this));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(6, new LookAroundGoal(this));
        this.targetSelector.add(1, new ObedientRevengeGoal(this, DriderEntity.class).setGroupRevenge(ZombifiedPiglinEntity.class));
        this.targetSelector.add(1, new TamedTrackAttackerGoal(this));
        this.targetSelector.add(2, new TamedAttackWithOwnerGoal<>(this));
        this.targetSelector.add(1, new TargetGoal<>(this, AnimalEntity.class, 10, true, false, animol -> {
            if (isTamed()) return false;
            if (!(animol instanceof TameableEntity)) return true;
            TameableEntity tameable = (TameableEntity) animol;
            return tameable.getOwner() == null || tameable.getOwner() != null && !ToTUtil.isDrider(tameable.getOwner());
        }));
        this.targetSelector.add(2, new TargetGoal<>(this, PlayerEntity.class, 10, true, false, player -> !ToTUtil.isDrider(player) && !isTamed()));
        this.targetSelector.add(2, new TargetGoal<>(this, IronGolemEntity.class, 10, true, false, golem -> !isTamed()));
    }

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (this.world.isClient) {
            boolean bl = this.isOwner(player) || this.isTamed() || stack.isIn(ToTObjects.MEAT) && !this.isTamed() && !this.isAngryAt(player);
            return bl ? ActionResult.CONSUME : ActionResult.PASS;
        }
        if(stack.isFood() && stack.isIn(ToTObjects.MEAT) && ToTUtil.isDrider(player)) {
            if (!isTamed()) {
                if (!player.getAbilities().creativeMode) {
                    stack.decrement(1);
                }
                if(player.getRandom().nextInt(6) == 1) {
                    this.setOwner(player);
                    this.setTamed(true);
                    this.setOwnerUuid(player.getUuid());
                    this.reset();
                    this.emitGameEvent(GameEvent.ENTITY_INTERACT, this);
                    this.world.sendEntityStatus(this, (byte)7);
                    return ActionResult.SUCCESS;
                } else {
                    this.world.sendEntityStatus(this, (byte)6);
                }
            } else {
                if (!player.getAbilities().creativeMode) {
                    stack.decrement(1);
                }
                heal(stack.getItem().getFoodComponent().getHunger());
                this.emitGameEvent(GameEvent.ENTITY_INTERACT, this);
                return ActionResult.SUCCESS;
            }
        }
        return super.interactMob(player, hand);
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController<>(this, "controller", 3, this::predicate));
    }
    @Override
    public void handleStatus(byte status) {
        if (status == 7) {
            this.showEmoteParticle(true);
        } else if (status == 6) {
            this.showEmoteParticle(false);
        } else {
            super.handleStatus(status);
        }
    }
    protected void showEmoteParticle(boolean positive) {
        DefaultParticleType particleEffect = ParticleTypes.HEART;
        if (!positive) {
            particleEffect = ParticleTypes.SMOKE;
        }
        for (int i = 0; i < 7; ++i) {
            double d = this.random.nextGaussian() * 0.02;
            double e = this.random.nextGaussian() * 0.02;
            double f = this.random.nextGaussian() * 0.02;
            this.world.addParticle(particleEffect, this.getParticleX(1.0), this.getRandomBodyY() + 0.5, this.getParticleZ(1.0), d, e, f);
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        if (this.getOwnerUuid() != null) {
            nbt.putUuid("Owner", this.getOwnerUuid());
        }
    }
    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        UUID ownerUUID;
        if (nbt.containsUuid("Owner")) {
            ownerUUID = nbt.getUuid("Owner");
        } else {
            String string = nbt.getString("Owner");
            ownerUUID = ServerConfigHandler.getPlayerUuidByName(this.getServer(), string);
        }

        if (ownerUUID != null) {
            try {
                this.setOwnerUuid(ownerUUID);
                this.setTamed(true);
            } catch (Throwable var4) {
                this.setTamed(false);
            }
        }
    }
    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(TAMEABLE, (byte) 0);
        dataTracker.startTracking(BINDER_UUID, Optional.empty());
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        AnimationBuilder animationBuilder = new AnimationBuilder();
        if(event.isMoving()) {
            animationBuilder.addAnimation("walkForward", true);
        } else {
            animationBuilder.addAnimation("idle", true);
        }
        if(!animationBuilder.getRawAnimationList().isEmpty()) {
            event.getController().setAnimation(animationBuilder);
        }
        return PlayState.CONTINUE;
    }

    @Override
    public UUID getOwnerUuid() {
        return (UUID) ((Optional) this.dataTracker.get(BINDER_UUID)).orElse(null);
    }


    @Override
    public void setOwnerUuid(@Nullable UUID uuid) {
        this.dataTracker.set(BINDER_UUID, Optional.ofNullable(uuid));
    }

    @Override
    public void setOwner(PlayerEntity player) {
        this.setTamed(true);
        this.setOwnerUuid(player.getUuid());
    }

    @Nullable
    @Override
    public LivingEntity getOwner() {
        try {
            UUID uUID = this.getOwnerUuid();
            return uUID == null ? null : this.world.getPlayerByUuid(uUID);
        } catch (IllegalArgumentException var2) {
            return null;
        }
    }

    @Override
    public boolean isOwner(LivingEntity entity) {
        return entity == this.getOwner();
    }

    @Override
    public boolean isTamed() {
        return (this.dataTracker.get(TAMEABLE) & 4) != 0;
    }

    @Override
    public void setTamed(boolean tamed) {
        byte b = this.dataTracker.get(TAMEABLE);
        if (tamed) {
            this.dataTracker.set(TAMEABLE, (byte) (b | 4));
        } else {
            this.dataTracker.set(TAMEABLE, (byte) (b & -5));
        }

        this.onTamedChanged();
    }
    public void reset() {
        this.setTarget(null);
        this.navigation.stop();
    }
}
