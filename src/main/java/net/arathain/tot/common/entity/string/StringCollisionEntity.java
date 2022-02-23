package net.arathain.tot.common.entity.string;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.arathain.tot.common.init.ToTEntities;
import net.arathain.tot.common.init.ToTObjects;
import net.arathain.tot.common.network.NetworkingPackages;
import net.arathain.tot.common.util.ToTUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ToolItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * StringCollisionEntity is an Entity that is invisible but has a collision.
 * It is used to create a collision for links.
 *
 * @author legoatoom, Qendolin
 */
public class StringCollisionEntity extends Entity implements StringLinkEntity {

    /**
     * The link that this collider is a part of.
     */
    @Nullable
    private StringLink link;

    public StringCollisionEntity(World world, double x, double y, double z, @NotNull StringLink link) {
        this(ToTEntities.STRING_COLLISION, world);
        this.link = link;
        this.setPosition(x, y, z);
    }

    public StringCollisionEntity(EntityType<? extends StringCollisionEntity> entityType, World world) {
        super(entityType, world);
    }

    @SuppressWarnings("unused")
    public @Nullable StringLink getLink() {
        return link;
    }

    @Override
    protected void initDataTracker() {
    }

    /**
     * If this entity can even be collided with.
     * Different from {@link #isCollidable()} as this tells if something can collide with this.
     *
     * @return true when not removed.
     */
    @Override
    public boolean collides() {
        return !isRemoved();
    }

    /**
     * We don't want to be able to push the collision box of the String.
     *
     * @return false
     */
    @Override
    public boolean isPushable() {
        return false;
    }

    /**
     * We only allow the collision box to be rendered if a player is holding an item that has tag {@link FabricToolTags#SHEARS}.
     * This might be helpful when using F3+B to see the boxes of the String.
     *
     * @param distance the camera distance from the collider.
     * @return true when it should be rendered
     */
    @Environment(EnvType.CLIENT)
    @Override
    public boolean shouldRender(double distance) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null && player.isHolding(item -> item.getItem() instanceof ToolItem) || ToTUtil.isDrider(player)) {
            return super.shouldRender(distance);
        } else {
            return false;
        }
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound tag) {
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound tag) {
    }

    /**
     * Makes sure that nothing can walk through it.
     *
     * @return true
     */
    @Override
    public boolean isCollidable() {
        for (Entity entity : this.world.getEntitiesByClass(LivingEntity.class, new Box(this.getPos().add(-0.3, -0.3, -0.3), this.getPos().add(0.3, 0.3, 0.3)), entity -> (!ToTUtil.isDrider(entity)) && !(entity instanceof SpiderEntity))) {
            entity.slowMovement(ToTObjects.HANGING_WEB.getDefaultState(), new Vec3d(entity.isOnGround() ? 0.02f : 0.00000001f, 0.01f, entity.isOnGround() ? 0.02f : 0.00000001f));
        }
        return true;
    }

    /**
     * @see StringKnotEntity#handleAttack(Entity)
     */
    @Override
    public boolean handleAttack(Entity attacker) {
        if (attacker instanceof PlayerEntity playerEntity) {
            this.damage(DamageSource.player(playerEntity), 0.0F);
        } else {
            playSound(SoundEvents.BLOCK_WOOL_HIT, 0.5F, 1.0F);
        }
        return true;
    }

    /**
     * @see StringKnotEntity#damage(DamageSource, float)
     */
    @Override
    public boolean damage(DamageSource source, float amount) {
        ActionResult result = StringLinkEntity.onDamageFrom(this, source);

        if (result.isAccepted()) {
            destroyLinks(result == ActionResult.SUCCESS);
            return true;
        }
        return false;
    }

    @Override
    public void destroyLinks(boolean mayDrop) {
        if (link != null) link.destroy(mayDrop);
    }

    /**
     * Interaction (attack or use) of a player and this entity.
     * Tries to destroy the link with the item in the players hand.
     *
     * @param player The player that interacted.
     * @param hand   The hand that interacted.
     * @return {@link ActionResult#SUCCESS} when the interaction was successful.
     */
    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if (StringLinkEntity.canDestroyWith(player.getStackInHand(hand).getItem(), player.getRandom())) {
            destroyLinks(!player.isCreative());
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    /**
     * The client only needs to know type info for the pick item action.
     * Links are handled server-side.
     */
    @Override
    public Packet<?> createSpawnPacket() {
        Function<PacketByteBuf, PacketByteBuf> extraData = packetByteBuf -> packetByteBuf;
        return StringPacketCreator.createSpawn(this, NetworkingPackages.S2C_SPAWN_STRING_COLLISION_PACKET, extraData);
    }

    /**
     * Destroys broken links and removes itself when there is no alive link.
     */
    @Override
    public void tick() {
        if (world.isClient) return;
        // Condition can be met when the knots were removed with commands
        // but the collider still exists
        if (link != null && link.needsBeDestroyed()) link.destroy(true);

        // Collider removes itself when the link is dead
        if (link == null || link.isDead()) {
            remove(Entity.RemovalReason.DISCARDED);
        }
    }
}
