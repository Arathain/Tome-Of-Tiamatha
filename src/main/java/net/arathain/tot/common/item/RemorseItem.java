package net.arathain.tot.common.item;

import net.arathain.tot.common.init.ToTToolMaterials;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;

public class RemorseItem extends SwordItem {
    public RemorseItem(int attackDamage, float attackSpeed, Settings settings) {
        super(ToTToolMaterials.BONE, attackDamage, attackSpeed, settings);
    }

}
