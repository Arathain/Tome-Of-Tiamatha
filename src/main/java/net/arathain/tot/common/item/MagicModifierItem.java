package net.arathain.tot.common.item;

public interface MagicModifierItem {
    default SynthesisScepterItem.SpellType getSpellType() {
        return null;
    }
}
