package com.hotmail.intrinsic;

import org.bukkit.entity.Player;

import java.util.*;

public class RegionSet {

    private NavigableMap<Integer, Region> regionSet = new TreeMap<>();

    public RegionSet() {}

    public void put(int priority, Region region) {
        regionSet.put(priority, region);
    }

    public Collection<Region> all() {
        if(regionSet.isEmpty()) return new ArrayList<>();
        return regionSet.values();
    }

    public Region highestPriority() {
        if(regionSet.isEmpty()) return null;
        return this.regionSet.lastEntry().getValue();
    }

    /**
     * Load all of the regions in this region set
     */
    public void load() {
        //Intrinsic.getRegionContainer().loadRegions(all());
        for(Region r : all()) {
            if(Intrinsic.getRegionContainer().loadRegion(r)) {
                System.out.println("Region loaded " + r.getLocation().toString());
            }
        }
    }

    public boolean hasPermission(UUID uuid) {
        Region highest = highestPriority();
        return highest != null && (highest.isOwner(uuid) || highest.isWhitelisted(uuid));
    }

}
