package com.hotmail.intrinsic;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

public class Region {
    private RegionType type;
    private Location loc;
    private Chunk center;
    private OfflinePlayer owner;
    private Chunk[] bounds = new Chunk[2];
    private int priority;

    public Region(RegionType type, Location loc, OfflinePlayer owner, int priority) {
        this.type = type;
        this.loc = loc;
        this.center = loc.getChunk();
        this.owner = owner;
        this.priority = priority;

        bounds[0] = center.getWorld().getChunkAt(center.getX() - type.getRadius(), center.getZ() - type.getRadius());
        bounds[1] = center.getWorld().getChunkAt(center.getX() + type.getRadius(), center.getZ() + type.getRadius());
    }

    public RegionType getType() {
        return this.type;
    }

    public Chunk getCenter() {
        return this.center;
    }

    public OfflinePlayer getOwner() {
        return this.owner;
    }

    public String getDisplayName() {
        return type.getName().replace("-", " ");
    }

    public Chunk[] getBounds() { return this.bounds; }

    public int getPriority() { return this.priority; }

    public Location getLocation() { return this.loc; }

    /**
     * Unique id for this region
     * @return
     */
    public String getId() {
        return loc.getBlockX() + "" + loc.getBlockY() + "" +  loc.getBlockZ() + "" + center.getWorld().getUID().toString();
    }
}
