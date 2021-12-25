package net.arathain.tot.mixin;

import net.arathain.tot.common.entity.ToTUtil;
import net.arathain.tot.common.entity.goal.BetterLeapAtTargetGoal;
import net.arathain.tot.common.entity.movement.BetterSpiderPathNavigation;
import net.arathain.tot.common.entity.spider.IClimberEntity;
import net.arathain.tot.common.entity.spider.IMobEntityHook;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.entity.SpiderEntityRenderer;
import net.minecraft.client.render.entity.model.SpiderEntityModel;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.PounceAtTargetGoal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(value = SpiderEntity.class, priority = 1001)
public abstract class BetterSpiderEntityMixin extends HostileEntity implements IClimberEntity, IMobEntityHook {

//    private static final UUID FOLLOW_RANGE_INCREASE_ID = UUID.fromString("9e815957-3a8e-4b65-afbc-eba39d2a06b4");
//    private static final AttributeModifier FOLLOW_RANGE_INCREASE = new AttributeModifier(FOLLOW_RANGE_INCREASE_ID, "Spiders 2.0 follow range increase", 8.0D, AttributeModifier.Operation.ADDITION);

    private boolean pathFinderDebugPreview;

    private BetterSpiderEntityMixin(EntityType<? extends HostileEntity> type, World worldIn) {
        super(type, worldIn);
    }

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void onConstructed(CallbackInfo ci) {
        this.getAttributeInstance(EntityAttributes.GENERIC_FOLLOW_RANGE);
    }

    @Override
    public EntityNavigation onCreateNavigation(World world) {
        BetterSpiderPathNavigation<BetterSpiderEntityMixin> navigate = new BetterSpiderPathNavigation<>(this, world, false);
        navigate.setCanSwim(true);
        return navigate;
    }

    @Inject(method = "initDataTracker()V", at = @At("HEAD"))
    private void onRegisterData(CallbackInfo ci) {
        //this.pathFinderDebugPreview = Config.PATH_FINDER_DEBUG_PREVIEW.get();
    }
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/goal/GoalSelector;add(ILnet/minecraft/entity/ai/goal/Goal;)V", ordinal = 1), method = "initGoals")
    private void redirectPounceGoal(GoalSelector goalSelector, int priority, Goal goal) {
        goalSelector.add(3, new BetterLeapAtTargetGoal<>(this, 0.4f));
    }
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/goal/GoalSelector;add(ILnet/minecraft/entity/ai/goal/Goal;)V", ordinal = 7), method = "initGoals")
    private void redirectTargetGoal(GoalSelector goalSelector, int priority, Goal goal) {
        Goal newGoal = new ActiveTargetGoal<>(this, PlayerEntity.class, 10, true, false, player -> !ToTUtil.isDrider(player)).setMaxTimeWithoutVisibility(200);
        goalSelector.add(priority, newGoal);
    }
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/goal/GoalSelector;add(ILnet/minecraft/entity/ai/goal/Goal;)V", ordinal = 8), method = "initGoals")
    private void redirectIGGoal(GoalSelector goalSelector, int priority, Goal goal) {
        Goal newGoal = new ActiveTargetGoal<>(this, IronGolemEntity.class, 10, true, false, ironGolem -> true).setMaxTimeWithoutVisibility(200);
        goalSelector.add(priority, newGoal);
    }

    @Override
    public boolean shouldTrackPathingTargets() {
        return this.pathFinderDebugPreview;
    }

    @Override
    public boolean canClimbOnBlock(BlockState state, BlockPos pos) {
        return true;
    }


    @Override
    public float getBlockSlipperiness(BlockPos pos) {
        BlockState offsetState = this.world.getBlockState(pos);

       float slipperiness = offsetState.getBlock().getSlipperiness() * 0.91f;

//        if(offsetState.getBlock().isIn(ModTags.NON_CLIMBABLE)) {
//            slipperiness = 1 - (1 - slipperiness) * 0.25f;
//        }

        return slipperiness;
    }

    @Override
    public float getPathingPenalty(WorldAccess cache, MobEntity entity, PathNodeType nodeType, BlockPos pos, Vec3i direction, Predicate<Direction> sides) {
        if(direction.getY() != 0) {
//            if(Config.PREVENT_CLIMBING_IN_RAIN.get() && !sides.test(Direction.UP) && !sides.test(Direction.DOWN) && this.world.isRainingAt(pos)) {
//                return -1.0f;
//            }

            boolean hasClimbableNeigbor = false;

            BlockPos.Mutable offsetPos = new BlockPos.Mutable();

            for(Direction offset : Direction.values()) {
                if(sides.test(offset)) {
                    offsetPos.set(pos.getX() + offset.getOffsetX(), pos.getY() + offset.getOffsetY(), pos.getZ() + offset.getOffsetZ());

                    BlockState state = cache.getBlockState(offsetPos);

                    if(this.canClimbOnBlock(state, offsetPos)) {
                        hasClimbableNeigbor = true;
                    }
                }
            }

            if(!hasClimbableNeigbor) {
                return -1.0f;
            }
        }

        return entity.getPathfindingPenalty(nodeType);
    }
}
