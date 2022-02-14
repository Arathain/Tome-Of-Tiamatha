package net.arathain.tot.common.entity.living.drider.arachne;

import net.arathain.tot.common.entity.living.drider.DriderEntity;
import net.arathain.tot.common.entity.living.goal.ArachneAttackLogicGoal;
import net.arathain.tot.common.entity.living.goal.ArachneSummonWeavechildrenGoal;
import net.arathain.tot.common.util.ToTUtil;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

import java.util.Optional;

public class ArachneEntity extends DriderEntity {
    protected static final TrackedData<Boolean> RESTING = DataTracker.registerData(ArachneEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    public static final TrackedData<Optional<BlockPos>> RESTING_POS = DataTracker.registerData(ArachneEntity.class, TrackedDataHandlerRegistry.OPTIONAL_BLOCK_POS);

    private final ServerBossBar bossBar = (new ServerBossBar(this.getDisplayName(), BossBar.Color.PINK, BossBar.Style.PROGRESS));
    public ArachneEntity(EntityType<? extends SpiderEntity> entityType, World world) {
        super(entityType, world);
    }
    public static DefaultAttributeContainer.Builder createArachneAttributes() {
        return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_FOLLOW_RANGE, 128.0).add(EntityAttributes.GENERIC_MAX_HEALTH, 200.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.5f).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 7.0).add(EntityAttributes.GENERIC_ARMOR, 12.0).add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.9);
    }

    @Override
    protected void updateDespawnCounter() {

    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new ArachneAttackLogicGoal(this));
        this.goalSelector.add(1, new ArachneSummonWeavechildrenGoal(this));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 10.0f));
        this.goalSelector.add(6, new LookAtEntityGoal(this, DriderEntity.class, 6.0f));
        this.goalSelector.add(6, new LookAroundGoal(this));
        this.targetSelector.add(1, new RevengeGoal(this, SpiderEntity.class).setGroupRevenge());
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, 10, true, false, player -> !ToTUtil.isDrider(player)));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, IronGolemEntity.class, true));
    }
    @Override
    public void writeCustomDataToNbt(NbtCompound tag) {
        super.skipWriteNbtData(tag);
        if(getRestingPos().isPresent()) {
            tag.put("RestingPos", NbtHelper.fromBlockPos(getRestingPos().get()));
        }
        tag.putBoolean("Resting", this.isResting());
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityTag) {
        setRestingPos(getBlockPos());
        return super.initialize(world, difficulty, spawnReason, entityData, entityTag);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if(source.getAttacker() instanceof LivingEntity attacker && !this.world.isClient) {
            this.getWorld().getEntitiesByClass(SpiderEntity.class, this.getBoundingBox().expand(20), spiderEntity -> isAlive()).forEach(spooder -> {spooder.setTarget(attacker);});
        }
        return super.damage(source, amount);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.skipReadNbtData(nbt);
        this.bossBar.setName(this.getDisplayName());
        if(nbt.contains("RestingPos")) {
            setRestingPos(NbtHelper.toBlockPos(nbt.getCompound("RestingPos")));
        }
        this.setResting(nbt.getBoolean("Resting"));
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController<>(this, "controller", 4, this::predicate));
        animationData.addAnimationController(new AnimationController<>(this, "torsoController", 3, this::torsoPredicate));
    }
    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        AnimationBuilder animationBuilder = new AnimationBuilder();
        if(!this.hasVehicle() && event.isMoving()) {
            animationBuilder.addAnimation("walk", true);
        } else if(isResting()) {
            animationBuilder.addAnimation("sitIdle", true);
        } else if(!this.hasVehicle()) {
            animationBuilder.addAnimation("idle", true);
        }

        if(!animationBuilder.getRawAnimationList().isEmpty()) {
            event.getController().setAnimation(animationBuilder);
        }
        return PlayState.CONTINUE;
    }

    protected void initDataTracker() {
        super.skipInitDataTracker();
        this.dataTracker.startTracking(RESTING_POS, Optional.empty());
        this.dataTracker.startTracking(RESTING, true);
    }
    public Optional<BlockPos> getRestingPos() {
        return getDataTracker().get(RESTING_POS);
    }

    public void setRestingPos(BlockPos pos) {
        getDataTracker().set(RESTING_POS, Optional.of(pos));
    }
    private boolean isAtRestingPos() {
        Optional<BlockPos> restPos = getRestingPos();
        return restPos.filter(blockPos -> blockPos.getSquaredDistance(getPos(), false) < 36).isPresent();
    }

    private void updateRestPos() {
        boolean reassign = true;
        if (getRestingPos().isPresent()) {
            BlockPos pos = getRestingPos().get();
            if (getNavigation().startMovingTo(pos.getX(), pos.getY(), pos.getZ(), 0.5)) {
                reassign = false;
            }
        }
        if (reassign) {
            setRestingPos(getBlockPos());
        }
    }

    public boolean isResting() {
        return getDataTracker().get(RESTING);
    }

    public void setResting(boolean rest) {
        getDataTracker().set(RESTING, rest);
    }

    @Override
    protected void mobTick() {
        this.bossBar.setPercent(this.getHealth()/this.getMaxHealth());
        super.mobTick();
    }
    @Override
    public void tick() {
        super.tick();

        if (age % 120 == 0 && getHealth() < getMaxHealth()) {
            heal(isResting() ? 16 : 4);
        }
        if (getTarget() != null && (!getTarget().isAlive() || getTarget().getHealth() <= 0)) setTarget(null);
        if(!world.isClient) {
            if(!isResting()) {
                if(this.getTarget() == null && forwardSpeed == 0 && isAtRestingPos()) {
                    setResting(true);
                }

            } else if(getTarget() != null && squaredDistanceTo(getTarget()) < 20) {
                setResting(false);
            }
        }
        if(isResting()) {
            setVelocity(0, getVelocity().y, 0);
            bodyYaw = prevBodyYaw;
        }
        if (getTarget() == null && getNavigation().isIdle() && !isAtRestingPos() && !isResting()) updateRestPos();
    }

    @Override
    public boolean cannotDespawn() {
        return true;
    }

    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        super.onStartedTrackingBy(player);
        this.bossBar.addPlayer(player);
    }
    public double getAngleBetweenEntities(Entity first, Entity second) {
        return Math.atan2(second.getZ() - first.getZ(), second.getX() - first.getX()) * (180 / Math.PI) + 90;
    }

    @Override
    public void onStoppedTrackingBy(ServerPlayerEntity player) {
        super.onStoppedTrackingBy(player);
        this.bossBar.removePlayer(player);
    }


    private <E extends IAnimatable> PlayState torsoPredicate(AnimationEvent<E> event) {
        AnimationBuilder animationBuilder = new AnimationBuilder();

        animationBuilder.addAnimation("torsoIdle", true);

        if(!animationBuilder.getRawAnimationList().isEmpty()) {
            event.getController().setAnimation(animationBuilder);
        }
        return PlayState.CONTINUE;
    }
    public void setCustomName(@Nullable Text name) {
        super.setCustomName(name);
        this.bossBar.setName(this.getDisplayName());
    }

}
