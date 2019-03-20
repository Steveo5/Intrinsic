package com.hotmail.intrinsic;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
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

    public void addRegion(Region region) {
        this.regions.add(region);
    }

    public List<Region> getLoadedRegions() {
        return this.regions;
    }

    public Region createRegion(RegionType type, Location loc, Player owner) {
        OfflinePlayer p = Bukkit.getOfflinePlayer(owner.getUniqueId());
        Region region = new Region(type, loc, p, 0);
        this.addRegion(region);

        Intrinsic.getStorage().saveRegion(region);

        return region;
    }

    public List<Region> getIntersecting(Location location) {
        return this.getIntersecting(location, this.regions);
    }

    public List<Region> getIntersecting(Location location, List<Region> list) {
        List<Region> intersecting = new ArrayList<Region>();


        return intersecting;
    }
}
