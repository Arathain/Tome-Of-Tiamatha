package net.arathain.tot.common.entity.living.drider.arachne;

import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ToTBossBar extends ServerBossBar {
    private final MobEntity obj;
    private final Set<ServerPlayerEntity> invis = new HashSet<>();
    public ToTBossBar(MobEntity entity, Color color) {
        super(entity.getDisplayName(), color, BossBar.Style.PROGRESS);
        obj = entity;
    }

    public void update() {
        this.setPercent(this.obj.getHealth() / this.obj.getMaxHealth());
        Iterator<ServerPlayerEntity> itater = this.invis.iterator();
        while (itater.hasNext()) {
            ServerPlayerEntity player = itater.next();
            if (this.obj.getVisibilityCache().canSee(player)) {
                super.addPlayer(player);
                itater.remove();
            }
        }
    }

    @Override
    public void addPlayer(ServerPlayerEntity player) {
        if(this.obj.getVisibilityCache().canSee(player)) {
            super.addPlayer(player);
        } else {
            this.invis.add(player);
        }
    }

    @Override
    public void removePlayer(ServerPlayerEntity player) {
        super.removePlayer(player);
        this.invis.remove(player);
    }
}
