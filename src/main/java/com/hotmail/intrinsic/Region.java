package com.hotmail.intrinsic;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

public class Region {
    private RegionType type;
    private Location loc;
    private OfflinePlayer owner;

    protected Region(RegionType type, Location loc, OfflinePlayer owner) {
        this.type = type;
        this.loc = loc;
        this.owner = owner;
    }

    public RegionType getType() {
        return this.type;
    }

    public Location getLocation() {
        return this.loc;
    }

    public OfflinePlayer getOwner() {
        return this.owner;
    }

    public String getDisplayName() {
        return type.getName().replace("-", " ");
    }
}
