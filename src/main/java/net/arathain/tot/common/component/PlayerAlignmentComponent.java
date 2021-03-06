package net.arathain.tot.common.component;

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.MathHelper;

public class PlayerAlignmentComponent implements AutoSyncedComponent {
    private final PlayerEntity obj;
    private int villagerAlignment = 0;
    private int ravenAlignment = 0;
    private int illagerAlignment = -50;

    public PlayerAlignmentComponent(PlayerEntity obj) {
        this.obj = obj;
    }

    public void setVAlignment(int alignment) {
        this.villagerAlignment = MathHelper.clamp(alignment, -200, 200);
    }

    public int getVAlignment() {
        return villagerAlignment;
    }

    public void setIAlignment(int alignment) {
        this.illagerAlignment = MathHelper.clamp(alignment, -200, 200);
    }

    public int getIAlignment() {
        return illagerAlignment;
    }

    public void setRAlignment(int alignment) {
        this.ravenAlignment = MathHelper.clamp(alignment, -200, 200);
    }

    public int getRAlignment() {
        return ravenAlignment;
    }

    public void incrementVAlignment(int increment) {
        setVAlignment(this.villagerAlignment + increment);
    }
    public void incrementIAlignment(int increment) {
        setIAlignment(this.illagerAlignment + increment);
    }
    public void incrementRAlignment(int increment) {
        setRAlignment(this.ravenAlignment + increment);
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        setVAlignment(tag.getInt("valignment"));
        setIAlignment(tag.getInt("ialignment"));
        setRAlignment(tag.getInt("ralignment"));

    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putInt("valignment", this.getVAlignment());
        tag.putInt("ialignment", this.getIAlignment());
        tag.putInt("ralignment", this.getRAlignment());

    }
}
