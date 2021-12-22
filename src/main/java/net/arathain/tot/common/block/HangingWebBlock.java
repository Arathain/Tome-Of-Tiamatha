package net.arathain.tot.common.block;

import net.arathain.tot.common.init.ToTObjects;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class HangingWebBlock extends CobwebBlock {
    private static final BooleanProperty ATTACHED = Properties.ATTACHED;

    public HangingWebBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(ATTACHED, false));
    }

    private BlockState getBlockState(WorldView world, BlockPos pos) {
        return world.getBlockState(pos);
    }

    private void setBlockState(World world, BlockPos pos, BlockState state) {
        world.setBlockState(pos, state, NOTIFY_LISTENERS);
    }

    private BlockState getWebState(boolean value) {
        return getDefaultState().with(ATTACHED, value);
    }

    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext ctx) {
        if (state.get(ATTACHED)) {
            return createCuboidShape(4, 0, 4, 12, 16, 12);
        } else {
            return createCuboidShape(4, 6, 4, 12, 16, 12);
        }
    }

    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (player.getStackInHand(hand).getItem().equals(getPickStack(world, pos, state).getItem())) {
            if (getBlockState(world, pos.down()).equals(getWebState(false))) {
                setBlockState(world, pos.down(), getWebState(true));
                setBlockState(world, pos.down(2), getWebState(false));
                world.playSound(player, pos, SoundEvents.BLOCK_WOOL_PLACE, SoundCategory.BLOCKS, 0, 0);
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
    }

    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return getBlockState(world, pos.up()).isOpaque() || getBlockState(world, pos.up()).isOf(this);
    }

    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos posFrom) {
        if (state.canPlaceAt(world, pos)) {
            if (getBlockState(world, pos.down()).isOf(ToTObjects.HANGING_WEB)) {
                return getWebState(true);
            } else if (getBlockState(world, pos.down()).isOf(this)) {
                return getWebState(true);
            } else {
                return getWebState(false);
            }
        } else {
            return Blocks.AIR.getDefaultState();
        }
    }


    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(ATTACHED);
    }

    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        entity.slowMovement(state, new Vec3d(0.025D, 0.000500074505806D, 0.025D));
    }


    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return type == NavigationType.AIR && !collidable || super.canPathfindThrough(state, world, pos, type);
    }


}
