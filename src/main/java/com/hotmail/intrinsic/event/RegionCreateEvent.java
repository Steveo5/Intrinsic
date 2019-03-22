package com.hotmail.intrinsic.event;

import com.hotmail.intrinsic.Region;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RegionCreateEvent extends Event implements Cancellable {
    private boolean isCancelled = false;
    private static final HandlerList handlers = new HandlerList();
    private Region region;

    public RegionCreateEvent(Region region) {
        this.region = region;
    }

    public Region getRegion() {
        return region;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.isCancelled = b;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
