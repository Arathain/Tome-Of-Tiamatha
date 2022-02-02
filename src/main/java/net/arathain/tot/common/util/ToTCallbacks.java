package net.arathain.tot.common.util;

import net.arathain.tot.common.entity.string.StringKnotEntity;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ToTCallbacks {
    /**
     * Because of how mods work, this function is called always when a player uses right click.
     * But if the right click doesn't involve this mod (No string/block to connect to) then we ignore immediately.
     * <p>
     * If it does involve us, then we have work to do, we create connections remove items from inventory and such.
     *
     * //courtesy of legoatoom
     * @param player    PlayerEntity that right-clicked on a block.
     * @param world     The world the player is in.
     * @param hand      What hand the player used.
     * @param hitResult General information about the block that was clicked.
     * @return An ActionResult.
     */
    public static ActionResult stringUseEvent(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        if (player == null) return ActionResult.PASS;
        BlockPos blockPos = hitResult.getBlockPos();
        Block block = world.getBlockState(blockPos).getBlock();
        if (ToTUtil.isDrider(player) && player.isSneaking() && player.getStackInHand(hand).isEmpty()) {
            if (StringKnotEntity.canConnectTo(block)) {
                if (!world.isClient) {
                    StringKnotEntity knot = StringKnotEntity.getOrCreate(world, blockPos, false);
                    if (!StringKnotEntity.tryAttachHeldStringsToBlock(player, world, blockPos, knot)) {
                        // If this didn't work connect the player to the new string instead.
                        assert knot != null; // This can never happen as long as getOrCreate has false as parameter.
                        if (knot.getHoldingEntities().contains(player)) {
                            knot.detachString(player, true, false);
                            knot.onBreak(null);
                        } else if (knot.attachString(player, true, 0)) {
                            knot.onPlace();
                        }
                    }
                }
                return ActionResult.success(world.isClient);
            }
        }
        if (StringKnotEntity.canConnectTo(block)) {
            if (world.isClient) {
                return (ToTUtil.isDrider(player) && player.isSneaking()) ? ActionResult.SUCCESS : ActionResult.PASS;
            } else {
                return StringKnotEntity.tryAttachHeldStringsToBlock(player, world, blockPos, StringKnotEntity.getOrCreate(world, blockPos, true)) ? ActionResult.SUCCESS : ActionResult.PASS;
            }
        }
        return ActionResult.PASS;
    }
}
