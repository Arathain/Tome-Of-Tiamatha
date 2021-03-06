package net.arathain.tot.common.item;

import net.minecraft.block.AnvilBlock;
import net.minecraft.enchantment.MendingEnchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public class SilksteelArmorItem extends ArmorItem {
    public SilksteelArmorItem(ArmorMaterial material, EquipmentSlot slot, Settings settings) {
        super(material, slot, settings);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if(stack.isDamaged() && world.getRandom().nextInt(20) == 3) {
            stack.setDamage(stack.getDamage() - 4);
        }
        super.inventoryTick(stack, world, entity, slot, selected);
    }
}
