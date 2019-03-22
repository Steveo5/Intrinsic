package com.hotmail.intrinsic;

import java.util.*;

public class RegionSet {

    private NavigableMap<Integer, Region> regionSet = new TreeMap<>();

    protected RegionSet() {}

    protected void put(int priority, Region region) {
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

}
