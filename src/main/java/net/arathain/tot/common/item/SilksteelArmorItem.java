package net.arathain.tot.common.item;

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
        if(stack.isDamaged() && world.getRandom().nextInt(20) == 3 && entity instanceof ServerPlayerEntity player) {
            stack.damage(-10, world.getRandom(), player);
        }
        super.inventoryTick(stack, world, entity, slot, selected);
    }
}
