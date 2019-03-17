package com.hotmail.intrinsic;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

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
}
