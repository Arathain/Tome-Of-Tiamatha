package net.arathain.tot.common.util;

import net.arathain.tot.common.entity.string.StringKnotEntity;
import net.arathain.tot.common.entity.string.StringLink;
import net.arathain.tot.common.entity.string.StringLinkEntity;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class ToTCallbacks {
    /**
     * Because of how mods work, this function is called always when a player uses right click.
     * But if the right click doesn't involve this mod (No chain/block to connect to) then we ignore immediately.
     * <p>
     * If it does involve us, then we have work to do, we create connections remove items from inventory and such.
     *
     * @param player    PlayerEntity that right-clicked on a block.
     * @param world     The world the player is in.
     * @param hand      What hand the player used.
     * @param hitResult General information about the block that was clicked.
     * @return An ActionResult.
     * @author Qendolin
     */
    public static ActionResult stringUseEvent(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        if (player == null) return ActionResult.PASS;
        ItemStack stack = player.getStackInHand(hand);
        Item item = stack.getItem();
        BlockPos blockPos = hitResult.getBlockPos();
        Block block = world.getBlockState(blockPos).getBlock();

        if (!StringKnotEntity.canAttachTo(block)) return ActionResult.PASS;
        else if (world.isClient) {
            if (ToTUtil.isDrider(player) && player.isSneaking() && player.getStackInHand(hand).isEmpty()) {
                return ActionResult.SUCCESS;
            }

            // Check if any held chains can be attached. This can be done without holding a chain item
            if (StringKnotEntity.getHeldStringsInRange(player, blockPos).size() > 0) {
                return ActionResult.SUCCESS;
            }

            // Check if a knot exists and can be destroyed
            // Would work without this check but no swing animation would be played
            if (StringKnotEntity.getKnotAt(player.world, blockPos) != null && StringLinkEntity.canDestroyWith(item, player.getRandom())) {
                return ActionResult.SUCCESS;
            }

            return ActionResult.PASS;
        }

        // 1. Try with existing knot, regardless of hand item
        StringKnotEntity knot = StringKnotEntity.getKnotAt(world, blockPos);
        if (knot != null) {
            if (knot.interact(player, hand) == ActionResult.CONSUME) {
                return ActionResult.CONSUME;
            }
            return ActionResult.PASS;
        }

        // 2. Check if any held chains can be attached.
        List<StringLink> attachableChains = StringKnotEntity.getHeldStringsInRange(player, blockPos);

        // Allow default interaction behaviour.
        if (attachableChains.size() == 0 && !(ToTUtil.isDrider(player) && player.isSneaking() && player.getStackInHand(hand).isEmpty())) return ActionResult.PASS;


        // 3. Create new knot if none exists and delegate interaction
        knot = new StringKnotEntity(world, blockPos);
        knot.setGraceTicks((byte) 0);
        world.spawnEntity(knot);
        knot.onPlace();
        return knot.interact(player, hand);
    }

}
