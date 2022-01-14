package net.arathain.tot.common.component;

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;

public class VIAlignmentComponent implements AutoSyncedComponent {
    private final PlayerEntity obj;
    private int villagerAlignment = 0;
    private int illagerAlignment = -50;

    public VIAlignmentComponent(PlayerEntity obj) {
        this.obj = obj;
    }

    public void setVAlignment(int alignment) {
        this.villagerAlignment = alignment;
    }

    public int getVAlignment() {
        return villagerAlignment;
    }

    public void setIAlignment(int alignment) {
        this.illagerAlignment = alignment;
    }

    public int getIAlignment() {
        return illagerAlignment;
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        setVAlignment(tag.getInt("valignment"));
        setIAlignment(tag.getInt("ialignment"));

    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putInt("valignment", this.getVAlignment());
        tag.putInt("ialignment", this.getIAlignment());

    }
}
