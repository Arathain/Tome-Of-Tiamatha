package net.arathain.tot.common.entity.living.drider.arachne;

import net.arathain.tot.common.entity.living.drider.DriderEntity;
import net.arathain.tot.common.entity.living.goal.ArachneRestGoal;
import net.arathain.tot.common.entity.living.goal.ArachneSitGoal;
import net.arathain.tot.common.entity.living.goal.DriderAttackGoal;
import net.arathain.tot.common.entity.living.goal.DriderShieldGoal;
import net.arathain.tot.common.util.ToTUtil;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.PillagerEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
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

public class ArachneEntity extends DriderEntity {
    protected static final TrackedData<Byte> ARACHNE_FLAGS = DataTracker.registerData(TameableEntity.class, TrackedDataHandlerRegistry.BYTE);
    public static final TrackedData<BlockPos> RESTING_POS = DataTracker.registerData(DriderEntity.class, TrackedDataHandlerRegistry.BLOCK_POS);

    private final ServerBossBar bossBar = (new ServerBossBar(this.getDisplayName(), BossBar.Color.PINK, BossBar.Style.PROGRESS));
    public ArachneEntity(EntityType<? extends SpiderEntity> entityType, World world) {
        super(entityType, world);
    }
    private boolean sitting;
    public static DefaultAttributeContainer.Builder createArachneAttributes() {
        return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_FOLLOW_RANGE, 128.0).add(EntityAttributes.GENERIC_MAX_HEALTH, 200.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3f).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 7.0).add(EntityAttributes.GENERIC_ARMOR, 12.0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        //this.goalSelector.add(2, new DriderAttackGoal(this, 1.0, false));
        //this.goalSelector.add(0, new DriderShieldGoal(this));
        this.goalSelector.add(5, new WanderAroundGoal(this, 0.7));
        this.goalSelector.add(1, new ArachneRestGoal(this));
        this.goalSelector.add(0, new ArachneSitGoal(this));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 10.0f));
        this.goalSelector.add(6, new LookAtEntityGoal(this, DriderEntity.class, 6.0f));
        this.goalSelector.add(6, new LookAroundGoal(this));
        this.targetSelector.add(1, new RevengeGoal(this, SpiderEntity.class).setGroupRevenge());
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, 10, true, false, player -> !ToTUtil.isDrider(player)));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, IronGolemEntity.class, true));
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound tag) {
        super.writeCustomDataToNbt(tag);
        tag.putBoolean("Sitting", this.sitting);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.bossBar.setName(this.getDisplayName());
        this.sitting = nbt.getBoolean("Sitting");
        this.setInSittingPose(this.sitting);
    }
    public boolean isInSittingPose() {
        return (this.dataTracker.get(ARACHNE_FLAGS) & 1) != 0;
    }

    public void setInSittingPose(boolean inSittingPose) {
        byte b = this.dataTracker.get(ARACHNE_FLAGS);
        if (inSittingPose) {
            this.dataTracker.set(ARACHNE_FLAGS, (byte)(b | 1));
        } else {
            this.dataTracker.set(ARACHNE_FLAGS, (byte)(b & -2));
        }

    }
    public boolean isSitting() {
        return this.sitting;
    }

    public void setSitting(boolean sitting) {
        this.sitting = sitting;
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
        } else if(isInSittingPose()) {
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
        super.initDataTracker();
        this.dataTracker.startTracking(ARACHNE_FLAGS, (byte)0);
        this.dataTracker.startTracking(RESTING_POS, BlockPos.ORIGIN);
    }

    @Override
    protected void mobTick() {
        this.bossBar.setPercent(this.getHealth()/this.getMaxHealth());
        if(!isSitting()) {
            this.setRestingPos(this.getBlockPos());
            this.setSitting(true);
        }
        System.out.println("the soop");
        System.out.println(this.getRestingPos().getX());
        System.out.println(this.getRestingPos().getY());
        System.out.println(this.getRestingPos().getZ());
        System.out.println("-----");

        super.mobTick();
    }
    @Override
    public void tick() {
        super.tick();

        if (age % 120 == 0 && getHealth() < getMaxHealth()) {
            heal(4);
        }
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
    public BlockPos getRestingPos() {
        BlockPos pos = dataTracker.get(RESTING_POS);
        return pos == BlockPos.ZERO ? null : pos;
    }

    public void setRestingPos(@Nullable BlockPos pos) {
        dataTracker.set(RESTING_POS, pos == null ? BlockPos.ORIGIN : pos);
    }
    public void setCustomName(@Nullable Text name) {
        super.setCustomName(name);
        this.bossBar.setName(this.getDisplayName());
    }

}
