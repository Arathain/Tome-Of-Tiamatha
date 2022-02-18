package net.arathain.tot.common.entity.living.drider.arachne;

import net.arathain.tot.common.entity.living.drider.DriderEntity;
import net.arathain.tot.common.entity.living.drider.arachne.waves.WaveSpawnEntry;
import net.arathain.tot.common.entity.living.entityinterface.Broodchild;
import net.arathain.tot.common.entity.living.goal.ArachneAttackLogicGoal;
import net.arathain.tot.common.entity.living.goal.ArachneEmitShockwaveGoal;
import net.arathain.tot.common.entity.living.goal.ArachneSummonWeavechildrenGoal;
import net.arathain.tot.common.entity.living.goal.DriderAttackGoal;
import net.arathain.tot.common.init.ToTWaves;
import net.arathain.tot.common.util.ToTUtil;
import net.minecraft.block.CobwebBlock;
import net.minecraft.client.MinecraftClient;
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
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.command.TitleCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.village.raid.Raid;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ArachneEntity extends DriderEntity {
    protected static final TrackedData<Boolean> RESTING = DataTracker.registerData(ArachneEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    public static final TrackedData<Optional<BlockPos>> RESTING_POS = DataTracker.registerData(ArachneEntity.class, TrackedDataHandlerRegistry.OPTIONAL_BLOCK_POS);
    public static final TrackedData<Integer> ATTACK_STATE = DataTracker.registerData(ArachneEntity.class, TrackedDataHandlerRegistry.INTEGER);
    public static final TrackedData<Integer> WAVE = DataTracker.registerData(ArachneEntity.class, TrackedDataHandlerRegistry.INTEGER);
    protected static final TrackedData<Boolean> WAVE_IN_PROGRESS = DataTracker.registerData(ArachneEntity.class, TrackedDataHandlerRegistry.BOOLEAN);


    private final ServerBossBar bossBar = (new ServerBossBar(this.getDisplayName(), BossBar.Color.PINK, BossBar.Style.PROGRESS));
    public int slamTicks = 0;
    private int waveCooldown = 0;
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
        tag.putInt("WaveCooldown", waveCooldown);
        tag.putInt("slamTicks", slamTicks);
        tag.putBoolean("Resting", this.isResting());
        tag.putBoolean("WaveInProgress", this.hasWaveInProgress());
        tag.putInt("Wave", this.getWave());
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
        waveCooldown = nbt.getInt("WaveCooldown");
        slamTicks = nbt.getInt("slamTicks");
        this.setWave(nbt.getInt("Wave"));
        this.setHasWaveInProgress(nbt.getBoolean("WaveInProgress"));
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
        this.dataTracker.startTracking(WAVE_IN_PROGRESS, true);
        this.dataTracker.startTracking(WAVE, 0);
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

    public boolean hasWaveInProgress() {
        return getDataTracker().get(WAVE_IN_PROGRESS);
    }

    public void setHasWaveInProgress(boolean has) {
        getDataTracker().set(WAVE_IN_PROGRESS, has);
    }

    public int getWave() {
        return this.dataTracker.get(WAVE);
    }

    public void setWave(int wave) {
        this.dataTracker.set(WAVE, wave);
    }

    @Override
    protected void mobTick() {
        this.bossBar.setPercent(this.getHealth()/this.getMaxHealth());
        super.mobTick();
    }
    @Override
    public void tick() {
        super.tick();
        if(!isResting())
        waveTick();

        if (age % 5 == 0 && getHealth() < getMaxHealth() && isResting()) {
            heal(2);
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
        if(this.getAttackState() == 1) {
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
    private void waveTick() {
        if(waveCooldown > 0) waveCooldown--;
        if (!world.isClient && this.age % 5 == 0 && waveCooldown == 0) {
            int wave = this.getWave();
            if(ToTWaves.ARACHNE_WAVES.isEmpty() || ToTWaves.ARACHNE_WAVES.size() == 0 || ToTWaves.ARACHNE_WAVES.get(wave) == null)
            ToTWaves.updateArachneWaves(this.getRandom());

            // check if there are enemies left to spawn
            List<MobEntity> enemiesLeft = world.getEntitiesByClass(MobEntity.class, this.getBoundingBox().expand(100f, 30f, 100f), entity -> entity instanceof Broodchild);
            ToTWaves.ARACHNE_WAVES.get(wave).removeIf(waveSpawnEntry -> waveSpawnEntry.count <= 0);
            if (!ToTWaves.ARACHNE_WAVES.get(wave).isEmpty() && enemiesLeft.size() < 50 && this.getRestingPos().isPresent()) {
                int i = random.nextInt(ToTWaves.ARACHNE_WAVES.get(wave).size());

                MobEntity enemy = ToTWaves.ARACHNE_WAVES.get(wave).get(i).entityType.create(this.world);

                enemy.initialize((ServerWorldAccess) world, world.getLocalDifficulty(this.getBlockPos()), SpawnReason.MOB_SUMMONED, null, null);

                var angle = Math.random() * Math.PI * 2;
                float x = (float) (this.getRestingPos().get().getX() + (Math.cos(angle) * (16f - random.nextFloat(2f) * 2)));
                float z = (float) (this.getRestingPos().get().getZ() + (Math.sin(angle) * (16f - random.nextFloat(2f) * 2)));

                BlockPos offsetPos = new BlockPos(x, this.getRestingPos().get().getY(), z);
                for (int h = -10; h < 10; h++) {
                    if ((world.getBlockState(offsetPos.add(0, h, 0)).isAir() || world.getBlockState(offsetPos.add(0, h, 0)).getBlock() instanceof CobwebBlock) && (world.getBlockState(offsetPos.add(0, h - 1, 0)).isSolidBlock(world, offsetPos.add(0, h - 1, 0)))) {
                        enemy.setPosition(offsetPos.getX(), offsetPos.getY() + h, offsetPos.getZ());
                        enemy.setPersistent();
                        world.spawnEntity(enemy);
                        ToTWaves.ARACHNE_WAVES.get(wave).get(i).count--;
                        break;
                    }
                }

            } else {
                // wave end check
                if (enemiesLeft.size() <= 2) {

                    this.setWave(wave + 1);

                    world.getPlayers().forEach(serverPlayerEntity -> {
                        serverPlayerEntity.sendMessage(
                                new TranslatableText("info.tot.wave_cleared", world.getRegistryKey().getValue().getPath()).setStyle(Style.EMPTY.withColor(Formatting.AQUA)), false);
                    });

                    if(getWave() >= 9) setWave(0);
                    waveCooldown = 200;
                    this.playSound(SoundEvents.ENTITY_SPIDER_DEATH, 1.0f, 1.2f);
                    this.playSound(SoundEvents.ENTITY_DRAGON_FIREBALL_EXPLODE, 1.0f, 1.2f);
                }
            }
        }
    }
    public void slamTick() {
        double perpFacing = this.bodyYaw * (Math.PI / 180);
        double facingAngle = perpFacing + Math.PI / 2;
        final int maxDistance = 6;
        if (slamTicks >= 10) {
            if (slamTicks == 10) {
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
                int distance = slamTicks / 2 - 3;
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
                            world.addParticle(ParticleTypes.CLOUD, px + world.random.nextFloat() * 2 - 1, getBoundingBox().minY + 0.1 + world.random.nextFloat() * 1.5, pz + world.random.nextFloat() * 2 - 1, velX, velY, velZ);
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

        if(this.getAttackState() == 1) {
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
