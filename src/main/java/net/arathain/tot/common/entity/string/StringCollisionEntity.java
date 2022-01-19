package net.arathain.tot.common.entity.string;

import net.arathain.tot.common.init.ToTEntities;
import net.arathain.tot.common.network.NetworkingPackages;
import net.arathain.tot.common.network.packet.StringSpawnPacketCreator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ToolItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

import java.util.function.Function;

/**
 * StringCollisionEntity is an Entity that is invisible but has a collision.
 * It is used to create a collision for connections between chains.
 *
 * @author legoatoom
 */
public class StringCollisionEntity extends Entity {

    /**
     * The StringKnot entity id that has a connection to another StringKnot with id {@link #endOwnerId}.
     */
    private int startOwnerId;
    /**
     * The StringKnot entity id that has a connection from another StringKnot with id {@link #startOwnerId}.
     */
    private int endOwnerId;

    @SuppressWarnings("WeakerAccess")
    public StringCollisionEntity(EntityType<? extends StringCollisionEntity> entityType, World world) {
        super(entityType, world);

    }

    @SuppressWarnings("WeakerAccess")
    public StringCollisionEntity(World world, double x, double y, double z, int startOwnerId, int endOwnerId) {
        this(ToTEntities.STRING_COLLISION, world);
        this.startOwnerId = startOwnerId;
        this.endOwnerId = endOwnerId;
        this.setPosition(x, y, z);
    }

    @Override
    protected void initDataTracker() {
        // Required by Entity
    }

    /**
     * When this entity is attacked by a player with a item that has Tag: {@link FabricToolTags#SHEARS},
     * it calls the {@link StringKnotEntity#damageLink(boolean, StringKnotEntity)} method
     * to destroy the link between the {@link #startOwnerId} and {@link #endOwnerId}
     */
    @Override
    public boolean damage(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        } else if (!this.world.isClient) {
            Entity startOwner = this.world.getEntityById(startOwnerId);
            Entity endOwner = this.world.getEntityById(endOwnerId);
            Entity sourceEntity = source.getAttacker();
            if (source.getSource() instanceof PersistentProjectileEntity) {
                return false;
            } else if (sourceEntity instanceof PlayerEntity
                    && startOwner instanceof StringKnotEntity && endOwner instanceof StringKnotEntity) {
                boolean isCreative = ((PlayerEntity) sourceEntity).isCreative();
                if (!((PlayerEntity) sourceEntity).getMainHandStack().isEmpty() && FabricToolTags.SHEARS.contains(((PlayerEntity) sourceEntity).getMainHandStack().getItem()) || ((PlayerEntity) sourceEntity).getMainHandStack().getItem() instanceof ToolItem && ((ToolItem)((PlayerEntity) sourceEntity).getMainHandStack().getItem()).getMaterial().getAttackDamage() > ((PlayerEntity) sourceEntity).getRandom().nextInt(30)) {
                    ((StringKnotEntity) startOwner).damageLink(isCreative, (StringKnotEntity) endOwner);
                }
            }
            return true;
        } else {
            return !(source.getSource() instanceof PersistentProjectileEntity);
        }
    }

    /**
     * If this entity can even be collided with.
     * Different from {@link #isCollidable()} ()} as this tells if something can collide with this.
     *
     * @return true
     */
    @Override
    public boolean collides() {
        return !isRemoved();
    }

    /**
     * We don't want to be able to push the collision box of the chain.
     * @return false
     */
    @Override
    public boolean isPushable() {
        return false;
    }

    /**
     * We only allow the collision box to be rendered if a player is holding a item that has tag {@link FabricToolTags#SHEARS}.
     * This might be helpful when using F3+B to see the boxes of the chain.
     *
     * @return boolean - should the collision box be rendered.
     */
    @Environment(EnvType.CLIENT)
    @Override
    public boolean shouldRender(double distance) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null && player.isHolding(item -> item.isIn(FabricToolTags.SHEARS) || item.getItem() instanceof ToolItem)) {
            return super.shouldRender(distance);
        } else {
            return false;
        }
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound tag) {
        // Required by Entity, but does nothing.
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound tag) {
        // Required by Entity, but does nothing.
    }

    /**
     * Makes sure that nothing can walk through it.
     */
    @Override
    public boolean isCollidable() {
        return true;
    }

    /**
     * What happens when this is attacked?
     * This method is called by {@link PlayerEntity#attack(Entity)} to allow an entity to choose what happens when
     * it is attacked. We don't want to play sounds when we attack it without shears, so that is why we override this.
     */
    @Override
    public boolean handleAttack(Entity attacker) {
        playSound(SoundEvents.BLOCK_WOOL_HIT, 0.5F, 1.0F);
        if (attacker instanceof PlayerEntity playerEntity) {
            return this.damage(DamageSource.player(playerEntity), 0.0F);
        } else {
            return false;
        }
    }

    /**
     * When this entity is created we need to send a packet to the client.
     * This method sends a packet that contains the entityID of both the start and
     * end StringKnot of this entity.
     *
     */
    @Override
    public Packet<?> createSpawnPacket() {
        //Write our id and the id of the one we connect to.
        Function<PacketByteBuf, PacketByteBuf> extraData = packetByteBuf -> {
            packetByteBuf.writeVarInt(startOwnerId);
            packetByteBuf.writeVarInt(endOwnerId);
            return packetByteBuf;
        };
        return StringSpawnPacketCreator.create(this, NetworkingPackages.S2C_SPAWN_STRING_COLLISION_PACKET, extraData);
    }

    public void setStartOwnerId(int startOwnerId) {
        this.startOwnerId = startOwnerId;
    }

    public void setEndOwnerId(int endOwnerId) {
        this.endOwnerId = endOwnerId;
    }
}
