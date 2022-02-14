package net.arathain.tot.common.util;

import net.arathain.tot.common.component.DriderPlayerComponent;
import net.arathain.tot.common.entity.living.drider.DriderEntity;
import net.arathain.tot.common.init.ToTComponents;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.function.Predicate;

public class ToTUtil {
    public static boolean isLookingAt(LivingEntity entity, LivingEntity target) {
        Vec3d vec3 = entity.getRotationVec(1.0F).normalize();
        Vec3d vec31 = new Vec3d(target.getX() - entity.getX(), target.getEyeY() - entity.getEyeY(), target.getZ() - entity.getZ());
        double lenkth = vec31.length();
        vec31 = vec31.normalize();
        double dotProduct = vec3.dotProduct(vec31);

        double range = 6.5D;

        return dotProduct > 1.0D - range / lenkth && canSee(entity, target);

    }
    public static boolean canSee(LivingEntity entity, Entity target) {
        if (target.world != entity.world) {
            return false;
        } else {
            Vec3d vec3 = new Vec3d(entity.getX(), entity.getEyeY(), entity.getZ());
            Vec3d vec31 = new Vec3d(target.getX(), target.getEyeY(), target.getZ());

            return entity.world.raycast(new RaycastContext(vec3, vec31, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity)).getType() == HitResult.Type.MISS;
        }
    }
    public static boolean isDrider(Entity entity) {
        if (entity instanceof PlayerEntity player) {
            DriderPlayerComponent transformationComponent = ToTComponents.DRIDER_COMPONENT.get(player);
            if (transformationComponent.isDrider()) {
                return true;
            }
        }
        return entity instanceof DriderEntity;
    }
    public static EntityHitResult hitscanEntity(World world, LivingEntity user, double distance, Predicate<Entity> targetPredicate){
        Vec3d vec3d = user.getCameraPosVec(1);
        Vec3d vec3d2 = user.getRotationVec(1);
        Vec3d vec3d3 = vec3d.add(vec3d2.x * distance, vec3d2.y * distance, vec3d2.z * distance);
        double squareDistance = Math.pow(distance, 2);
        return ProjectileUtil.getEntityCollision(world, user, vec3d, vec3d3, user.getBoundingBox().stretch(vec3d2.multiply(squareDistance)).expand(1.0D, 1.0D, 1.0D), targetPredicate);
    }
    public static BlockHitResult hitscanBlock(World world, LivingEntity user, double distance, RaycastContext.FluidHandling fluidHandling, Predicate<Block> targetPredicate){
        Vec3d vec3d = user.getCameraPosVec(1);
        Vec3d vec3d2 = user.getRotationVec(1);
        Vec3d vec3d3 = vec3d.add(vec3d2.x * distance, vec3d2.y * distance, vec3d2.z * distance);
        double squareDistance = Math.pow(distance, 2);
        vec3d3.multiply(squareDistance);
        return world.raycast(new RaycastContext(vec3d, vec3d3, RaycastContext.ShapeType.OUTLINE, fluidHandling, user));
    }
}
