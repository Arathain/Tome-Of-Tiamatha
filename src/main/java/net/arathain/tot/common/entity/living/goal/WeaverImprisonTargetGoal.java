package net.arathain.tot.common.entity.living.goal;

import net.arathain.tot.common.entity.living.drider.weaver.WeaverEntity;
import net.arathain.tot.common.entity.living.drider.weaver.WebbingEntity;
import net.arathain.tot.common.init.ToTComponents;
import net.arathain.tot.common.init.ToTEffects;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.world.World;

public class WeaverImprisonTargetGoal extends Goal {
    private final WeaverEntity obj;
    public WeaverImprisonTargetGoal(WeaverEntity entity) {
            this.obj = entity;
    }

    @Override
    public boolean canStart() {
        return obj.getTarget() != null && obj.getTarget().hasStatusEffect(ToTEffects.BROODS_CURSE) && obj.getTarget().getStatusEffect(ToTEffects.BROODS_CURSE).getDuration() < 200 && ToTComponents.DRIDER_COMPONENT.get(obj.getTarget()).getStage() == 0;
    }

    @Override
    public void tick() {
        LivingEntity target = obj.getTarget();
        obj.lookAtEntity(target, 90f, 90f);
        World world = obj.getWorld();
        if(target != null) {
            WebbingEntity web = new WebbingEntity(world, target.getX(), target.getY(), target.getZ());
            world.spawnEntity(web);
            target.startRiding(web, true);
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 3600, 2), obj);
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 3600, 2), obj);
            for (Entity entity : world.getOtherEntities(obj, obj.getBoundingBox().expand(32), entity -> entity instanceof HostileEntity hostile && hostile.getTarget() == target)) {
                ((HostileEntity) entity).setTarget(null);
            }
        }
    }
}
