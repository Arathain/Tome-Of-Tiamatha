package net.arathain.tot.common.entity.string;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;

/**
 * Due to the asynchronous nature of networking an attach- or detach-packet cann arrive before the secondary exists.
 * This class acts as a temporary storage until the real link can be created.
 *
 * @author Qendolin
 */
@Environment(EnvType.CLIENT)
public class IncompleteStringLink {
    /**
     * @see StringLink#primary
     */
    public final StringKnotEntity primary;
    /**
     * @see StringLink#secondary
     */
    public final int secondaryId;
    /**
     * Whether the link exists and is active
     */
    private boolean alive = true;

    public IncompleteStringLink(StringKnotEntity primary, int secondaryId) {
        this.primary = primary;
        this.secondaryId = secondaryId;
    }

    /**
     * Tries to complete the chain link.
     *
     * @return true if the incomplete chain link should be removed
     */
    public boolean tryCompleteOrRemove() {
        if (isDead()) return true;
        Entity secondary = primary.world.getEntityById(secondaryId);
        if (secondary == null) return false;
        StringLink.create(primary, secondary);
        return true;
    }

    public boolean isDead() {
        return !alive || this.primary.isRemoved();
    }

    /**
     * Sometimes the detach-packed can be received before the secondary exists
     * so even incomplete links can be destroyed.
     */
    public void destroy() {
        if (!alive) return;
        this.alive = false;
        // Can't drop items on the client I guess - Qendolin
    }
}
