package net.arathain.tot.common.item;

import com.github.crimsondawn45.fabricshieldlib.lib.object.FabricShieldItem;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public class SilksteelShieldItem extends FabricShieldItem {
    public SilksteelShieldItem(Settings settings, int cooldownTicks, int enchantability, Item... repairItems) {
        super(settings, cooldownTicks, enchantability, repairItems);
    }
    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if(stack.isDamaged() && world.getRandom().nextInt(20) == 3) {
            stack.setDamage(stack.getDamage() - 4);
        }
        super.inventoryTick(stack, world, entity, slot, selected);
    }
}
