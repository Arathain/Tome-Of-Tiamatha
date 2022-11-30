package net.arathain.tot.common.entity.string;

import com.github.legoatoom.connectiblechains.tag.CommonTags;
import net.arathain.tot.common.util.ToTUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.ToolItem;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.random.RandomGenerator;

import java.util.Random;

/**
 * ChainLinkEntity implements common functionality between {@link StringCollisionEntity} and {@link StringKnotEntity}.
 */
public interface StringLinkEntity {

    /**
     * When a chain link entity is damaged by
     * <ul>
     * <li>A player with a item that has a tool in his hand or is a drider</li>
     * <li>An explosion</li>
     * </ul>
     * it destroys the link that it is part of.
     * Otherwise, it plays a hit sound.
     *
     * @param self   A {@link StringCollisionEntity} or {@link StringKnotEntity}.
     * @param source The source that was used to damage.
     * @return {@link ActionResult#SUCCESS} when the link should be destroyed,
     * {@link ActionResult#CONSUME} when the link should be destroyed but not drop.
     */
    static ActionResult onDamageFrom(Entity self, DamageSource source) {
        if (self.isInvulnerableTo(source)) {
            return ActionResult.FAIL;
        }
        if (self.world.isClient) {
            return ActionResult.PASS;
        }

        if (source.isExplosive()) {
            return ActionResult.SUCCESS;
        }
        if (source.getSource() instanceof PlayerEntity player) {
            if (canDestroyWith(player.getMainHandStack().getItem(), player.getRandom()) || ToTUtil.isDrider(player)) {
                return ActionResult.success(!player.isCreative());
            }
        }

        if (!source.isProjectile()) {
            // Projectiles such as arrows (actually probably just arrows) can get "stuck"
            // on entities they cannot damage, such as players while blocking with shields or these chains.
            // That would cause some serious sound spam, and we want to avoid that.
            self.playSound(SoundEvents.BLOCK_WOOL_HIT, 0.5F, 1.0F);
        }
        return ActionResult.FAIL;
    }

    /**
     * @param item The item subject of an interaction
     * @return true if a chain link entity can be destroyed with the item
     */
    static boolean canDestroyWith(Item item, RandomGenerator random) {
        return item.getDefaultStack().isIn(CommonTags.SHEARS) || item instanceof ToolItem tool && random.nextInt(20 - MathHelper.ceil(tool.getMaterial().getAttackDamage())) == 2;
    }

    /**
     * Destroys all links associated with this entity
     *
     * @param mayDrop true when the links should drop
     */
    void destroyLinks(boolean mayDrop);
}
