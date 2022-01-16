package net.arathain.tot.common.item;

import net.minecraft.item.ToolItem;
import net.minecraft.item.ToolMaterial;

public class SynthesisScepterItem extends ToolItem {
    public SynthesisScepterItem(ToolMaterial material, Settings settings) {
        super(material, settings);
    }


    public enum SpellType {
        RAYCAST,
        SUMMON,
        MODIFY
    }
}
