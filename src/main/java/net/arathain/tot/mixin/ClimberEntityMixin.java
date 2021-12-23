package net.arathain.tot.mixin;

import com.google.common.collect.ImmutableList;
import net.arathain.tot.common.entity.CachedCollisionView;
import net.arathain.tot.common.entity.CollisionSmoothingUtil;
import net.arathain.tot.common.entity.movement.*;
import net.arathain.tot.common.entity.spider.IClimberEntity;
import net.arathain.tot.common.entity.spider.IEntityMovementHook;
import net.arathain.tot.common.entity.spider.Orientation;
import net.arathain.tot.common.entity.spider.PathingTarget;
import net.minecraft.block.BlockState;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.CollisionView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Mixin(SpiderEntity.class)
public abstract class ClimberEntityMixin extends HostileEntity implements IClimberEntity, IEntityMovementHook {
    private static final UUID SLOW_FALLING_ID = UUID.fromString("A5B6CF2A-2F7C-31EF-9022-7C3E7D5E6ABA");
    private static final EntityAttributeModifier SLOW_FALLING = new EntityAttributeModifier(SLOW_FALLING_ID, "Slow falling acceleration reduction", -0.07, EntityAttributeModifier.Operation.ADDITION);

    private static final TrackedData<Float> MOVEMENT_TARGET_X;
    private static final TrackedData<Float> MOVEMENT_TARGET_Y;
    private static final TrackedData<Float> MOVEMENT_TARGET_Z;
    private static final ImmutableList<TrackedData<Optional<BlockPos>>> PATHING_TARGETS;
    private static final ImmutableList<TrackedData<Direction>> PATHING_SIDES;

    private static final TrackedData<EulerAngle> ROTATION_BODY;
    private static final TrackedData<EulerAngle> ROTATION_HEAD;

    static {
        @SuppressWarnings("unchecked")
        Class<Entity> cls = (Class<Entity>) MethodHandles.lookup().lookupClass();

        MOVEMENT_TARGET_X = DataTracker.registerData(cls, TrackedDataHandlerRegistry.FLOAT);
        MOVEMENT_TARGET_Y = DataTracker.registerData(cls, TrackedDataHandlerRegistry.FLOAT);
        MOVEMENT_TARGET_Z = DataTracker.registerData(cls, TrackedDataHandlerRegistry.FLOAT);

        ImmutableList.Builder<TrackedData<Optional<BlockPos>>> pathingTargets = ImmutableList.builder();
        ImmutableList.Builder<TrackedData<Direction>> pathingSides = ImmutableList.builder();
        for(int i = 0; i < 8; i++) {
            pathingTargets.add(DataTracker.registerData(cls, TrackedDataHandlerRegistry.OPTIONAL_BLOCK_POS));
            pathingSides.add(DataTracker.registerData(cls, TrackedDataHandlerRegistry.FACING));
        }
        PATHING_TARGETS = pathingTargets.build();
        PATHING_SIDES = pathingSides.build();

        ROTATION_BODY = DataTracker.registerData(cls, TrackedDataHandlerRegistry.ROTATION);
        ROTATION_HEAD = DataTracker.registerData(cls, TrackedDataHandlerRegistry.ROTATION);
    }

    private double prevAttachmentOffsetX, prevAttachmentOffsetY, prevAttachmentOffsetZ;
    private double attachmentOffsetX, attachmentOffsetY, attachmentOffsetZ;

    private Vec3d attachmentNormal = new Vec3d(0, 1, 0);
    private Vec3d prevAttachmentNormal = new Vec3d(0, 1, 0);

    private float prevOrientationYawDelta;
    private float orientationYawDelta;

    private double lastAttachmentOffsetX, lastAttachmentOffsetY, lastAttachmentOffsetZ;
    private Vec3d lastAttachmentOrientationNormal = new Vec3d(0, 1, 0);

    private int attachedTicks = 5;

    private Vec3d attachedSides = new Vec3d(0, 0, 0);
    private Vec3d prevAttachedSides = new Vec3d(0, 0, 0);

    private boolean canClimbInWater = false;
    private boolean canClimbInLava = false;

    private boolean isClimbingDisabled = false;

    private float collisionsInclusionRange = 2.0f;
    private float collisionsSmoothingRange = 1.25f;

    private Orientation orientation;
    private Pair<Direction, Vec3d> groundDirection;

    private Orientation renderOrientation;

    private float nextStepDistance, nextFlap;
    private Vec3d preWalkingPosition;

    private double preMoveY;

    private Vec3d jumpDir;

    private ClimberEntityMixin(EntityType<? extends HostileEntity> type, World worldIn) {
        super(type, worldIn);
        this.stepHeight = 0.1f;
        this.orientation = this.calculateOrientation(1);
        this.groundDirection = this.getGroundDirection();
        this.moveControl = new ClimberMoveControl<>(this);
        this.lookControl = new ClimberLookControl<>(this);
        this.jumpControl = new ClimberJumpControl<>(this);
        this.prevAttachmentOffsetY = this.attachmentOffsetY = this.lastAttachmentOffsetY = this.getVerticalOffset(1);

    }

    //createNavigator overrides usually don't call super.createNavigator so this ensures that onCreateNavigator
    //still gets called in such cases
    @Inject(method = "createNavigation(Lnet/minecraft/world/World;)Lnet/minecraft/entity/ai/pathing/EntityNavigation;", at = @At("HEAD"), cancellable = true, require = 0, expect = 0)
    private void onCreateNavigator(World world, CallbackInfoReturnable<EntityNavigation> cir) {
        EntityNavigation navigator = this.createNavigation(world);
        if(navigator != null) {
            cir.setReturnValue(navigator);
        }
    }

    @Override
    public EntityNavigation createNavigation(World world) {
        AdvancedClimberPathNavigation<ClimberEntityMixin> navigation = new AdvancedClimberPathNavigation<>(this, world, false, true);
        navigation.setCanSwim(true);
        return navigation;
    }

    @Override
    public void initDataTracker() {
        super.initDataTracker();
        if(this.shouldTrackPathingTargets()) {
            this.dataTracker.set(MOVEMENT_TARGET_X, 0.0f);
            this.dataTracker.set(MOVEMENT_TARGET_Y, 0.0f);
            this.dataTracker.set(MOVEMENT_TARGET_Z, 0.0f);

            for(TrackedData<Optional<BlockPos>> pathingTarget : PATHING_TARGETS) {
                this.dataTracker.set(pathingTarget, Optional.empty());
            }

            for(TrackedData<Direction> pathingSide : PATHING_SIDES) {
                this.dataTracker.set(pathingSide, Direction.DOWN);
            }
        }
        this.dataTracker.set(ROTATION_BODY, new EulerAngle(0, 0, 0));

        this.dataTracker.set(ROTATION_HEAD, new EulerAngle(0, 0, 0));
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putDouble("SpidersTPO.AttachmentNormalX", this.attachmentNormal.x);
        nbt.putDouble("SpidersTPO.AttachmentNormalY", this.attachmentNormal.y);
        nbt.putDouble("SpidersTPO.AttachmentNormalZ", this.attachmentNormal.z);

        nbt.putInt("SpidersTPO.AttachedTicks", this.attachedTicks);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.prevAttachmentNormal = this.attachmentNormal = new Vec3d(
                nbt.getDouble("SpidersTPO.AttachmentNormalX"),
                nbt.getDouble("SpidersTPO.AttachmentNormalY"),
                nbt.getDouble("SpidersTPO.AttachmentNormalZ")
        );

        this.attachedTicks = nbt.getInt("SpidersTPO.AttachedTicks");

        this.orientation = this.calculateOrientation(1);
    }

    @Override
    public boolean canClimbInWater() {
        return this.canClimbInWater;
    }

    @Override
    public void setCanClimbInWater(boolean value) {
        this.canClimbInWater = value;
    }

    @Override
    public boolean canClimbInLava() {
        return this.canClimbInLava;
    }

    @Override
    public void setCanClimbInLava(boolean value) {
        this.canClimbInLava = value;
    }

    @Override
    public float getCollisionsInclusionRange() {
        return this.collisionsInclusionRange;
    }

    @Override
    public void setCollisionsInclusionRange(float range) {
        this.collisionsInclusionRange = range;
    }

    @Override
    public float getCollisionsSmoothingRange() {
        return this.collisionsSmoothingRange;
    }

    @Override
    public void setCollisionsSmoothingRange(float range) {
        this.collisionsSmoothingRange = range;
    }

    @Override
    public float getBridgePathingPenalty(MobEntity entity, BlockPos pos, @Nullable PathNode fallPathNode) {
        return -1.0f;
    }

    @Override
    public void onPathingObstructed(Direction facing) {

    }

    @Override
    public int getSafeFallDistance() {
        return 0;
    }

    @Override
    public float getMovementSpeed() {
        EntityAttributeInstance attribute = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED); //MOVEMENT_SPEED
        return attribute != null ? (float) attribute.getValue() : 1.0f;
    }

    private static double calculateOffsetX(Box aabb, Box other, double offsetX) {
        if(other.maxY > aabb.minY && other.minY < aabb.maxY && other.maxZ > aabb.minZ && other.minZ < aabb.maxZ) {
            if(offsetX > 0.0D && other.maxX <= aabb.minX) {
                double dx = aabb.minX - other.maxX;

                if(dx < offsetX) {
                    offsetX = dx;
                }
            } else if(offsetX < 0.0D && other.minX >= aabb.maxX) {
                double dx = aabb.maxX - other.minX;

                if(dx > offsetX) {
                    offsetX = dx;
                }
            }

            return offsetX;
        } else {
            return offsetX;
        }
    }

    private static double calculateOffsetY(Box aabb, Box other, double offsetY) {
        if(other.maxX > aabb.minX && other.minX < aabb.maxX && other.maxZ > aabb.minZ && other.minZ < aabb.maxZ) {
            if(offsetY > 0.0D && other.maxY <= aabb.minY) {
                double dy = aabb.minY - other.maxY;

                if(dy < offsetY) {
                    offsetY = dy;
                }
            } else if(offsetY < 0.0D && other.minY >= aabb.maxY) {
                double dy = aabb.maxY - other.minY;

                if(dy > offsetY) {
                    offsetY = dy;
                }
            }

            return offsetY;
        } else {
            return offsetY;
        }
    }

    private static double calculateOffsetZ(Box aabb, Box other, double offsetZ) {
        if(other.maxX > aabb.minX && other.minX < aabb.maxX && other.maxY > aabb.minY && other.minY < aabb.maxY) {
            if(offsetZ > 0.0D && other.maxZ <= aabb.minZ) {
                double dz = aabb.minZ - other.maxZ;

                if(dz < offsetZ) {
                    offsetZ = dz;
                }
            } else if(offsetZ < 0.0D && other.minZ >= aabb.maxZ) {
                double dz = aabb.maxZ - other.minZ;

                if(dz > offsetZ) {
                    offsetZ = dz;
                }
            }

            return offsetZ;
        } else {
            return offsetZ;
        }
    }

    private void updateWalkingSide() {
        Direction avoidPathingFacing = null;

        Box entityBox = this.getBoundingBox();

        double closestFacingDst = Double.MAX_VALUE;
        Direction closestFacing = null;

        Vec3d weighting = new Vec3d(0, 0, 0);

        float stickingDistance = this.forwardSpeed != 0 ? 1.5f : 0.1f;

        for(Direction facing : Direction.values()) {
            if(avoidPathingFacing == facing || !this.canAttachToSide(facing)) {
                continue;
            }

            List<Box> collisionBoxes = this.getClimbableCollisionBoxes(entityBox.expand(0.2f).expand(facing.getOffsetX() * stickingDistance, facing.getOffsetY() * stickingDistance, facing.getOffsetZ() * stickingDistance));

            double closestDst = Double.MAX_VALUE;

            for(Box collisionBox : collisionBoxes) {
                switch(facing) {
                    case EAST:
                    case WEST:
                        closestDst = Math.min(closestDst, Math.abs(calculateOffsetX(entityBox, collisionBox, -facing.getOffsetX() * stickingDistance)));
                        break;
                    case UP:
                    case DOWN:
                        closestDst = Math.min(closestDst, Math.abs(calculateOffsetY(entityBox, collisionBox, -facing.getOffsetY() * stickingDistance)));
                        break;
                    case NORTH:
                    case SOUTH:
                        closestDst = Math.min(closestDst, Math.abs(calculateOffsetZ(entityBox, collisionBox, -facing.getOffsetZ() * stickingDistance)));
                        break;
                }
            }

            if(closestDst < closestFacingDst) {
                closestFacingDst = closestDst;
                closestFacing = facing;
            }

            if(closestDst < Double.MAX_VALUE) {
                weighting = weighting.add(new Vec3d(facing.getOffsetX(), facing.getOffsetY(), facing.getOffsetZ()).multiply(1 - Math.min(closestDst, stickingDistance) / stickingDistance));
            }
        }

        if(closestFacing == null) {
            this.groundDirection = new Pair<>(Direction.DOWN, new Vec3d(0, -1, 0));
        } else {
            this.groundDirection = new Pair<>(closestFacing, new Vec3d(weighting.normalize().add(0, -0.001f, 0).normalize().x, weighting.normalize().add(0, -0.001f, 0).normalize().y, weighting.normalize().add(0, -0.001f, 0).normalize().z));
        }
    }

    @Override
    public boolean canAttachToSide(Direction side) {
        return true;
    }

    @Override
    public Pair<Direction, Vec3d> getGroundDirection() {
        return this.groundDirection;
    }

    @Override
    public Direction getGroundSide() {
        return this.groundDirection.getLeft();
    }

    @Override
    public Orientation getOrientation() {
        return this.orientation;
    }

    @Override
    public void setRenderOrientation(Orientation orientation) {
        this.renderOrientation = orientation;
    }

    @Override
    public Orientation getRenderOrientation() {
        return this.renderOrientation;
    }

    @Override
    public float getAttachmentOffset(Direction.Axis axis, float partialTicks) {
        switch(axis) {
            default:
            case X:
                return (float) (this.prevAttachmentOffsetX + (this.attachmentOffsetX - this.prevAttachmentOffsetX) * partialTicks);
            case Y:
                return (float) (this.prevAttachmentOffsetY + (this.attachmentOffsetY - this.prevAttachmentOffsetY) * partialTicks);
            case Z:
                return (float) (this.prevAttachmentOffsetZ + (this.attachmentOffsetZ - this.prevAttachmentOffsetZ) * partialTicks);
        }
    }

    @Override
    public void lookAt(EntityAnchorArgumentType.EntityAnchor anchorPoint, Vec3d target) {
        Vec3d dir = target.subtract(this.getPos());
        dir = this.getOrientation().getLocal(dir);
        super.lookAt(anchorPoint, dir);
    }

    @Override
    public void tick() {
        if(!this.world.isClient() && this.world instanceof ServerWorld) {
                Orientation orientation = this.getOrientation();

                Vec3d look = orientation.getGlobal(this.getYaw(), this.getPitch());
                this.dataTracker.set(ROTATION_BODY, new EulerAngle((float) look.x, (float) look.y, (float) look.z));

                look = orientation.getGlobal(this.headYaw, 0.0f);
                this.dataTracker.set(ROTATION_HEAD, new EulerAngle((float) look.x, (float) look.y, (float) look.z));

                if(this.shouldTrackPathingTargets()) {
                    if(this.sidewaysSpeed != 0) {
                        Vec3d forwardVector = orientation.getGlobal(this.getYaw(), 0);
                        Vec3d strafeVector = orientation.getGlobal(this.getYaw() - 90.0f, 0);

                        Vec3d offset = forwardVector.multiply(this.forwardSpeed).add(strafeVector.multiply(this.sidewaysSpeed)).normalize();

                        this.dataTracker.set(MOVEMENT_TARGET_X, (float) (this.getX() + offset.x));
                        this.dataTracker.set(MOVEMENT_TARGET_Y, (float) (this.getY() + this.getHeight() * 0.5f + offset.y));
                        this.dataTracker.set(MOVEMENT_TARGET_Z, (float) (this.getZ() + offset.z));
                    } else {
                        this.dataTracker.set(MOVEMENT_TARGET_X, (float) this.getMoveControl().getTargetX());
                        this.dataTracker.set(MOVEMENT_TARGET_Y, (float) this.getMoveControl().getTargetY());
                        this.dataTracker.set(MOVEMENT_TARGET_Z, (float) this.getMoveControl().getTargetZ());
                    }

                    Path path = this.getNavigation().getCurrentPath();
                    if(path != null) {
                        int i = 0;

                        for(TrackedData<Optional<BlockPos>> pathingTarget : PATHING_TARGETS) {
                            TrackedData<Direction> pathingSide = PATHING_SIDES.get(i);

                            if(path.getCurrentNodeIndex() + i < path.getLength()) {
                                PathNode point = path.getNode(path.getCurrentNodeIndex() + i);

                                this.dataTracker.set(pathingTarget, Optional.of(new BlockPos(point.x, point.y, point.z)));

                                if(point instanceof DirectionalPathNode) {
                                    Direction dir = ((DirectionalPathNode) point).getPathSide();

                                    if(dir != null) {
                                        this.dataTracker.set(pathingSide, dir);
                                    } else {
                                        this.dataTracker.set(pathingSide, Direction.DOWN);
                                    }
                                }

                            } else {
                                this.dataTracker.set(pathingTarget, Optional.empty());
                                this.dataTracker.set(pathingSide, Direction.DOWN);
                            }

                            i++;
                        }
                    } else {
                        for(TrackedData<Optional<BlockPos>> pathingTarget : PATHING_TARGETS) {
                            this.dataTracker.set(pathingTarget, Optional.empty());
                        }

                        for(TrackedData<Direction> pathingSide : PATHING_SIDES) {
                            this.dataTracker.set(pathingSide, Direction.DOWN);
                        }
                    }
                }
            }
        super.tick();
    }

    public void onTick() {

    }

    @Override
    public void mobTick() {
        this.updateWalkingSide();
    }

    @Override
    public boolean isHoldingOntoLadder() {
        return false;
    }

    @Override
    @Nullable
    public Vec3d getTrackedMovementTarget() {
        if(this.shouldTrackPathingTargets()) {
            return new Vec3d(this.dataTracker.get(MOVEMENT_TARGET_X), this.dataTracker.get(MOVEMENT_TARGET_Y), this.dataTracker.get(MOVEMENT_TARGET_Z));
        }

        return null;
    }

    @Override
    public @Nullable List<PathingTarget> getTrackedPathingTargets() {
        if(this.shouldTrackPathingTargets()) {
            List<PathingTarget> pathingTargets = new ArrayList<>(PATHING_TARGETS.size());

            int i = 0;
            for(TrackedData<Optional<BlockPos>> key : PATHING_TARGETS) {
                BlockPos pos = this.dataTracker.get(key).orElse(null);

                if(pos != null) {
                    pathingTargets.add(new PathingTarget(pos, this.dataTracker.get(PATHING_SIDES.get(i))));
                }

                i++;
            }

            return pathingTargets;
        }

        return null;
    }

    @Override
    public boolean shouldTrackPathingTargets() {
        return true;
    }

    @Override
    public float getVerticalOffset(float partialTicks) {
        return 0.4f;
    }

    private void forEachClimbableCollisonBox(Box aabb, VoxelShapes.BoxConsumer action) {
        CollisionView cachedCollisionReader = new CachedCollisionView(this.world, aabb);

        Stream<VoxelShape> shapes = StreamSupport.stream(cachedCollisionReader.getEntityCollisions(this, aabb).stream().spliterator(), false);

        shapes.forEach(shape ->shape.forEachBox(action));

    }

    private List<Box> getClimbableCollisionBoxes(Box aabb) {
        List<Box> boxes = new ArrayList<>();
        this.forEachClimbableCollisonBox(aabb, (minX, minY, minZ, maxX, maxY, maxZ) -> boxes.add(new Box(minX, minY, minZ, maxX, maxY, maxZ)));
        return boxes;
    }



    @Override
    public float getBlockSlipperiness(BlockPos pos) {
        BlockState offsetState = this.world.getBlockState(pos);
        return offsetState.getBlock().getSlipperiness() * 0.91f;
    }

    private void updateOffsetsAndOrientation() {
        Vec3d direction = this.getOrientation().getGlobal(this.bodyYaw, this.getPitch());

        boolean isAttached = false;

        double baseStickingOffsetX = 0.0f;
        double baseStickingOffsetY = this.getVerticalOffset(1);
        double baseStickingOffsetZ = 0.0f;
        Vec3d baseOrientationNormal = new Vec3d(0, 1, 0);

        if(!this.isClimbingDisabled && this.onGround && this.getVehicle() == null) {
            Vec3d p = this.getPos();

            Vec3d s = p.add(0, this.getHeight() * 0.5f, 0);
            Vec3d pp = s;
            Vec3d pn = this.attachmentNormal.multiply(-1);

            //Give nudge towards ground direction so that the climber doesn't
            //get stuck in an incorrect orientation
            if(this.groundDirection != null) {
                double groundDirectionBlend = 0.25D;
                Vec3d scaledGroundDirection = this.groundDirection.getRight().multiply(groundDirectionBlend);
                pp = pp.add(scaledGroundDirection.multiply(-1));
                pn = pn.multiply(1.0D - groundDirectionBlend).add(scaledGroundDirection);
            }

            Box inclusionBox = new Box(s.x, s.y, s.z, s.x, s.y, s.z).expand(this.collisionsInclusionRange);

            Pair<Vec3d, Vec3d> attachmentPoint = CollisionSmoothingUtil.findClosestPoint(consumer -> this.forEachClimbableCollisonBox(inclusionBox, consumer), pp, pn, this.collisionsSmoothingRange, 0.5f, 1.0f, 0.001f, 20, 0.05f, s);

            Box entityBox = this.getBoundingBox();

            if(attachmentPoint != null) {
                Vec3d attachmentPos = attachmentPoint.getLeft();

                double dx = Math.max(entityBox.minX - attachmentPos.x, attachmentPos.x - entityBox.maxX);
                double dy = Math.max(entityBox.minY - attachmentPos.y, attachmentPos.y - entityBox.maxY);
                double dz = Math.max(entityBox.minZ - attachmentPos.z, attachmentPos.z - entityBox.maxZ);

                if(Math.max(dx, Math.max(dy, dz)) < 0.5f) {
                    isAttached = true;

                    this.lastAttachmentOffsetX = MathHelper.clamp(attachmentPos.x - p.x, -this.getWidth() / 2, this.getWidth() / 2);
                    this.lastAttachmentOffsetY = MathHelper.clamp(attachmentPos.y - p.y, 0, this.getHeight());
                    this.lastAttachmentOffsetZ = MathHelper.clamp(attachmentPos.z - p.z, -this.getWidth() / 2, this.getWidth() / 2);
                    this.lastAttachmentOrientationNormal = attachmentPoint.getRight();
                }
            }
        }

        this.prevAttachmentOffsetX = this.attachmentOffsetX;
        this.prevAttachmentOffsetY = this.attachmentOffsetY;
        this.prevAttachmentOffsetZ = this.attachmentOffsetZ;
        this.prevAttachmentNormal = this.attachmentNormal;

        float attachmentBlend = this.attachedTicks * 0.2f;

        this.attachmentOffsetX = baseStickingOffsetX + (this.lastAttachmentOffsetX - baseStickingOffsetX) * attachmentBlend;
        this.attachmentOffsetY = baseStickingOffsetY + (this.lastAttachmentOffsetY - baseStickingOffsetY) * attachmentBlend;
        this.attachmentOffsetZ = baseStickingOffsetZ + (this.lastAttachmentOffsetZ - baseStickingOffsetZ) * attachmentBlend;
        this.attachmentNormal = baseOrientationNormal.add(this.lastAttachmentOrientationNormal.subtract(baseOrientationNormal).multiply(attachmentBlend)).normalize();

        if(!isAttached) {
            this.attachedTicks = Math.max(0, this.attachedTicks - 1);
        } else {
            this.attachedTicks = Math.min(5, this.attachedTicks + 1);
        }

        this.orientation = this.calculateOrientation(1);

        net.minecraft.util.Pair<Float, Float> newEulerAngle = this.getOrientation().getLocalRotation(direction);

        float yawDelta = newEulerAngle.getLeft() - this.bodyYaw;
        float pitchDelta = newEulerAngle.getRight() - this.getPitch();

        this.prevOrientationYawDelta = this.orientationYawDelta;
        this.orientationYawDelta = yawDelta;

        this.bodyYaw = MathHelper.wrapDegrees(this.bodyYaw + yawDelta);
        this.prevYaw = this.wrapAngleInRange(this.prevYaw/* + yawDelta*/, this.bodyYaw);
        this.serverYaw = MathHelper.wrapDegrees(this.serverYaw + yawDelta);

        this.orientationYawDelta = MathHelper.wrapDegrees(this.orientationYawDelta  + yawDelta);
        this.prevOrientationYawDelta = this.wrapAngleInRange(this.prevOrientationYawDelta/* + yawDelta*/, this.orientationYawDelta);

        this.headYaw = MathHelper.wrapDegrees(this.headYaw + yawDelta);
        this.prevHeadYaw = this.wrapAngleInRange(this.prevHeadYaw/* + yawDelta*/, this.headYaw);
        this.serverHeadYaw= MathHelper.wrapDegrees(this.serverHeadYaw + yawDelta);

        this.setPitch(MathHelper.wrapDegrees(this.getPitch() + pitchDelta));
        this.prevPitch = this.wrapAngleInRange(this.prevPitch/* + pitchDelta*/, this.getPitch());
        this.serverPitch = MathHelper.wrapDegrees(this.serverPitch + pitchDelta);
    }

    private float wrapAngleInRange(float angle, float target) {
        while(target - angle < -180.0F) {
            angle -= 360.0F;
        }

        while(target - angle >= 180.0F) {
            angle += 360.0F;
        }

        return angle;
    }

    @Override
    public Orientation calculateOrientation(float partialTicks) {
        Vec3d attachmentNormal = this.prevAttachmentNormal.add(this.attachmentNormal.subtract(this.prevAttachmentNormal).multiply(partialTicks));

        Vec3d localZ = new Vec3d(0, 0, 1);
        Vec3d localY = new Vec3d(0, 1, 0);
        Vec3d localX = new Vec3d(1, 0, 0);

        float componentZ = (float) localZ.dotProduct(attachmentNormal);
        float componentY;
        float componentX = (float) localX.dotProduct(attachmentNormal);

        float yaw = (float) Math.toDegrees(MathHelper.atan2(componentX, componentZ));

        localZ = new Vec3d(Math.sin(Math.toRadians(yaw)), 0, Math.cos(Math.toRadians(yaw)));
        localY = new Vec3d(0, 1, 0);
        localX = new Vec3d(Math.sin(Math.toRadians(yaw - 90)), 0, Math.cos(Math.toRadians(yaw - 90)));

        componentZ = (float) localZ.dotProduct(attachmentNormal);
        componentY = (float) localY.dotProduct(attachmentNormal);
        componentX = (float) localX.dotProduct(attachmentNormal);

        float pitch = (float) Math.toDegrees(MathHelper.atan2(MathHelper.sqrt(componentX * componentX + componentZ * componentZ), componentY));

        Matrix4f m = new Matrix4f();

        m.multiply(new Matrix4f(new Quaternion((float) Math.toRadians(yaw), 0, 1, 0)));
        m.multiply(new Matrix4f(new Quaternion((float) Math.toRadians(pitch), 1, 0, 0)));
        m.multiply(new Matrix4f(new Quaternion((float) Math.toRadians((float) Math.signum(0.5f - componentY - componentZ - componentX) * yaw), 0, 1, 0)));
        float a = 0;
        float b = 0;
        float c = -1;
        float d = 0;
        float e = 1;
        float f = 0;
        float g = 1;
        float h = 0;
        float i = 0;
        m.multiplyByTranslation(a, b, c);
        m.multiplyByTranslation(d, e, f);
        m.multiplyByTranslation(g, h, i);
        localZ= new Vec3d(a, b, c);
        localY = new Vec3d(d, e, f);
        localX = new Vec3d(g, h, i);

        return new Orientation(attachmentNormal, localZ, localY, localX, componentZ, componentY, componentX, yaw, pitch);
    }

    //@Override
    public float getServerYaw(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
        return (float) this.serverYaw;
    }

    //@Override
    public float getTargetPitch(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
        return (float) this.serverPitch;
    }

    //@Override
    public float getTargetHeadYaw(float yaw, int rotationIncrements) {
        return (float) this.serverHeadYaw;
    }


    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        if(ROTATION_BODY.equals(data)) {
            EulerAngle rotation = this.dataTracker.get(ROTATION_BODY);
            Vec3d look = new Vec3d(rotation.getWrappedPitch(), rotation.getWrappedYaw(), rotation.getWrappedRoll());

            net.minecraft.util.Pair<Float, Float> eulerAngle = this.getOrientation().getLocalRotation(look);

            this.serverYaw = eulerAngle.getLeft();
            this.serverPitch = eulerAngle.getRight();
        } else if(ROTATION_HEAD.equals(data)) {
            EulerAngle rotation = this.dataTracker.get(ROTATION_HEAD);
            Vec3d look = new Vec3d(rotation.getWrappedPitch(), rotation.getWrappedYaw(), rotation.getWrappedRoll());

            Pair<Float, Float> eulerAngle = this.getOrientation().getLocalRotation(look);

            this.serverHeadYaw = eulerAngle.getLeft();
            this.headTrackingIncrements = 3;
        }
    }


    private double getGravity() {
        if(this.hasNoGravity()) {
            return 0;
        } else {
            return 0.32;
        }
    }

    private Vec3d getStickingForce(Pair<Direction, Vec3d> walkingSide) {
        double uprightness = Math.max(this.attachmentNormal.y, 0);
        double gravity = this.getGravity();
        double stickingForce = gravity * uprightness + 0.08D * (1 - uprightness);
        return walkingSide.getRight().multiply(stickingForce);
    }

    //@Override
    public void setJumpDirection(Vec3d dir) {
        this.jumpDir = dir != null ? dir.normalize() : null;
    }

    //@Override
    public boolean onJump() {
        if(this.jumpDir != null) {
            float jumpStrength = this.getJumpVelocity();
            if(this.hasStatusEffect(StatusEffects.JUMP_BOOST)) {
                jumpStrength += 0.1F * (float)(this.getStatusEffect(StatusEffects.JUMP_BOOST).getAmplifier() + 1);
            }

            Vec3d motion = this.getVelocity();

            Vec3d orthogonalMotion = this.jumpDir.multiply(this.jumpDir.dotProduct(motion));
            Vec3d tangentialMotion = motion.subtract(orthogonalMotion);

            this.setVelocity(tangentialMotion.x + this.jumpDir.x * jumpStrength, tangentialMotion.y + this.jumpDir.y * jumpStrength, tangentialMotion.z + this.jumpDir.z * jumpStrength);

            if(this.isSprinting()) {
                Vec3d boost = this.getOrientation().getGlobal(this.bodyYaw, 0).multiply(0.2f);
                this.setVelocity(this.getVelocity().add(boost));
            }

            this.onGround = false;
            //net.minecraftforge.common.ForgeHooks.onLivingJump(this);

            return true;
        }

        return false;
    }

    //@Override
    public boolean onTravel(Vec3d relative, boolean pre) {
        if(pre) {
            boolean canTravel = !this.getWorld().isClient || this.canBeControlledByRider();

            this.isClimbingDisabled = false;

            FluidState fluidState = this.world.getFluidState(this.getBlockPos());

            if(!this.canClimbInWater && this.isInsideWaterOrBubbleColumn() && this.isPushedByFluids() && !this.canWalkOnFluid(fluidState.getFluid())) {
                this.isClimbingDisabled = true;

                if(canTravel) {
                    return false;
                }
            } else if(!this.canClimbInLava && this.isInLava() && this.isPushedByFluids() && !this.canWalkOnFluid(fluidState.getFluid())) {
                this.isClimbingDisabled = true;

                if(canTravel) {
                    return false;
                }
            } else if(canTravel) {
                this.travelOnGround(relative);
            }

            if(!canTravel) {
                this.updateLimbs(this, true);
            }

            this.updateOffsetsAndOrientation();
            return true;
        } else {
            this.updateOffsetsAndOrientation();
            return false;
        }
    }

    private float getRelevantMoveFactor(float slipperiness) {
        return this.onGround ? this.getMovementSpeed() * (0.16277136F / (slipperiness * slipperiness * slipperiness)) : this.getJumpVelocityMultiplier();
    }

    private void travelOnGround(Vec3d relative) {
        Orientation orientation = this.getOrientation();

        Vec3d forwardVector = orientation.getGlobal(this.bodyYaw, 0);
        Vec3d strafeVector = orientation.getGlobal(this.bodyYaw - 90.0f, 0);
        Vec3d upVector = orientation.getGlobal(this.bodyYaw, -90.0f);

        Pair<Direction, Vec3d> groundDirection = this.getGroundDirection();

        Vec3d stickingForce = this.getStickingForce(groundDirection);

        boolean isFalling = this.getVelocity().y <= 0.0D;

        if(isFalling && this.hasStatusEffect(StatusEffects.SLOW_FALLING)) {
            this.fallDistance = 0;
        }

        float forward = (float) relative.z;
        float strafe = (float) relative.x;

        if(forward != 0 || strafe != 0) {
            float slipperiness = 0.91f;

            if(this.onGround) {
                BlockPos offsetPos = new BlockPos(this.getPos()).offset(groundDirection.getLeft());
                slipperiness = this.getBlockSlipperiness(offsetPos);
            }

            float f = forward * forward + strafe * strafe;
            if(f >= 1.0E-4F) {
                f = Math.max(MathHelper.sqrt(f), 1.0f);
                f = this.getRelevantMoveFactor(slipperiness) / f;
                forward *= f;
                strafe *= f;

                Vec3d movementOffset = new Vec3d(forwardVector.x * forward + strafeVector.x * strafe, forwardVector.y * forward + strafeVector.y * strafe, forwardVector.z * forward + strafeVector.z * strafe);

                double px = this.getX();
                double py = this.getY();
                double pz = this.getZ();
                Vec3d motion = this.getVelocity();
                Box aabb = this.getBoundingBox();

                //Probe actual movement vector
                this.move(MovementType.SELF, movementOffset);

                Vec3d movementDir = new Vec3d(this.getX() - px, this.getY() - py, this.getZ() - pz).normalize();

                this.setBoundingBox(aabb);
                this.calculateBoundingBox();
                this.setVelocity(motion);

                //Probe collision normal
                Vec3d probeVector = new Vec3d(Math.abs(movementDir.x) < 0.001D ? -Math.signum(upVector.x) : 0, Math.abs(movementDir.y) < 0.001D ? -Math.signum(upVector.y) : 0, Math.abs(movementDir.z) < 0.001D ? -Math.signum(upVector.z) : 0).normalize().multiply(0.0001D);
                this.move(MovementType.SELF, probeVector);

                Vec3d collisionNormal = new Vec3d(Math.abs(this.getX() - px - probeVector.x) > 0.000001D ? Math.signum(-probeVector.x) : 0, Math.abs(this.getY() - py - probeVector.y) > 0.000001D ? Math.signum(-probeVector.y) : 0, Math.abs(this.getZ() - pz - probeVector.z) > 0.000001D ? Math.signum(-probeVector.z) : 0).normalize();

                this.setBoundingBox(aabb);
                this.calculateBoundingBox();
                this.setVelocity(motion);

                //Movement vector projected to surface
                Vec3d surfaceMovementDir = movementDir.subtract(collisionNormal.multiply(collisionNormal.dotProduct(movementDir))).normalize();

                boolean isInnerCorner = Math.abs(collisionNormal.x) + Math.abs(collisionNormal.y) + Math.abs(collisionNormal.z) > 1.0001f;

                //Only project movement vector to surface if not moving across inner corner, otherwise it'd get stuck in the corner
                if(!isInnerCorner) {
                    movementDir = surfaceMovementDir;
                }

                //Nullify sticking force along movement vector projected to surface
                stickingForce = stickingForce.subtract(surfaceMovementDir.multiply(surfaceMovementDir.normalize().dotProduct(stickingForce)));

                float moveSpeed = MathHelper.sqrt(forward * forward + strafe * strafe);
                this.setVelocity(this.getVelocity().add(movementDir.multiply(moveSpeed)));
            }
        }

        this.setVelocity(this.getVelocity().add(stickingForce));

        double px = this.getX();
        double py = this.getY();
        double pz = this.getZ();
        Vec3d motion = this.getVelocity();

        this.move(MovementType.SELF, motion);

        this.prevAttachedSides = this.attachedSides;
        this.attachedSides = new Vec3d(Math.abs(this.getX() - px - motion.x) > 0.001D ? -Math.signum(motion.x) : 0, Math.abs(this.getY() - py - motion.y) > 0.001D ? -Math.signum(motion.y) : 0, Math.abs(this.getZ() - pz - motion.z) > 0.001D ? -Math.signum(motion.z) : 0);

        float slipperiness = 0.91f;

        if(this.onGround) {
            this.fallDistance = 0;

            BlockPos offsetPos = new BlockPos(this.getPos()).offset(groundDirection.getLeft());
            slipperiness = this.getBlockSlipperiness(offsetPos);
        }

        motion = this.getVelocity();
        Vec3d orthogonalMotion = upVector.multiply(upVector.dotProduct(motion));
        Vec3d tangentialMotion = motion.subtract(orthogonalMotion);

        this.setVelocity(tangentialMotion.x * slipperiness + orthogonalMotion.x * 0.98f, tangentialMotion.y * slipperiness + orthogonalMotion.y * 0.98f, tangentialMotion.z * slipperiness + orthogonalMotion.z * 0.98f);

        boolean detachedX = this.attachedSides.x != this.prevAttachedSides.x && Math.abs(this.attachedSides.x) < 0.001D;
        boolean detachedY = this.attachedSides.y != this.prevAttachedSides.y && Math.abs(this.attachedSides.y) < 0.001D;
        boolean detachedZ = this.attachedSides.z != this.prevAttachedSides.z && Math.abs(this.attachedSides.z) < 0.001D;

        if(detachedX || detachedY || detachedZ) {
            float stepHeight = this.stepHeight;
            this.stepHeight = 0;

            boolean prevOnGround = this.onGround;
            boolean prevCollidedHorizontally = this.horizontalCollision;
            boolean prevCollidedVertically = this.verticalCollision;

            //Offset so that Box is moved above the new surface
            this.move(MovementType.SELF, new Vec3d(detachedX ? -this.prevAttachedSides.x * 0.25f : 0, detachedY ? -this.prevAttachedSides.y * 0.25f : 0, detachedZ ? -this.prevAttachedSides.z * 0.25f : 0));

            Vec3d axis = this.prevAttachedSides.normalize();
            Vec3d attachVector = upVector.multiply(-1);
            attachVector = attachVector.subtract(axis.multiply(axis.dotProduct(attachVector)));

            if(Math.abs(attachVector.x) > Math.abs(attachVector.y) && Math.abs(attachVector.x) > Math.abs(attachVector.z)) {
                attachVector = new Vec3d(Math.signum(attachVector.x), 0, 0);
            } else if(Math.abs(attachVector.y) > Math.abs(attachVector.z)) {
                attachVector = new Vec3d(0, Math.signum(attachVector.y), 0);
            } else {
                attachVector = new Vec3d(0, 0, Math.signum(attachVector.z));
            }

            double attachDst = motion.length() + 0.1f;

            Box aabb = this.getBoundingBox();
            motion = this.getVelocity();

            //Offset AABB towards new surface until it touches
            for(int i = 0; i < 2 && !this.onGround; i++) {
                this.move(MovementType.SELF, attachVector.multiply(attachDst));
            }

            this.stepHeight = stepHeight;

            //Attaching failed, fall back to previous position
            if(!this.onGround) {
                this.setBoundingBox(aabb);
                this.calculateBoundingBox();
                this.setVelocity(motion);
                this.onGround = prevOnGround;
                this.horizontalCollision = prevCollidedHorizontally;
                this.verticalCollision = prevCollidedVertically;
            } else {
                this.setVelocity(Vec3d.ZERO);
            }
        }

        this.updateLimbs(this, true);
    }



    @Override
    public boolean onMove(MovementType type, Vec3d pos, boolean pre) {
        if(pre) {
            this.preWalkingPosition = this.getPos();
            this.preMoveY = this.getY();
        } else {
            if(Math.abs(this.getY() - this.preMoveY - pos.y) > 0.000001D) {
                this.setVelocity(this.getVelocity().multiply(1, 0, 1));
            }

            this.onGround |= this.horizontalCollision || this.verticalCollision;
        }

        return false;
    }




    @Override
    public BlockPos getAdjustedOnPosition(BlockPos onPosition) {
        float verticalOffset = this.getVerticalOffset(1);

        int x = MathHelper.floor(this.getX() + this.attachmentOffsetX - (float) this.attachmentNormal.x * (verticalOffset + 0.2f));
        int y = MathHelper.floor(this.getY() + this.attachmentOffsetY - (float) this.attachmentNormal.y * (verticalOffset + 0.2f));
        int z = MathHelper.floor(this.getZ() + this.attachmentOffsetZ - (float) this.attachmentNormal.z * (verticalOffset + 0.2f));
        BlockPos pos = new BlockPos(x, y, z);

        if(this.world.isAir(pos) && this.attachmentNormal.y < 0.0f) {
            BlockPos posDown = pos.down();
            BlockState stateDown = this.world.getBlockState(posDown);

            if(stateDown.isSideSolidFullSquare(this.world, posDown, Direction.DOWN)) {
                return posDown;
            }
        }

        return pos;
    }

    @Override
    public boolean getAdjustedCanTriggerWalking(boolean canTriggerWalking) {
        if(this.preWalkingPosition != null && this.canClimberTriggerWalking() && !this.hasVehicle()) {
            Vec3d moved = this.getPos().subtract(this.preWalkingPosition);
            this.preWalkingPosition = null;

            BlockPos pos = this.getLandingPos();
            BlockState state = this.world.getBlockState(pos);

            double dx = moved.x;
            double dy = moved.y;
            double dz = moved.z;

            Vec3d tangentialMovement = moved.subtract(this.attachmentNormal.multiply(this.attachmentNormal.dotProduct(moved)));

            this.distanceTraveled = (float) ((double) this.distanceTraveled + tangentialMovement.length() * 0.6D);

            this.nextStepDistance = (float) ((double) this.nextStepDistance + (double) MathHelper.sqrt((float) (dx * dx + dy * dy + dz * dz)) * 0.6D);

            if(this.nextStepDistance > this.nextStepDistance && !state.isAir()) {
                this.nextStepDistance = this.calculateNextStepSoundDistance();

                if(this.isInsideWaterOrBubbleColumn()) {
                    Entity controller = this.hasPassengers() && this.getPrimaryPassenger() != null ? this.getPrimaryPassenger() : this;

                    float multiplier = controller == this ? 0.35F : 0.4F;

                    Vec3d motion = controller.getVelocity();

                    float swimStrength = MathHelper.sqrt((float) (motion.x * motion.x * (double) 0.2F + motion.y * motion.y + motion.z * motion.z * 0.2F)) * multiplier;
                    if(swimStrength > 1.0F) {
                        swimStrength = 1.0F;
                    }

                    this.playSwimSound(swimStrength);
                } else {
                    this.playStepSound(pos, state);
                }
            } else if(this.nextStepDistance > this.nextFlap && state.isAir()) {
                this.nextFlap = this.calculateNextStepSoundDistance();
            }
        }

        return false;
    }

    @Override
    public boolean canClimberTriggerWalking() {
        return true;
    }
}
