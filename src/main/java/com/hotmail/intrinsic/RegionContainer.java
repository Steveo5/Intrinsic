package com.hotmail.intrinsic;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RegionContainer {
    private List<Region> regions;

    public RegionContainer() {
        this.regions = new ArrayList<Region>();
    }

    public void addLoadedRegion(Region region) {
        this.regions.add(region);
    }

    public List<Region> getLoadedRegions() {
        return this.regions;
    }

    public Region createRegion(RegionType type, Location loc, Player owner) {
        OfflinePlayer p = Bukkit.getOfflinePlayer(owner.getUniqueId());
        Region region = new Region(type, loc, p);
        this.addLoadedRegion(region);

        Intrinsic.getStorage().saveRegion(region);

        return region;
    }

    public List<Region> getIntersecting(Location location) {
        return this.getIntersecting(location, this.regions);
    }

    public List<Region> getIntersecting(Location location, List<Region> list) {
        List<Region> intersecting = new ArrayList<Region>();

        for(Region region : list) {
            Location bound1 = region.getBounds()[0];
            Location bound2 = region.getBounds()[1];

            // Check if the location is inside the region
            if(location.getBlockX() >= bound1.getBlockX() && location.getBlockX() <= bound2.getBlockX()) {
                if(location.getBlockY() >= bound1.getBlockY() && location.getBlockY() <= bound2.getBlockY()) {
                    if(location.getBlockZ() >= bound1.getBlockZ() && location.getBlockZ() <= bound2.getBlockZ()) {
                        intersecting.add(region);
                    }
                }
            }
        }

        return intersecting;
    }
}
