package com.hotmail.intrinsic;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

public class Region {
    private RegionType type;
    private Location loc;
    private OfflinePlayer owner;
    private Location[] bounds = new Location[2];

    public Region(RegionType type, Location loc, OfflinePlayer owner) {
        this.type = type;
        this.loc = loc;
        this.owner = owner;

        int r = type.getRadius();

        bounds[0] = loc.clone().subtract(r, r, r);
        bounds[1] = loc.clone().add(r, r, r);
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

    public Location[] getBounds() { return this.bounds; }

    /**
     * Unique id for this region
     * @return
     */
    public String getId() {
        return loc.getBlockX() + "" + loc.getBlockY() + "" + loc.getBlockZ() + "" + loc.getWorld().getUID().toString();
    }
}
