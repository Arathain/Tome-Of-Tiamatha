package net.arathain.tot.common.component;

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;

public class VIAlignmentComponent implements AutoSyncedComponent {
    private final PlayerEntity obj;
    private int alignment = 0;

    public VIAlignmentComponent(PlayerEntity obj) {
        this.obj = obj;
    }

    public void setAlignment(int alignment) {
        this.alignment = alignment;
    }

    public int getAlignment() {
        return alignment;
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        setAlignment(tag.getInt("alignment"));

    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putInt("alignment", this.getAlignment());

    }
}
