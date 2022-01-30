package net.arathain.tot.common.block.entity;

import net.arathain.tot.common.entity.living.drider.weavekin.WeavechildEntity;
import net.arathain.tot.common.init.ToTEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.List;

public class WeaverkinEggBlockEntity extends BlockEntity implements IAnimatable {
    private AnimationFactory factory = new AnimationFactory(this);
    public static final AnimationBuilder IDLE = new AnimationBuilder().addAnimation("idle");
    public static final AnimationBuilder HATCH = new AnimationBuilder().addAnimation("hatch");
    private static int breakTicker = 0;
    public WeaverkinEggBlockEntity(BlockPos pos, BlockState state) {
        super(ToTEntities.WEAVERKIN_EGG, pos, state);
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController<>(this, "controller", 2, animationEvent -> {
            AnimationBuilder anime = IDLE;
            if (breakTicker > 0) {
                anime = HATCH;
            }
            animationEvent.getController().setAnimation(anime);
            return PlayState.CONTINUE;
        }));

    }
    public static void tick(World tickerWorld, BlockPos pos, BlockState tickerState, WeaverkinEggBlockEntity blockEntity) {
        if (tickerWorld != null) {
            if(breakTicker >= 120) {
                WeavechildEntity weavekin = new WeavechildEntity(ToTEntities.WEAVECHILD, tickerWorld);
                weavekin.setPos(pos.getX() + 0.5f, pos.getY(), pos.getZ() + 0.5f);
                weavekin.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 1000, 1));
                tickerWorld.spawnEntity(weavekin);
                tickerWorld.breakBlock(pos, false);
            }
            List<AnimalEntity> animols = tickerWorld.getEntitiesByClass(AnimalEntity.class, new Box(pos).expand(5), LivingEntity::canTakeDamage);
            if(!animols.isEmpty()) {
                breakTicker++;
                if(breakTicker % 5 == 0) {
                    tickerWorld.addBlockBreakParticles(pos, tickerState);
                }
            } else {
                breakTicker = 0;
            }
        }
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }
}
