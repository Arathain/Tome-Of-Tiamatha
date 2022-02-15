package net.arathain.tot.common.entity.living.drider.arachne;

import net.arathain.tot.common.entity.living.drider.DriderEntity;
import net.arathain.tot.common.entity.living.goal.ArachneAttackLogicGoal;
import net.arathain.tot.common.entity.living.goal.ArachneEmitShockwaveGoal;
import net.arathain.tot.common.entity.living.goal.ArachneSummonWeavechildrenGoal;
import net.arathain.tot.common.entity.living.goal.DriderAttackGoal;
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
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
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
    public static final TrackedData<Integer> ATTACK_STATE = DataTracker.registerData(ArachneEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private final ServerBossBar bossBar = (new ServerBossBar(this.getDisplayName(), BossBar.Color.PINK, BossBar.Style.PROGRESS));
    public int slamTicks = 0;
    public boolean canSlam = false;
    public ArachneEntity(EntityType<? extends SpiderEntity> entityType, World world) {
        super(entityType, world);
    }
    public static DefaultAttributeContainer.Builder createArachneAttributes() {
        return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_FOLLOW_RANGE, 128.0).add(EntityAttributes.GENERIC_MAX_HEALTH, 200.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.5f).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 7.0).add(EntityAttributes.GENERIC_ARMOR, 20.0).add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0);
    }
    @Override
    protected void updateDespawnCounter() {}
    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new ArachneAttackLogicGoal(this));
        this.goalSelector.add(1, new ArachneEmitShockwaveGoal(this));
        //this.goalSelector.add(1, new ArachneSummonWeavechildrenGoal(this));
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
        tag.putBoolean("canSlam", canSlam);
        tag.putInt("slamTicks", slamTicks);
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
        slamTicks = nbt.getInt("slamTicks");
        canSlam = nbt.getBoolean("canSlam");
        this.setResting(nbt.getBoolean("Resting"));
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController<>(this, "controller", 5, this::predicate));
        animationData.addAnimationController(new AnimationController<>(this, "attackController", 3, this::attackPredicate));
    }
    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        AnimationBuilder animationBuilder = new AnimationBuilder();
        if(!this.hasVehicle() && event.isMoving()) {
            animationBuilder.addAnimation("walk", true);
        } else if(this.isResting()) {
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
        this.dataTracker.startTracking(ATTACK_STATE, 0);
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
        return restPos.filter(blockPos -> blockPos.getSquaredDistance(getPos(), false) < 9).isPresent();
    }

    private void updateRestingPos() {
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

        if (age % 60 == 0 && getHealth() < getMaxHealth()) {
            heal(isResting() ? 16 : 2);
        }
        //if (getTarget() != null && (!getTarget().isAlive() || getTarget().getHealth() <= 0)) setTarget(null);
        if(!world.isClient) {
            if(!isResting()) {
                if(this.getTarget() == null && forwardSpeed == 0 && isAtRestingPos()) {
                    setResting(true);
                }

            } else if(getTarget() != null && squaredDistanceTo(getTarget()) < 30) {
                setResting(false);
            }
        }
        if(isResting()) {
            setVelocity(0, getVelocity().y, 0);
            setYaw(prevYaw);
            setBodyYaw(prevBodyYaw);
            setHeadYaw(MathHelper.clamp(headYaw, bodyYaw - 90, bodyYaw + 90));
        }
        if(canSlam) {
            slamTick();
        }
        if (getTarget() == null && getNavigation().isIdle() && !isAtRestingPos() && !isResting()) updateRestingPos();
    }
    public int getAttackState() {
        return this.dataTracker.get(ATTACK_STATE);
    }

    public void setAttackState(int state) {
        this.dataTracker.set(ATTACK_STATE, state);
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
    private void slamTick() {
        double perpFacing = this.bodyYaw * (Math.PI / 180);
        double facingAngle = perpFacing + Math.PI / 2;
        final int maxDistance = 6;
        if (slamTicks >= 20) {
            if (slamTicks == 20) {
                final double infront = 1.47, side = -0.21;
                double vx = Math.cos(facingAngle) * infront;
                double vz = Math.sin(facingAngle) * infront;
                double perpX = Math.cos(perpFacing);
                double perpZ = Math.sin(perpFacing);
                double fx = getX() + vx + perpX * side;
                double fy = getBoundingBox().minY + 0.1;
                double fz = getZ() + vz + perpZ * side;
                int amount = 16 + world.random.nextInt(8);
                while (amount-- > 0) {
                    double theta = world.random.nextDouble() * Math.PI * 2;
                    double dist = world.random.nextDouble() * 0.1 + 0.25;
                    double sx = Math.cos(theta);
                    double sz = Math.sin(theta);
                    double px = fx + sx * dist;
                    double py = fy + world.random.nextDouble() * 0.1;
                    double pz = fz + sz * dist;
                    world.addParticle(ParticleTypes.LARGE_SMOKE, px, py, pz, sx * 0.065, 0, sz * 0.065);
                }
            }
            if (slamTicks % 2 == 0) {
                int distance = slamTicks / 2 - 8;
                double spread = Math.PI * 2;
                int arcLen = MathHelper.ceil(distance * spread);
                for (int i = 0; i < arcLen; i++) {
                    double theta = (i / (arcLen - 1.0) - 0.5) * spread + facingAngle;
                    double vx = Math.cos(theta);
                    double vz = Math.sin(theta);
                    double px = getX() + vx * distance;
                    double pz = getZ() + vz * distance;
                    float factor = 1 - distance / (float) maxDistance;
                    if (world.random.nextInt(5) < 4) {
                        int amount = world.random.nextInt(5);
                        while (amount-- > 0) {
                            double velX = vx * 0.075;
                            double velY = factor * 0.3 + 0.025;
                            double velZ = vz * 0.075;
                            world.addParticle(ParticleTypes.SMOKE, px + world.random.nextFloat() * 2 - 1, getBoundingBox().minY + 0.1 + world.random.nextFloat() * 1.5, pz + world.random.nextFloat() * 2 - 1, velX, velY, velZ);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onStoppedTrackingBy(ServerPlayerEntity player) {
        super.onStoppedTrackingBy(player);
        this.bossBar.removePlayer(player);
    }


    private <E extends IAnimatable> PlayState attackPredicate(AnimationEvent<E> event) {
        AnimationBuilder animationBuilder = new AnimationBuilder();

        if(this.dataTracker.get(ATTACK_STATE) == 1) {
            animationBuilder.addAnimation("slam", true);
        }

        if(!animationBuilder.getRawAnimationList().isEmpty()) {
            event.getController().setAnimation(animationBuilder);
            return PlayState.CONTINUE;
        }
        return PlayState.STOP;
    }
    public void setCustomName(@Nullable Text name) {
        super.setCustomName(name);
        this.bossBar.setName(this.getDisplayName());
    }

}
