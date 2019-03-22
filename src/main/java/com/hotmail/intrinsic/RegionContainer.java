package com.hotmail.intrinsic;

import com.hotmail.intrinsic.event.RegionCreateEvent;
import com.hotmail.intrinsic.event.RegionLoadEvent;
import com.hotmail.intrinsic.exception.RegionLoadException;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventException;

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
     * @return true if loaded or false if the region is already loaded or
     * the event is canelled
     */
    public boolean loadRegion(Region region) {
        if(this.regions.containsKey(region.getId())) return false;

        // Create the event here
        RegionLoadEvent event = new RegionLoadEvent(region);
        // Call the event
        Bukkit.getServer().getPluginManager().callEvent(event);
        // Check if the event is not cancelled
        if (!event.isCancelled())  {
            this.regions.put(region.getId(), region);
            return false;
        }

        return true;
    }

    /**
     * Load all regions in a list, ignoring those that return false
     * or fail to load
     * @param regions
     */
    public void loadRegions(Collection<Region> regions) {
        for (Region r : regions) loadRegion(r);
    }

    public boolean unloadRegion(Region region) {
        if(this.regions.containsKey(region.getId())) {
            this.regions.remove(region.getId());
            return true;
        }

        return false;
    }

    public Collection<Region> getLoadedRegions() {
        return this.regions.values();
    }

    /**
     * Create a new region at a specific location, will throw an exception if the
     * region create event is cancelled, failed to load region or failed to save
     * region to database
     * @param type
     * @param loc
     * @param owner
     * @return
     */
    public Region createRegion(RegionType type, Location loc, Player owner) throws RegionLoadException, EventException {

        OfflinePlayer p = Bukkit.getOfflinePlayer(owner.getUniqueId());

        // Get the highest priority region and make this region higher priority
        Region highest = Intrinsic.getRegionContainer().getIntersecting(loc.getChunk(), 0).highestPriority();
        int highestPriority = highest == null ? 1 : highest.getPriority() + 1;

        Region region = new Region(type, loc, p, highestPriority);

        // Create the event here
        RegionCreateEvent event = new RegionCreateEvent(region);
        // Call the event
        Bukkit.getServer().getPluginManager().callEvent(event);
        // Check if the event is not cancelled
        if (event.isCancelled()) throw new EventException("The event was cancelled");

        if(!this.loadRegion(region)) {
            throw new RegionLoadException("The region is already loaded");
        }

        Intrinsic.getStorage().saveRegion(region);

        return region;
    }

    /**
     * Unload and destroy a region
     * @param region
     */
    public void destroyRegion(Region region) {
        region.unload();
        Intrinsic.getStorage().destroyRegion(region);
    }

    public RegionSet getIntersecting(Chunk chunk, int radius) {
        RegionSet rs = new RegionSet();

        for(Region r : this.getLoadedRegions()) {
            Chunk min = r.getBounds()[0], max = r.getBounds()[1];
            int minX = min.getX(), maxX = max.getX(), minZ = min.getZ(), maxZ = max.getZ();

            // Check if the chunk is within the loop regions min max bounds
            if(chunk.getX() >= minX && chunk.getX() <= maxX && chunk.getZ() >= minZ && chunk.getZ() <= maxZ) continue;
            rs.put(r.getPriority(), r);
        }

        return rs;
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
