package net.arathain.tot.common.entity.living.drider.weaver;

import net.arathain.tot.common.init.ToTEntities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class WebbingEntity extends Entity implements IAnimatable {
    private final AnimationFactory factory = new AnimationFactory(this);

    public WebbingEntity(EntityType<? extends WebbingEntity> type, World world) {
        super(type, world);
        this.inanimate = true;
    }

    public WebbingEntity(World world, double x, double y, double z) {
        this(ToTEntities.WEBBING, world);
        this.setPosition(x, y, z);
        this.prevX = x;
        this.prevY = y;
        this.prevZ = z;
    }

    @Override
    protected void initDataTracker() {

    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        if (this.world.isClient || this.isRemoved()) {
            return true;
        }
        this.scheduleVelocityUpdate();
        this.emitGameEvent(GameEvent.ENTITY_DAMAGED, source.getAttacker());
        if (((!(source.getAttacker() == this.getFirstPassenger()) || this.age > 2000) && amount >= 6) || source.isExplosive() || source.isFire() || source.isSourceCreativePlayer()) {
            this.discard();
        }
        return true;
    }
    @Override
    protected Vec3d positionInPortal(Direction.Axis portalAxis, BlockLocating.Rectangle portalRect) {
        return LivingEntity.positionInPortal(super.positionInPortal(portalAxis, portalRect));
    }

    @Override
    public boolean isCollidable() {
        return true;
    }

    @Override
    public boolean collides() {
        return !this.isRemoved();
    }

    @Override
    public void updatePassengerPosition(Entity passenger) {
        if (!this.hasPassenger(passenger)) {
            return;
        }
        float f = 0.0f;
        float g = (float) ((this.isRemoved() ? (double) 0.01f : this.getMountedHeightOffset()) + passenger.getHeightOffset());
        passenger.setPosition(this.getX(), this.getY(), this.getZ());
        passenger.setYaw(this.getYaw());
        passenger.setPitch(0);
        passenger.setHeadYaw(this.getYaw());

    }


    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {

    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {

    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if (player.shouldCancelInteraction()) {
            return ActionResult.PASS;
        }
        if (!this.world.isClient) {
            return player.startRiding(this) ? ActionResult.CONSUME : ActionResult.PASS;
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isLogicalSideForUpdatingMovement()) {
            this.updateVelocity(1, new Vec3d(0, -0.1f, 0));
            this.move(MovementType.SELF, this.getVelocity());


        } else {
            this.setVelocity(Vec3d.ZERO);
        }
    }

    @Override
    public Packet<?> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }

    @Override
    public void registerControllers(AnimationData animationData) {

    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }
}
