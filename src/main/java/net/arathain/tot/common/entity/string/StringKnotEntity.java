package net.arathain.tot.common.entity.string;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.arathain.tot.TomeOfTiamatha;
import net.arathain.tot.common.init.ToTEntities;
import net.arathain.tot.common.init.ToTObjects;
import net.arathain.tot.common.item.SynthesisScepterItem;
import net.arathain.tot.common.network.NetworkingPackages;
import net.arathain.tot.common.util.ToTUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.Block;
import net.minecraft.block.CobwebBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/**
 * The StringKnotEntity is the main entity of this mod.
 * It has links to others of its kind, and is a combination of {@link net.minecraft.entity.mob.MobEntity}
 * and {@link net.minecraft.entity.decoration.LeashKnotEntity}.
 *
 * @author legoatoom, Qendolin, (Arathain was here too)
 */
public class StringKnotEntity extends AbstractDecorationEntity implements StringLinkEntity {

    /**
     * The distance when it is visible.
     */
    public static final double VISIBLE_RANGE = 2048.0D;
    /**
     * Ticks where the knot can live without any links.
     * This is important for 2 reasons: When the world loads, a 'secondary' knot might load before it's 'primary'
     * In which case the knot would remove itself as it has no links and when the 'primary' loads it fails to create
     * a link to this as this is already removed. The second use is for /summon for basically the same reasons.
     */
    private static final byte GRACE_PERIOD = 100;
    /**
     * All links that involve this knot (secondary and primary)
     */
    private final ObjectList<StringLink> links = new ObjectArrayList<>();
    /**
     * Links where the 'secondary' might not exist yet. Will be cleared after the grace period.
     */
    private final ObjectList<NbtElement> incompleteLinks = new ObjectArrayList<>();
    /**
     * Increments each tick, when it reached 100 it resets and checks {@link #canStayAttached()}.
     */
    private int obstructionCheckTimer = 0;
    /**
     * Remaining grace ticks, will be set to 0 when the last incomplete link is removed.
     */
    private byte graceTicks = GRACE_PERIOD;
    /**
     * What block the knot is attached to.
     */
    @Environment(EnvType.CLIENT)
    private Block attachTarget;

    public StringKnotEntity(EntityType<? extends StringKnotEntity> entityType, World world) {
        super(entityType, world);
    }

    public StringKnotEntity(World world, BlockPos pos) {
        super(ToTEntities.STRING_KNOT, world, pos);
        setPosition((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D);
    }

    /**
     * Set the {@link #attachmentPos}.
     *
     * @see #updateAttachmentPosition()
     */
    @Override
    public void setPosition(double x, double y, double z) {
        super.setPosition((double) MathHelper.floor(x) + 0.5D, (double) MathHelper.floor(y) + 0.5D, (double) MathHelper.floor(z) + 0.5D);
    }

    public void setGraceTicks(byte graceTicks) {
        this.graceTicks = graceTicks;
    }

    @Override
    public void setFacing(Direction facing) {
        // AbstractDecorationEntity.facing should not be used - Qendolin
    }

    /**
     * Update the position of this String to the position of the block this is attached to.
     * Also updates the bounding box.
     */
    protected void updateAttachmentPosition() {
        setPos(attachmentPos.getX() + 0.5D, attachmentPos.getY() + 0.5D, attachmentPos.getZ() + 0.5D);
        double w = getType().getWidth() / 2.0;
        double h = getType().getHeight();
        setBoundingBox(new Box(getX() - w, getY(), getZ() - w, getX() + w, getY() + h, getZ() + w));
    }

    /**
     * On the server it:
     * <ol>
     * <li>Checks if its in the void and deletes itself.</li>
     * <li>Tries to convert incomplete links</li>
     * <li>Updates the Strings, see {@link #updateLinks()}</li>
     * <li>Removes any dead links, and, when outside the grace period, itself if none are left.</li>
     * </ol>
     */
    @Override
    public void tick() {
        if (world.isClient) {
            // All other logic in handled on the server. The client only knows enough to render the entity. - Qendolin
            links.removeIf(StringLink::isDead);
            attachTarget = world.getBlockState(attachmentPos).getBlock();
            return;
        }
        attemptTickInVoid();

        boolean anyConverted = convertIncompleteLinks();
        updateLinks();
        removeDeadLinks();

        if (graceTicks < 0 || (anyConverted && incompleteLinks.isEmpty())) {
            graceTicks = 0;
        } else if (graceTicks > 0) {
            graceTicks--;
        }
    }

    /**
     * Will try to convert any {@link #incompleteLinks} using {@link #deserializeStringTag(NbtElement)}.
     *
     * @return true if any were converted
     */
    private boolean convertIncompleteLinks() {
        if (!incompleteLinks.isEmpty()) {
            return incompleteLinks.removeIf(this::deserializeStringTag);
        }
        return false;
    }

    /**
     * Will break all connections that are larger than the {@link #getMaxRange()},
     * when this knot is dead, or can't stay attached.
     */
    private void updateLinks() {
        double squaredMaxRange = getMaxRange() * getMaxRange();
        for (StringLink link : links) {
            if (link.isDead()) continue;

            if (!isAlive()) {
                link.destroy(true);
            } else if (link.primary == this && link.getSquaredDistance() > squaredMaxRange) {
                // no need to check the distance on both ends
                link.destroy(true);
            }
        }

        if (obstructionCheckTimer++ == 100) {
            obstructionCheckTimer = 0;
            if (!canStayAttached()) {
                destroyLinks(true);
            }
        }
    }

    /**
     * Removes any dead links and plays a break sound if any were removed.
     * Removes itself when no {@link #links} or {@link #incompleteLinks} are left, and it's outside the grace period.
     */
    private void removeDeadLinks() {
        boolean playBreakSound = false;
        for (StringLink link : links) {
            if (link.needsBeDestroyed()) link.destroy(true);
            if (link.isDead() && !link.removeSilently) playBreakSound = true;
        }
        if (playBreakSound) onBreak(null);

        links.removeIf(StringLink::isDead);
        if (links.isEmpty() && incompleteLinks.isEmpty() && graceTicks <= 0) {
            remove(RemovalReason.DISCARDED);
            // No break sound
        }
    }

    /**
     * This method tries to connect to the secondary that is in the {@link #incompleteLinks}.
     * If they do not exist yet, we try again later. If they do, make a connection and remove it from the tag.
     * <br>
     * When the grace period is over, we remove the tag from the {@link #incompleteLinks} and drop an item
     * meaning that we cannot find the connection anymore, and we assume that it will not be loaded in the future.
     *
     * @param element the tag that contains a single connection.
     * @return true if the tag has been used
     * @see #updateLinks()
     */
    private boolean deserializeStringTag(NbtElement element) {
        if (element == null || world.isClient) {
            return true;
        }

        assert element instanceof NbtCompound;
        NbtCompound tag = (NbtCompound) element;

        if (tag.contains("UUID")) {
            UUID uuid = tag.getUuid("UUID");
            Entity entity = ((ServerWorld) world).getEntity(uuid);
            if (entity != null) {
                StringLink.create(this, entity);
                return true;
            }
        } else if (tag.contains("RelX") || tag.contains("RelY") || tag.contains("RelZ")) {
            BlockPos blockPos = new BlockPos(tag.getInt("RelX"), tag.getInt("RelY"), tag.getInt("RelZ"));
            // Adjust position to be relative to our facing direction
            blockPos = getBlockPosAsFacingRelative(blockPos, Direction.fromRotation(this.getYaw()));
            StringKnotEntity entity = StringKnotEntity.getKnotAt(world, blockPos.add(attachmentPos));
            if (entity != null) {
                StringLink.create(this, entity);
                return true;
            }
        } else {
            //concern.ing
        }

        // At the start the server and client need to tell each other the info.
        // So we need to check if the object is old enough for these things to exist before we delete them.
        if (graceTicks <= 0) {
            dropItem(ToTObjects.STEELSILK);
            onBreak(null);
            return true;
        }

        return false;
    }

    /**
     * The maximum distance between two knots.
     */
    public static double getMaxRange() {
        return TomeOfTiamatha.CONFIG.maxStringRange;
    }

    /**
     * Simple checker to see if the block is connected to a fence or a wall.
     *
     * @return true if it can stay attached.
     */
    public boolean canStayAttached() {
        Block block = world.getBlockState(attachmentPos).getBlock();
        return canAttachTo(block);
    }

    /**
     * Destroys all links and sets the grace ticks to 0
     *
     * @param mayDrop true when the links should drop
     */
    @Override
    public void destroyLinks(boolean mayDrop) {
        for (StringLink link : links) {
            link.destroy(mayDrop);
        }
        graceTicks = 0;
    }

    @Override
    public void onBreak(@Nullable Entity entity) {
        playSound(SoundEvents.BLOCK_WOOL_BREAK, 1.0F, 1.0F);
    }

    /**
     * To support structure blocks which can rotate structures we need to treat the relative secondary position in the
     * NBT as relative to our facing direction.
     *
     * @param relPos The relative position when the knot would be facing the +Z direction (0 deg).
     * @param facing The target direction
     * @return The yaw's equivalent block rotation.
     */
    private BlockPos getBlockPosAsFacingRelative(BlockPos relPos, Direction facing) {
        BlockRotation rotation = BlockRotation.values()[facing.getHorizontal()];
        return relPos.rotate(rotation);
    }

    /**
     * Searches for a knot at {@code pos} and returns it.
     *
     * @param world The world to search in.
     * @param pos   The position to search at.
     * @return {@link StringKnotEntity} or null when none exists at {@code pos}.
     */
    @Nullable
    public static StringKnotEntity getKnotAt(World world, BlockPos pos) {
        List<StringKnotEntity> results = world.getNonSpectatingEntities(StringKnotEntity.class,
                Box.of(Vec3d.of(pos), 2, 2, 2));

        for (StringKnotEntity current : results) {
            if (current.getDecorationBlockPos().equals(pos)) {
                return current;
            }
        }

        return null;
    }

    /**
     * Is this block acceptable to attach a knot?
     *
     * @param block the block in question.
     * @return true if is allowed.
     */
    public static boolean canAttachTo(Block block) {
        return block.getStateManager().getDefaultState().isOpaque() || block instanceof CobwebBlock;
    }

    /**
     * Mirrors the incomplete links, otherwise {@link #getBlockPosAsFacingRelative(BlockPos, Direction)} won't work.
     */
    @Override
    public float applyMirror(BlockMirror mirror) {
        if(mirror != BlockMirror.NONE) {
            // Mirror the X axis, I am not sure why
            for (NbtElement element : incompleteLinks) {
                if (element instanceof NbtCompound link) {
                    if (link.contains("RelX")) {
                        link.putInt("RelX", -link.getInt("RelX"));
                    }
                }
            }
        }
            // Opposite of Entity.applyMirror, again I am not sure why, but it works
            float yaw = MathHelper.wrapDegrees(this.getYaw());
            return switch (mirror) {
                case LEFT_RIGHT -> 180 - yaw;
                case FRONT_BACK -> -yaw;
                default -> yaw;
            };
    }

    /**
     * Calls {@link #damage(DamageSource, float)} when attacked by a player. Plays a hit sound otherwise. <br/>
     * It is used by {@link PlayerEntity#attack(Entity)} where a true return value indicates
     * that this entity handled the attack and no further actions should be made.
     *
     * @param attacker The source of the attack.
     * @return true
     */
    @Override
    public boolean handleAttack(Entity attacker) {
        if (attacker instanceof PlayerEntity playerEntity) {
            damage(DamageSource.player(playerEntity), 0.0F);
        } else {
            playSound(SoundEvents.BLOCK_WOOL_HIT, 0.5F, 1.0F);
        }
        return true;
    }

    /**
     * @return true when damage was effective
     * @see StringKnotEntity#onDamageFrom(Entity, DamageSource)
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

    /**
     * Stores all primary links
     * and old, incomplete links inside {@code root}
     *
     * @param root the tag to write info in.
     */
    @Override
    public void writeCustomDataToNbt(NbtCompound root) {
        StringKnotFixer.INSTANCE.addVersionTag(root);
        NbtList linksTag = new NbtList();

        // Write complete links
        for (StringLink link : links) {
            if (link.isDead()) continue;
            if (link.primary != this) continue;
            Entity secondary = link.secondary;
            NbtCompound compoundTag = new NbtCompound();
            if (secondary instanceof PlayerEntity) {
                UUID uuid = secondary.getUuid();
                compoundTag.putUuid("UUID", uuid);
            } else if (secondary instanceof AbstractDecorationEntity) {
                BlockPos srcPos = this.attachmentPos;
                BlockPos dstPos = ((AbstractDecorationEntity) secondary).getDecorationBlockPos();
                BlockPos relPos = dstPos.subtract(srcPos);
                // Inverse rotation to store the position as 'facing' agnostic
                Direction inverseFacing = Direction.fromRotation(Direction.SOUTH.asRotation() - getYaw());
                relPos = getBlockPosAsFacingRelative(relPos, inverseFacing);
                compoundTag.putInt("RelX", relPos.getX());
                compoundTag.putInt("RelY", relPos.getY());
                compoundTag.putInt("RelZ", relPos.getZ());
            }
            linksTag.add(compoundTag);
        }

        // Write old, incomplete links
        linksTag.addAll(incompleteLinks);

        if (!linksTag.isEmpty()) {
            root.put("Strings", linksTag);
        }
    }

    /**
     * Read all the data from {@link #writeCustomDataToNbt(NbtCompound)}
     * and stores the links in {@link #incompleteLinks}.
     *
     * @param root the tag to read from.
     */
    public void readCustomDataFromNbt(NbtCompound root) {
        if (root.contains("Strings")) {
            incompleteLinks.addAll(root.getList("Strings", NbtType.COMPOUND));
        }
    }

    @Override
    public int getWidthPixels() {
        return 9;
    }

    @Override
    public int getHeightPixels() {
        return 9;
    }

    /**
     * Checks if the {@code distance} is within the {@link #VISIBLE_RANGE visible range}.
     *
     * @param distance the camera distance from the knot.
     * @return true when it is in range.
     */
    @Environment(EnvType.CLIENT)
    @Override
    public boolean shouldRender(double distance) {
        return distance < VISIBLE_RANGE;
    }

    @Override
    public Vec3d getLeashOffset() {
        return new Vec3d(0, 4.5 / 16, 0);
    }

    /**
     * The offset where a leash / string will visually connect to.
     */
    @Environment(EnvType.CLIENT)
    @Override
    public Vec3d getLeashPos(float f) {
        return getLerpedPos(f);
    }

    @Override
    protected float getEyeHeight(EntityPose pose, EntityDimensions dimensions) {
        return 0;
    }

    /**
     * Interaction (attack or use) of a player and this entity.
     * On the server it will:
     * <ol>
     * <li>Try to move existing link from player to this.</li>
     * <li>Try to cancel String links (when clicking a knot that already has a connection to {@code player}).</li>
     * <li>Try to create a new connection.</li>
     * <li>Try to destroy the knot with the item in the players hand.</li>
     * </ol>
     *
     * @param player The player that interacted.
     * @param hand   The hand that interacted.
     * @return {@link ActionResult#SUCCESS} or {@link ActionResult#CONSUME} when the interaction was successful.
     * @see #tryAttachHeldStrings(PlayerEntity)
     */
    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        Item handItem = player.getStackInHand(hand).getItem();
        if (world.isClient) {
            if (handItem instanceof SynthesisScepterItem || ToTUtil.isDrider(player)) {
                return ActionResult.SUCCESS;
            }

            if (StringLinkEntity.canDestroyWith(handItem, player.getRandom())) {
                return ActionResult.SUCCESS;
            }

            return ActionResult.PASS;
        }

        // 1. Try to move existing link from player to this.
        boolean madeConnection = tryAttachHeldStrings(player);
        if (madeConnection) {
            onPlace();
            return ActionResult.CONSUME;
        }

        // 2. Try to cancel String links (when clicking same knot twice)
        boolean broke = false;
        for (StringLink link : links) {
            if (link.secondary == player) {
                broke = true;
                link.destroy(true);
            }
        }
        if (broke) {
            return ActionResult.CONSUME;
        }

        // 3. Try to create a new connection
        if (handItem instanceof SynthesisScepterItem || ToTUtil.isDrider(player)) {
            // Interacted with a valid String item, create a new link
            onPlace();
            StringLink.create(this, player);
            if (!player.isCreative()) {
                player.getStackInHand(hand).decrement(1);
            }

            return ActionResult.CONSUME;
        }

        // 4. Interacted with anything else, check for shears
        if (StringLinkEntity.canDestroyWith(handItem, player.getRandom())) {
            destroyLinks(!player.isCreative());
            graceTicks = 0;
            return ActionResult.CONSUME;
        }

        return ActionResult.PASS;
    }

    /**
     * Destroys all Strings held by {@code player} that are in range and creates new links to itself.
     *
     * @param player the player wo tries to make a connection.
     * @return true if it has made a connection.
     */
    public boolean tryAttachHeldStrings(PlayerEntity player) {
        boolean hasMadeConnection = false;
        List<StringLink> attachableLinks = getHeldStringsInRange(player, getDecorationBlockPos());
        for (StringLink link : attachableLinks) {
            // Prevent connections with self
            if (link.primary == this) continue;

            // Move that link to this knot
            StringLink newLink = StringLink.create(link.primary, this);

            // Check if the link does not already exist
            if (newLink != null) {
                link.destroy(false);
                link.removeSilently = true;
                hasMadeConnection = true;
            }
        }
        return hasMadeConnection;
    }

    @Override
    public void onPlace() {
        playSound(SoundEvents.BLOCK_WOOL_PLACE, 1.0F, 1.0F);
    }

    /**
     * Searches for other {@link StringKnotEntity StringKnotEntities} that are in range of {@code target} and
     * have a link to {@code player}.
     *
     * @param player the player wo tries to make a connection.
     * @param target center of the range
     * @return a list of all held Strings that are in range of {@code target}
     */
    public static List<StringLink> getHeldStringsInRange(PlayerEntity player, BlockPos target) {
        Box searchBox = Box.of(Vec3d.of(target), getMaxRange() * 2, getMaxRange() * 2, getMaxRange() * 2);
        List<StringKnotEntity> otherKnots = player.world.getNonSpectatingEntities(StringKnotEntity.class, searchBox);

        List<StringLink> attachableLinks = new ArrayList<>();

        for (StringKnotEntity source : otherKnots) {
            for (StringLink link : source.getLinks()) {
                if (link.secondary != player) continue;
                // We found a knot that is connected to the player.
                attachableLinks.add(link);
            }
        }
        return attachableLinks;
    }

    /**
     * @return all complete links that are associated with this knot.
     * @apiNote Operating on the list has potential for bugs as it does not include incomplete links.
     * For example {@link StringLink#create(StringKnotEntity, Entity)} checks if the link already exists
     * using this list. Same goes for {@link #tryAttachHeldStrings(PlayerEntity)}
     * but at the end of the day it doesn't really matter.
     * When an incomplete link is not resolved within the first two ticks it is unlikely to ever complete.
     * And even if it completes it will be stopped either because the knot is dead or the duplicates check in {@code StringLink}.
     */
    public List<StringLink> getLinks() {
        return links;
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.BLOCKS;
    }

    /**
     * Writes all client side relevant information into a {@link NetworkingPackages#S2C_SPAWN_STRING_KNOT_PACKET} packet and sends it.
     *
     * @see StringPacketCreator
     */
    @Override
    public Packet<?> createSpawnPacket() {
        Function<PacketByteBuf, PacketByteBuf> extraData = packetByteBuf -> {
            return packetByteBuf;
        };
        return StringPacketCreator.createSpawn(this, NetworkingPackages.S2C_SPAWN_STRING_KNOT_PACKET, extraData);
    }

    /**
     * Checks if the knot model of the knot entity should be rendered.
     * To determine if the knot entity including Strings should be rendered use {@link #shouldRender(double)}
     *
     * @return true if the knot is not attached to a wall.
     */
    @Environment(EnvType.CLIENT)
    public boolean shouldRenderKnot() {
        return false;
    }

    public void addLink(StringLink link) {
        links.add(link);
    }
}
