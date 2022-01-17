package net.arathain.tot.common.item;

import net.arathain.tot.common.util.ToTUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.EvokerFangsEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class SynthesisScepterItem extends MiningToolItem {
    private static final String FOCUS_KEY = "Focus";
    private static final String PRIM_KEY = "Primary";
    private static final String SEC_KEY = "Secondary";
//    private ItemStack focusStack;
//    private ItemStack primaryEffectStack;
//    private ItemStack secondaryEffectStack;
    public SynthesisScepterItem(ToolMaterial material, Settings settings) {
        super(-3, -1, material, BlockTags.PICKAXE_MINEABLE, settings);
    }

    public static void setUsing(ItemStack stack, boolean using) {
        assert stack.getNbt() != null;
        stack.getNbt().putBoolean("using", using);
    }
    public static boolean getUsing(ItemStack stack) {
        assert stack.getNbt() != null;
        return stack.getNbt().getBoolean("using");
    }
    public static void setTargetPos(ItemStack stack, float x, float y, float z) {
        assert stack.getNbt() != null;
        NbtCompound targetPos = new NbtCompound();
        targetPos.putFloat("X", x);
        targetPos.putFloat("Y", y);
        targetPos.putFloat("Z", z);
        stack.getNbt().put("targetPos", targetPos);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (hasFocus(stack)) {
            tooltip.add(new LiteralText(getFocusStack(stack).toString()).formatted(Formatting.GOLD));
        }
        if (hasPrim(stack)) {
            tooltip.add(new LiteralText(getPrimaryEffectStack(stack).toString()).formatted(Formatting.DARK_PURPLE));
        }
        if (hasSec(stack)) {
            tooltip.add(new LiteralText(getSecondaryEffectStack(stack).toString()).formatted(Formatting.LIGHT_PURPLE));
        }
    }
    public static boolean hasFocus(ItemStack stack) {
        if (!stack.hasNbt()) return false;
        assert stack.getNbt() != null;
        return stack.getNbt().contains(FOCUS_KEY);
    }
    public static boolean hasPrim(ItemStack stack) {
        if (!stack.hasNbt()) return false;
        assert stack.getNbt() != null;
        return stack.getNbt().contains(PRIM_KEY);
    }
    public static boolean hasSec(ItemStack stack) {
        if (!stack.hasNbt()) return false;
        assert stack.getNbt() != null;
        return stack.getNbt().contains(SEC_KEY);
    }
    public static ItemStack getFocusStack(ItemStack stack) {
        if(hasFocus(stack)) {
            return ItemStack.fromNbt(stack.getNbt().getCompound(FOCUS_KEY));
        } else {
            return null;
        }
    }
    public static ItemStack getPrimaryEffectStack(ItemStack stack) {
        if(hasFocus(stack)) {
            return ItemStack.fromNbt(stack.getNbt().getCompound(PRIM_KEY));
        } else {
            return null;
        }
    }
    public static ItemStack getSecondaryEffectStack(ItemStack stack) {
        if(hasFocus(stack)) {
            return ItemStack.fromNbt(stack.getNbt().getCompound(SEC_KEY));
        } else {
            return null;
        }
    }
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
//        ItemStack offHandStack = user.getStackInHand(hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND);
//        if (addToScepter(itemStack, offHandStack)) {
//            offHandStack.decrement(1);
//            user.incrementStat(Stats.USED.getOrCreateStat(this));
//            return TypedActionResult.success(itemStack, world.isClient());
//        }
        boolean hitscanRequired = hasFocus(itemStack) && getFocusStack(itemStack).getItem().equals(Items.TOTEM_OF_UNDYING);
        if (!getUsing(itemStack) && hitscanRequired) {
            BlockHitResult blockHit = ToTUtil.hitscanBlock(world, user, 60, RaycastContext.FluidHandling.NONE, (target) -> !target.equals(Blocks.AIR));
            EntityHitResult entityHit = ToTUtil.hitscanEntity(world, user, 60, (target) -> target instanceof LivingEntity && !target.isSpectator() && user.canSee(target));
            if (entityHit !=null) {

                setTargetPos(itemStack, (float)entityHit.getPos().x, (float)entityHit.getPos().y, (float)entityHit.getPos().z);
                setUsing(itemStack, true);
            }
            if (entityHit == null) {
                setTargetPos(itemStack, (float)blockHit.getPos().x, (float)blockHit.getPos().y, (float)blockHit.getPos().z);
                setUsing(itemStack, true);
            }
        }
        return ItemUsage.consumeHeldItem(world, user, hand);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        System.out.println("balls");
        System.out.println(getFocusStack(stack).getItem().equals(Items.TOTEM_OF_UNDYING));
        if(!world.isClient() && hasFocus(stack) && getFocusStack(stack).getItem().equals(Items.TOTEM_OF_UNDYING)) {
            NbtCompound targetPos = (NbtCompound) stack.getNbt().get("targetPos");
            assert targetPos != null;
            Vec3d pos = new Vec3d(targetPos.getFloat("X"), targetPos.getFloat("Y"), targetPos.getFloat("Z"));
            double d = Math.min(pos.getY(), user.getY());
            double e = Math.max(pos.getY(), user.getY()) + 1.0;
            float f = (float) MathHelper.atan2(pos.getZ() - user.getZ(), pos.getX() - user.getX());

            if (user.isSneaking()) {
                float g;
                int i;
                for (i = 0; i < 5; ++i) {
                    g = f + (float)i * (float)Math.PI * 0.4f;
                    this.conjureFangs(user.getX() + (double)MathHelper.cos(g) * 1.5, user.getZ() + (double)MathHelper.sin(g) * 1.5, d, e, g, 0, user);
                }
                for (i = 0; i < 8; ++i) {
                    g = f + (float)i * (float)Math.PI * 2.0f / 8.0f + 1.2566371f;
                    this.conjureFangs(user.getX() + (double)MathHelper.cos(g) * 2.5, user.getZ() + (double)MathHelper.sin(g) * 2.5, d, e, g, 3, user);
                }
            } else {
                for (int i = 0; i < 16; ++i) {
                    double g = 1.25 * (double)(i + 1);
                    this.conjureFangs(user.getX() + (double)MathHelper.cos(f) * g, user.getZ() + (double)MathHelper.sin(f) * g, d, e, f, i, user);
                }
            }
        }
        setUsing(user.getStackInHand(user.getActiveHand()), false);
        return stack;
    }
    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
       // super.onStoppedUsing(stack, world, user, remainingUseTicks);
        setUsing(user.getStackInHand(user.getActiveHand()), false);
    }


    @Override
    public boolean onStackClicked(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player) {
        if (clickType != ClickType.RIGHT) {
            return false;
        }
        ItemStack itemStack = slot.getStack();
        if (itemStack.isEmpty()) {
            removeFirstStack(stack).ifPresent(removedStack -> {
                addToScepter(itemStack, slot.insertStack(removedStack));
            });
        } else if (canAdd(itemStack)) {
            boolean b = addToScepter(stack, itemStack);
            if (b) {
                itemStack.decrement(1);
            }
        }
        return true;
    }
    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        if (clickType != ClickType.RIGHT || !slot.canTakePartial(player)) {
            return false;
        }
        if (otherStack.isEmpty()) {
            removeFirstStack(stack).ifPresent(itemStack -> {
                cursorStackReference.set((ItemStack)itemStack);
            });
        } else {
            boolean b = addToScepter(stack, otherStack);
            if (b) {
                otherStack.decrement(1);
            }
        }
        return true;
    }
    private static Optional<ItemStack> removeFirstStack(ItemStack stack) {
        NbtCompound nbtCompound = stack.getOrCreateNbt();
        ItemStack second;
        if (nbtCompound.contains(SEC_KEY)) {
            second = getSecondaryEffectStack(stack);

            nbtCompound.remove(SEC_KEY);
            return Optional.of(second);
        } else if (nbtCompound.contains(PRIM_KEY)) {
            second = getPrimaryEffectStack(stack);

            nbtCompound.remove(PRIM_KEY);
            return Optional.of(second);
        } else if (nbtCompound.contains(FOCUS_KEY)) {
            second = getFocusStack(stack);

            nbtCompound.remove(FOCUS_KEY);
            return Optional.of(second);
        }
        return Optional.empty();
    }

    private static boolean addToScepter(ItemStack scepter, ItemStack stack) {
        if (stack.isEmpty() || !stack.getItem().canBeNested()) {
            return false;
        }
        NbtCompound nbtCompound = scepter.getOrCreateNbt();
        if(canAdd(stack)) {
            NbtCompound stackNbt = new NbtCompound();
            if (!nbtCompound.contains(FOCUS_KEY)) {
                nbtCompound.put(FOCUS_KEY, stack.writeNbt(stackNbt));
                return true;
            } else if (!nbtCompound.contains(PRIM_KEY)) {
                nbtCompound.put(PRIM_KEY, stack.writeNbt(stackNbt));
                return true;
            } else if (!nbtCompound.contains(SEC_KEY)) {
                nbtCompound.put(SEC_KEY, stack.writeNbt(stackNbt));
                return true;
            }
        }

        return false;
    }
    private static boolean canAdd(ItemStack stack) {
        return stack.getItem() instanceof MagicModifierItem || stack.getItem().equals(Items.TOTEM_OF_UNDYING);
    }

    //hardcoded dumb specific stuff go here
    private void conjureFangs(double x, double z, double maxY, double y, float yaw, int warmup, LivingEntity user) {
        BlockPos blockPos = new BlockPos(x, y, z);
        boolean bl = false;
        double d = 0.0;
        do {
            BlockState blockState2;
            VoxelShape voxelShape;
            BlockPos blockPos2;
            BlockState blockState;
            if (!(blockState = user.world.getBlockState(blockPos2 = blockPos.down())).isSideSolidFullSquare(user.world, blockPos2, Direction.UP)) continue;
            if (!user.world.isAir(blockPos) && !(voxelShape = (blockState2 = user.world.getBlockState(blockPos)).getCollisionShape(user.world, blockPos)).isEmpty()) {
                d = voxelShape.getMax(Direction.Axis.Y);
            }
            bl = true;
            break;
        } while ((blockPos = blockPos.down()).getY() >= MathHelper.floor(maxY) - 1);
        if (bl) {
            user.world.spawnEntity(new EvokerFangsEntity(user.world, x, (double)blockPos.getY() + d, z, yaw, warmup, user));
        }
    }
    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 24;
    }

    @Override
    public boolean isSuitableFor(BlockState state) {
        return true;
    }

    public enum SpellType {
        RAYCAST,
        SUMMON,
        MODIFY
    }
}
