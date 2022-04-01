package net.arathain.tot.common.entity.string;

import com.github.legoatoom.connectiblechains.util.Helper;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.arathain.tot.common.init.ToTEntities;
import net.arathain.tot.common.init.ToTObjects;
import net.arathain.tot.common.network.NetworkingPackages;
import net.arathain.tot.common.network.PacketBufUtil;
import net.arathain.tot.common.network.packet.StringSpawnPacketCreator;
import net.arathain.tot.common.util.StringUtils;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A logical representation of the link between a knot and another entity.
 * It also serves as a single source of truth which prevents state mismatches in the code.
 *
 * @author Qendolin
 */
public class StringLink {
    /**
     * The x/z distance between {@link StringCollisionEntity StringCollisionEntities}.
     * A value of 1 means they are "shoulder to shoulder"
     */
    private static final float COLLIDER_SPACING = 1.5f;

    /**
     * The de facto owner of this link. It is responsive for managing the link and keeping track of it across saves.
     */
    @NotNull
    public final StringKnotEntity primary;
    /**
     * The de facto target of this link. Mostly used to calculate positions.
     */
    @NotNull
    public final Entity secondary;
    /**
     * Holds the entity ids of associated {@link StringCollisionEntity collision entities}.
     */
    private final IntList collisionStorage = new IntArrayList(16);
    /**
     * Indicates that no sound should be played when the link is destroyed.
     */
    public boolean removeSilently = false;
    /**
     * Whether the link exists and is active
     */
    private boolean alive = true;

    private StringLink(@NotNull StringKnotEntity primary, @NotNull Entity secondary) {
        if (primary.equals(secondary))
            throw new IllegalStateException("Tried to create a link between a knot and itself");
        this.primary = Objects.requireNonNull(primary);
        this.secondary = Objects.requireNonNull(secondary);
    }

    /**
     * Create a String link between primary and secondary,
     * adds it to their lists. Also spawns {@link StringCollisionEntity collision entities}
     * when the link is created between two knots.
     *
     * @param primary   The source knot
     * @param secondary A different String knot or player
     * @return A new String link or null if it already exists
     */
    @Nullable
    public static StringLink create(@NotNull StringKnotEntity primary, @NotNull Entity secondary) {
        StringLink link = new StringLink(primary, secondary);
        // Prevent multiple links between same targets.
        // Checking on the secondary is not required as the link always exists on both sides.
        if (primary.getLinks().contains(link)) return null;

        primary.addLink(link);
        if (secondary instanceof StringKnotEntity secondaryKnot) {
            secondaryKnot.addLink(link);
            link.createCollision();
        }
        if (!primary.world.isClient) {
            link.sendAttachStringPacket(primary.world);
        }
        return link;
    }

    /**
     * Create a collision between this and an entity.
     * It spawns multiple {@link StringCollisionEntity StringCollisionEntities} that are equal distance from each other.
     * Position is the same no matter what if the connection is from A -> B or A <- B.
     */
    private void createCollision() {
        if (!collisionStorage.isEmpty()) return;
        if (primary.world.isClient) return;

        double distance = primary.distanceTo(secondary);
        // step = spacing * √(width^2 + width^2) / distance
        double step = COLLIDER_SPACING * Math.sqrt(Math.pow(ToTEntities.STRING_COLLISION.getWidth(), 2) * 2) / distance;
        double v = step;
        // reserve space for the center collider
        double centerHoldout = ToTEntities.STRING_COLLISION.getWidth() / distance;

        while (v < 0.5 - centerHoldout) {
            Entity collider1 = spawnCollision(false, primary, secondary, v);
            if (collider1 != null) collisionStorage.add(collider1.getId());
            Entity collider2 = spawnCollision(true, primary, secondary, v);
            if (collider2 != null) collisionStorage.add(collider2.getId());

            v += step;
        }

        Entity centerCollider = spawnCollision(false, primary, secondary, 0.5);
        if (centerCollider != null) collisionStorage.add(centerCollider.getId());
    }

    /**
     * Send a package to all the clients around this entity that notifies them of this link's creation.
     */
    private void sendAttachStringPacket(World world) {
        assert world instanceof ServerWorld;

        Set<ServerPlayerEntity> trackingPlayers = getTrackingPlayers(world);
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

        buf.writeVarInt(primary.getId());
        buf.writeVarInt(secondary.getId());

        for (ServerPlayerEntity player : trackingPlayers) {
            ServerPlayNetworking.send(player, NetworkingPackages.S2C_STRING_ATTACH_PACKET_ID, buf);
        }
    }

    /**
     * Spawns a collider at {@code v} percent between {@code start} and {@code end}
     *
     * @param reverse Reverse start and end
     * @param start   the entity at {@code v} = 0
     * @param end     the entity at {@code v} = 1
     * @param v       percent of the distance
     * @return {@link StringCollisionEntity} or null
     */
    @Nullable
    private Entity spawnCollision(boolean reverse, Entity start, Entity end, double v) {
        assert primary.world instanceof ServerWorld;
        Vec3d startPos = start.getPos().add(start.getLeashOffset());
        Vec3d endPos = end.getPos().add(end.getLeashOffset());

        Vec3d tmp = endPos;
        if (reverse) {
            endPos = startPos;
            startPos = tmp;
        }

        Vec3f offset = StringUtils.getStringOffset(startPos, endPos);
        startPos = startPos.add(offset.getX(), 0, offset.getZ());
        endPos = endPos.add(-offset.getX(), 0, -offset.getZ());

        double distance = startPos.distanceTo(endPos);

        double x = MathHelper.lerp(v, startPos.getX(), endPos.getX());
        double y = startPos.getY() + StringUtils.drip2((v * distance), distance, endPos.getY() - startPos.getY());
        double z = MathHelper.lerp(v, startPos.getZ(), endPos.getZ());

        y += -ToTEntities.STRING_COLLISION.getHeight() + 2 / 16f;

        StringCollisionEntity c = new StringCollisionEntity(primary.world, x, y, z, this);
        if (primary.world.spawnEntity(c)) {
            return c;
        } else {
            return null;
        }
    }

    /**
     * Finds all players that are in {@code world} and tracking either the primary or secondary.
     *
     * @param world the world to search in
     * @return A set of all players that track the primary or secondary.
     */
    private Set<ServerPlayerEntity> getTrackingPlayers(World world) {
        assert world instanceof ServerWorld;
        Set<ServerPlayerEntity> trackingPlayers = new HashSet<>(
                PlayerLookup.around((ServerWorld) world, primary.getBlockPos(), StringKnotEntity.VISIBLE_RANGE));
        trackingPlayers.addAll(
                PlayerLookup.around((ServerWorld) world, secondary.getBlockPos(), StringKnotEntity.VISIBLE_RANGE));
        return trackingPlayers;
    }

    public boolean isDead() {
        return !alive;
    }

    /**
     * Returns the squared distance between the primary and secondary.
     */
    public double getSquaredDistance() {
        return this.primary.squaredDistanceTo(secondary);
    }

    /**
     * Two links are considered equal when the involved entities are the same, regardless of their designation
     * and the links have the same living status.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StringLink link = (StringLink) o;

        boolean partnersEqual = primary.equals(link.primary) && secondary.equals(link.secondary) ||
                primary.equals(link.secondary) && secondary.equals(link.primary);
        return alive == link.alive && partnersEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(primary, secondary, alive);
    }

    /**
     * If due to some error, or unforeseeable causes such as commands
     * the link still exists but needs to be destroyed.
     *
     * @return true when {@link #destroy(boolean)} needs to be called
     */
    public boolean needsBeDestroyed() {
        return primary.isRemoved() || secondary.isRemoved();
    }

    /**
     * Destroys the link including all collision entities and drops an item in its center when the conditions allow it. <br/>
     * This method is idempotent.
     *
     * @param mayDrop if an item may drop.
     */
    public void destroy(boolean mayDrop) {
        if (!alive) return;

        boolean drop = mayDrop;
        World world = primary.world;
        this.alive = false;

        if (world.isClient) return;

        if (secondary instanceof PlayerEntity player && player.isCreative()) drop = false;
        // I think DO_TILE_DROPS makes more sense than DO_ENTITY_DROPS in this case
        if (!world.getGameRules().getBoolean(GameRules.DO_TILE_DROPS)) drop = false;

        if (drop) {
            ItemStack stack = new ItemStack(ToTObjects.STEELSILK);
            if (secondary instanceof PlayerEntity player) {
                player.giveItemStack(stack);
            } else {
                Vec3d middle = Helper.middleOf(primary.getPos(), secondary.getPos());
                ItemEntity itemEntity = new ItemEntity(world, middle.x, middle.y, middle.z, stack);
                itemEntity.setToDefaultPickupDelay();
                world.spawnEntity(itemEntity);
            }
        }

        destroyCollision();
        if (!primary.isRemoved() && !secondary.isRemoved())
            sendDetachStringPacket(world);
    }

    /**
     * Removes the collision entities associated with this link.
     */
    private void destroyCollision() {
        for (Integer entityId : collisionStorage) {
            Entity e = primary.world.getEntityById(entityId);
            if (e instanceof StringCollisionEntity) {
                e.remove(Entity.RemovalReason.DISCARDED);
            } else {

            }
        }
        collisionStorage.clear();
    }

    /**
     * Send a package to all the clients around this entity that notifies them of this link's destruction.
     */
    private void sendDetachStringPacket(World world) {
        assert world instanceof ServerWorld;

        Set<ServerPlayerEntity> trackingPlayers = getTrackingPlayers(world);
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

        // Write both ids so that the client can identify the link
        buf.writeVarInt(primary.getId());
        buf.writeVarInt(secondary.getId());

        for (ServerPlayerEntity player : trackingPlayers) {
            ServerPlayNetworking.send(player, NetworkingPackages.S2C_STRING_DETACH_PACKET_ID, buf);
        }
    }
}
