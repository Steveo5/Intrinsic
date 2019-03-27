package com.hotmail.intrinsic;

import com.hotmail.intrinsic.event.RegionCreateEvent;
import com.hotmail.intrinsic.event.RegionLoadEvent;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class RegionContainer {
    private HashMap<String, Region> regions;

    public RegionContainer() {
        this.regions = new HashMap<>();
    }

    /**
     * Load a region into the region container
     * @param region specific region to load
     * @return true if loaded or false if the region is already loaded or
     * the event is cancelled
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
            return true;
        }

        return false;
    }

    /**
     * Load all regions in a list, ignoring those that return false
     * or fail to load
     * @param regions all of the regions to load at once, no return value so
     * if you want to see which ones were loaded it would be best to load them individually
     */
    public void loadRegions(Collection<Region> regions) {
        for (Region r : regions) loadRegion(r);
    }

    public boolean unloadRegion(Region region) {
        if(!this.regions.containsKey(region.getId())) return false;
        this.regions.remove(region.getId());

        return true;
    }

    public Collection<Region> getLoadedRegions() {
        return this.regions.values();
    }

    /**
     * Create a new region at a specific location, will throw an exception if the
     * region create event is cancelled, failed to load region or failed to save
     * region to database
     * @param type the basic parameters for this region
     * @param loc where the center block was placed
     * @param owner player who placed this region down
     * @return an object reference of the region that was created
     */
    public Region createRegion(RegionType type, Location loc, Player owner) {

        OfflinePlayer p = Bukkit.getOfflinePlayer(owner.getUniqueId());

        // Get the highest priority region and make this region higher priority
        Region highest = Intrinsic.getRegionContainer().getIntersecting(loc.getChunk()).highestPriority();
        int highestPriority = highest == null ? 1 : highest.getPriority() + 1;

        Region region = new Region(type, loc, p, highestPriority, new ArrayList<>());

        // Create the event here
        RegionCreateEvent event = new RegionCreateEvent(region);
        // Call the event
        Bukkit.getServer().getPluginManager().callEvent(event);
        // Check if the event is not cancelled
        if (event.isCancelled()) return null;

        this.loadRegion(region);

        Intrinsic.getStorage().saveRegion(region);

        return region;
    }

    /**
     * Unload and destroy a region
     * @param region to specifically unload and delete from the database
     */
    public void destroyRegion(Region region) {
        region.unload();
        Intrinsic.getStorage().destroyRegion(region);
    }

    public RegionSet getIntersecting(Chunk chunk) {
        RegionSet rs = new RegionSet();
        int x = chunk.getX(), z = chunk.getZ();

        for(Region r : this.getLoadedRegions()) {
            Chunk min = r.getBounds()[0], max = r.getBounds()[1];
            // Current iterator chunk bounds
            int minX = min.getX(), maxX = max.getX(), minZ = min.getZ(), maxZ = max.getZ();
            // Check if the chunk is within the loop regions min max bounds
            if(x >= minX && x <= maxX && z >= minZ && z <= maxZ) rs.add(r);
        }

        return rs;
    }

    /**
     * Check if a player has permission to do an action at the
     * specified location
     * @param chunk to check if the player can build at
     * @param player to check permissions for
     * @return boolean true if they have permission at the highest
     * priority region in the chunk
     */
    public boolean hasPermission(Chunk chunk, Player player) {
        RegionSet rs = Intrinsic.getRegionContainer().getIntersecting(chunk);

        if(rs.all().size() < 1) return true;

        return rs.hasPermission(player.getUniqueId());

    }

    /**
     * Get a region at a specific location
     * @param location to get a region at
     * @return null if no region found
     */
    public Region getRegionAt(Location location) {
        for(Region r : this.getLoadedRegions()) if(r.getLocation().equals(location)) return r;

        return null;
    }
}
