package net.arathain.tot.common.entity.living.drider.weavekin;

import net.arathain.tot.common.util.ToTUtil;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.IAnimationTickable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class WeavechildEntity extends SpiderEntity implements IAnimatable, IAnimationTickable {
    private final AnimationFactory factory = new AnimationFactory(this);
    public WeavechildEntity(EntityType<? extends SpiderEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(3, new PounceAtTargetGoal(this, 0.4f));
        this.goalSelector.add(4, new AttackGoal(this));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(6, new LookAroundGoal(this));
        this.targetSelector.add(1, new RevengeGoal(this).setGroupRevenge());
        this.targetSelector.add(2, new TargetGoal<PlayerEntity>(this, PlayerEntity.class));
        this.targetSelector.add(3, new TargetGoal<IronGolemEntity>(this, IronGolemEntity.class));
        this.goalSelector.add(3, new SmartFleeGoal<>(this, IronGolemEntity.class));
        this.goalSelector.add(3, new SmartFleeGoal<>(this, PlayerEntity.class));
    }

    public static DefaultAttributeContainer.Builder createWeavechildAttributes() {
        return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_FOLLOW_RANGE, 35.0).add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.4f).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2.0).add(EntityAttributes.GENERIC_ARMOR, 4.0);
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController<>(this, "controller", 5, this::predicate));
    }
    @Override
    public boolean tryAttack(Entity target) {
        if (super.tryAttack(target)) {
            if (target instanceof LivingEntity) {
                int i = 0;
                if (this.world.getDifficulty() == Difficulty.NORMAL) {
                    i = 7;
                } else if (this.world.getDifficulty() == Difficulty.HARD) {
                    i = 15;
                }
                if (i > 0) {
                    ((LivingEntity)target).addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, i * 20, 0), this);
                }
            }
            return true;
        }
        return false;
    }
    @Override
    protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
        return 0.45f;
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        AnimationBuilder animationBuilder = new AnimationBuilder();
        if(event.isMoving()) {
            animationBuilder.addAnimation("walk", true);
        } else {
            animationBuilder.addAnimation("idle", true);
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
            int f = 0;
            f += this.mob.getWorld().getEntitiesByClass(SpiderEntity.class, this.mob.getBoundingBox().expand(10), mob -> true).size();
            f += this.mob.getWorld().getEntitiesByClass(PlayerEntity.class, this.mob.getBoundingBox().expand(10), ToTUtil::isDrider).size();
            if (targetEntity != null && f >= targetEntity.getHealth()/3 && !ToTUtil.isDrider(targetEntity)) {
                return super.canStart();
            }
            return false;
        }
    }
    static class SmartFleeGoal<T extends LivingEntity> extends FleeEntityGoal<T> {
        public SmartFleeGoal(PathAwareEntity mob, Class<T> fleeFromType) {
            super(mob, fleeFromType, 10, 0.4f, 1.0f);
        }

        @Override
        public boolean canStart() {
            int f = 0;
            f += this.mob.getWorld().getEntitiesByClass(SpiderEntity.class, this.mob.getBoundingBox().expand(10), mob -> true).size();
            f += this.mob.getWorld().getEntitiesByClass(PlayerEntity.class, this.mob.getBoundingBox().expand(10), ToTUtil::isDrider).size();
            if (targetEntity != null && f < targetEntity.getHealth()/3 && !ToTUtil.isDrider(targetEntity)) {
                return false;
            }
            return super.canStart();
        }

        @Override
        public boolean shouldContinue() {
            int f = 0;
            f += this.mob.getWorld().getEntitiesByClass(SpiderEntity.class, this.mob.getBoundingBox().expand(10), mob -> true).size();
            f += this.mob.getWorld().getEntitiesByClass(PlayerEntity.class, this.mob.getBoundingBox().expand(10), ToTUtil::isDrider).size();
            if (targetEntity != null && f < targetEntity.getHealth()/3 && !ToTUtil.isDrider(targetEntity)) {
                return false;
            }
            return super.shouldContinue();
        }
    }


}
