package com.hotmail.intrinsic;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class RegionContainer {
    private HashMap<String, Region> regions;

    public RegionContainer() {
        this.regions = new HashMap<>();
    }

    /**
     * Load a region into the region container
     * @param region
     * @return true if loaded or false if the region is already loaded
     */
    public boolean loadRegion(Region region) {
        if(this.regions.containsKey(region.getId())) return false;
        this.regions.put(region.getId(), region);

        return true;
    }

    public void unloadRegion(Region region) {
        this.regions.remove(region);
    }

    public Collection<Region> getLoadedRegions() {
        return this.regions.values();
    }

    public Region createRegion(RegionType type, Location loc, Player owner) {
        OfflinePlayer p = Bukkit.getOfflinePlayer(owner.getUniqueId());
        Region region = new Region(type, loc, p, 0);
        this.loadRegion(region);

        Intrinsic.getStorage().saveRegion(region);

        return region;
    }

    public List<Region> getIntersecting(Chunk chunk) {
        return this.getIntersecting(chunk, this.getLoadedRegions());
    }

    public List<Region> getIntersecting(Chunk chunk, Collection<Region> list) {
        List<Region> intersecting = new ArrayList<Region>();

        // Check the bounds
        for(Region r : this.getLoadedRegions()) {
            Chunk[] bounds = r.getBounds();
            if(bounds[0].getX() >= chunk.getX() && bounds[0].getX() <= chunk.getX()) {
                if(bounds[0].getZ() >= chunk.getZ() && bounds[0].getZ() <= chunk.getZ()) {
                    intersecting.add(r);
                }
            }
        }

        return intersecting;
    }

    /**
     * Get a region at a specific location
     * @param location
     * @return null if no region found
     */
    public Region getRegionAt(Location location) {
        for(Region r : this.getLoadedRegions()) {
            if(r.getLocation().equals(location)) return r;
        }

        return null;
    }
}
