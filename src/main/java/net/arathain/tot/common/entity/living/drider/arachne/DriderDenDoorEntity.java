package net.arathain.tot.common.entity.living.drider.arachne;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class DriderDenDoorEntity extends Entity {
    private boolean open = false;
    private static final TrackedData<BlockPos> TARGET_POS = DataTracker.registerData(DriderDenDoorEntity.class, TrackedDataHandlerRegistry.BLOCK_POS);
    private int openTicks = 0;
    public DriderDenDoorEntity(EntityType<?> entityType, World world) {
        super(entityType, world);
        this.inanimate = true;
    }

    @Override
    public boolean hasNoGravity() {
        return true;
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if(player.getStackInHand(hand).isOf(Items.WARPED_DOOR) && !world.isClient()) {
            player.getStackInHand(hand).decrement(1);
            open = !open;
        }
        return super.interact(player, hand);
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        this.remove(RemovalReason.DISCARDED);
        return false;
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
    protected void initDataTracker() {
        this.dataTracker.startTracking(TARGET_POS, BlockPos.ORIGIN);
    }
    public void setTargetPos(BlockPos pos) {
        this.getDataTracker().set(TARGET_POS,  pos);
    }
    public BlockPos getTargetPos() {
        return this.getDataTracker().get(TARGET_POS);
    }

    @Override
    public void tick() {
        super.tick();
        if(this.getTargetPos().equals(BlockPos.ORIGIN)) {
            this.setTargetPos(this.getBlockPos());
        }
        if(open) {
            if(openTicks > 0) {
                openTicks--;
            }
        } else {
            if(openTicks < 10) {
                openTicks++;
            }
        }
        if(!world.isClient()) {
            float velocity = (float) (this.getTargetPos().getY() + ((Math.pow(openTicks-5, 3)*0.04 + 5) / 10f) * 3f - this.getY());
            if (this.isLogicalSideForUpdatingMovement()) {
                this.setVelocity(0,  velocity, 0);
                this.move(MovementType.SELF, this.getVelocity());
            }
            this.refreshPositionAfterTeleport(this.getTargetPos().getX() + 0.5, this.getPos().getY(), this.getTargetPos().getZ() + 0.5);
        }
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        setTargetPos(NbtHelper.toBlockPos(nbt.getCompound("target_pos")));
        open = nbt.getBoolean("open");
        this.openTicks = nbt.getInt("openTicks");
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.put("target_pos", NbtHelper.fromBlockPos(this.getTargetPos()));
        nbt.putBoolean("open", open);
        nbt.putInt("openTicks", this.openTicks);
    }

    @Override
    public Packet<?> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }
}
