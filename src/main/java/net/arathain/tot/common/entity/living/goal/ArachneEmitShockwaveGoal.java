package net.arathain.tot.common.entity.living.goal;

import net.arathain.tot.common.entity.living.drider.arachne.ArachneEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.entity.EntityHandler;

import java.util.EnumSet;
import java.util.List;

public class ArachneEmitShockwaveGoal extends Goal {
    private final ArachneEntity arachne;
    public ArachneEmitShockwaveGoal(ArachneEntity entity) {
        this.arachne = entity;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK, Control.JUMP));
    }
    @Override
    public boolean canStart() {
        return this.arachne.canSlam;
    }
    @Override
    public void tick() {
        arachne.setVelocity(0, arachne.getVelocity().y, 0);
        if(arachne.getTarget() != null) arachne.lookAtEntity(arachne.getTarget(), 45, 45);
        double perpFacing = arachne.bodyYaw * (Math.PI / 180);
        int hitY = MathHelper.floor(arachne.getBoundingBox().minY - 0.5);
        double facingAngle = perpFacing + Math.PI / 2;
        ServerWorld world = (ServerWorld) arachne.world;
        if(arachne.slamTicks >= 20) {
            if (arachne.slamTicks % 2 == 0) {
                int distance = arachne.slamTicks / 2 - 8;
                double spread = Math.PI * 2;
                int arcLen = MathHelper.ceil(distance * spread);
                double minY = arachne.getBoundingBox().minY;
                double maxY = arachne.getBoundingBox().maxY;
                for (int i = 0; i < arcLen; i++) {
                    double theta = (i / (arcLen - 1.0) - 0.5) * spread + facingAngle;
                    double vx = Math.cos(theta);
                    double vz = Math.sin(theta);
                    double px = arachne.getX() + vx * distance;
                    double pz = arachne.getZ() + vz * distance;
                    float factor = 1 - distance / (float) 6;
                    Box box = new Box(px - 1.5, minY, pz - 1.5, px + 1.5, maxY, pz + 1.5);
                    List<Entity> hit = world.getNonSpectatingEntities(Entity.class, box);
                    for (Entity entity : hit) {
                        if (entity.isOnGround()) {
                            if (entity == this.arachne || entity instanceof FallingBlockEntity || entity instanceof SpiderEntity) {
                                continue;
                            }
                            float applyKnockbackResistance = 0;
                            if (entity instanceof LivingEntity) {
                                entity.damage(DamageSource.mob(this.arachne), (factor * 5 + 1) * 2);
                                applyKnockbackResistance = (float) ((LivingEntity) entity).getAttributeValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE);
                            }
                            double magnitude = world.random.nextDouble() * 0.15 + 0.1;
                            float x = 0, y = 0, z = 0;
                            x += vx * factor * magnitude * (1 - applyKnockbackResistance);
                            y += 0.1 * (1 - applyKnockbackResistance) + factor * 0.15 * (1 - applyKnockbackResistance);
                            z += vz * factor * magnitude * (1 - applyKnockbackResistance);
                            entity.setVelocity(entity.getVelocity().add(x, y, z));
                            if (entity instanceof ServerPlayerEntity) {
                                ((ServerPlayerEntity) entity).networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(entity));
                            }
                        }
                    }
                    if (world.random.nextInt(5) < 4) {
                        int hitX = MathHelper.floor(px);
                        int hitZ = MathHelper.floor(pz);
                        BlockPos pos = new BlockPos(hitX, hitY, hitZ);
                        BlockPos abovePos = new BlockPos(pos).up();
                        BlockState state = world.getBlockState(pos);
                        BlockState stateAbove = world.getBlockState(abovePos);
                        if (state.getMaterial() != Material.AIR && state.isOpaqueFullCube(world, pos) && world.getBlockEntity(abovePos) == null && !stateAbove.getMaterial().blocksMovement()) {
                            FallingBlockEntity fallingBlock = new FallingBlockEntity(world, hitX + 0.5, hitY + 1, hitZ + 0.5, state);
                            fallingBlock.setPosition(hitX + 0.5, hitY + 1, hitZ + 0.5);
                            fallingBlock.setVelocity(0, 0.4 + factor * 0.2, 0);
                            world.spawnEntity(fallingBlock);
                        }
                    }
                }
                if (arachne.slamTicks > 32) {
                    this.arachne.slamTicks = 0;
                    this.arachne.getDataTracker().set(ArachneEntity.ATTACK_STATE, 0);
                    this.arachne.canSlam = false;
                }
            }
        }
        arachne.slamTicks++;
    }
}
