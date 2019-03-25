package com.hotmail.intrinsic;

import java.util.*;

public class RegionSet {

    private List<Region> regionSet = new ArrayList<>();

    public RegionSet() {}

    public void add(Region region) {
        regionSet.add(region);
    }

    public List<Region> all() {
        regionSet.sort(Comparator.comparing(Region::getPriority));
        return regionSet;
    }

    public Region highestPriority() {
        if(regionSet.isEmpty()) return null;
        return all().get(this.regionSet.size() - 1);
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
        return highest != null && highest.hasPermission(uuid);
    }

}
