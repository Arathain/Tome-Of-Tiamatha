package net.arathain.tot.common.item;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.world.World;

public class SilksteelSwordItem extends SwordItem {
    public SilksteelSwordItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings) {
        super(toolMaterial, attackDamage, attackSpeed, settings);
    }
    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if(stack.isDamaged() && world.getRandom().nextInt(20) == 3) {
            stack.setDamage(stack.getDamage() - 4);
        }
        super.inventoryTick(stack, world, entity, slot, selected);
    }
}
