package com.hotmail.intrinsic;

import com.hotmail.intrinsic.util.BlockUtil;
import org.bukkit.*;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Region {
    private RegionType type;
    private Location loc;
    private Chunk center;
    private OfflinePlayer owner;
    private Chunk[] bounds = new Chunk[2];
    private int priority;
    private List<Block> borderBlocks;
    private List<UUID> whitelist;

    private long visualizingTimeFor, visualizingTimeLeft;

    public Region(RegionType type, Location loc, OfflinePlayer owner, int priority) {
        this.type = type;
        this.loc = loc;
        this.center = loc.getChunk();
        this.owner = owner;
        this.priority = priority;
        this.borderBlocks = new ArrayList<Block>();

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

    public boolean isOwner(UUID uuid) { return uuid.equals(owner); }

    public List<UUID> getWhitelist() { return this.whitelist; }

    public boolean isWhitelisted(UUID uuid) { return this.whitelist.contains(uuid); }

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

    /**
     * Visibly show the region in the world
     */
    public void visualize() {
        Intrinsic.getVisualizer().show(this, Intrinsic.getIntrinsicConfig().getLong("visualization.time"));
    }

    public void visualize(long ms) {
        Intrinsic.getVisualizer().show(this, ms);
    }

    /**
     * Get the amount of time left this regions border
     * is visible for
     * @return
     */
    protected long getVisualizingTimeLeft() {
        return this.visualizingTimeLeft;
    }

    protected void setVisualizingTimeLeft(long timeLeft) {
        this.visualizingTimeLeft = timeLeft;
    }

    /**
     * Get how long this regions visible border was
     * initially set for
     * @return
     */
    protected long getVisualizingTimeFor() {
        return this.visualizingTimeFor;
    }

    protected void setVisualizingTimeFor(long timeFor) {
        this.visualizingTimeFor = timeFor;
    }

    /**
     * Get whether this region is currently being shown
     * to in-game players
     * @return
     */
    public boolean isVisualizing() {
        return this.visualizingTimeLeft > 0;
    }

    /**
     * Reset this regions visual timer
     */
    public void resetVisualizingTime() {
        this.visualizingTimeFor = 0;
        this.visualizingTimeLeft = 0;
    }

    public List<Block> getBorderBlocks() {
        if(borderBlocks.size() < 1) {
            World w = this.getLocation().getWorld();
            Location min = this.getBounds()[0].getBlock(0, 0, 0).getLocation();
            Location max = this.getBounds()[1].getBlock(15, 0, 15).getLocation();

            this.borderBlocks = BlockUtil.getBorderBlocks(min, max);
        }

        return this.borderBlocks;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * Unload a region
     * @return false if the region is already unloaded
     */
    public boolean unload() {
        return Intrinsic.getRegionContainer().unloadRegion(this);
    }

}
