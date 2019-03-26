package com.hotmail.intrinsic;

import org.bukkit.entity.Player;

public class IntrinsicPlayer {
    private Player base;
    private Region lastViewed;

    public IntrinsicPlayer(Player base) {
        this.base = base;
    }

    public Player getBase() {
        return this.base;
    }

    /**
     * Set the region the player has last viewed the dashboard of
     * @param region to set
     */
    public void setLastViewed(Region region) {
        this.lastViewed = region;
    }

    /**
     * Get the region the player has last opened/viewed the dashboard of
     * @return Region
     */
    public Region getLastViewed() {
        return lastViewed;
    }
}
