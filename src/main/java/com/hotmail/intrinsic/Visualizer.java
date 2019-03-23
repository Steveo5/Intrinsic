package com.hotmail.intrinsic;

import com.hotmail.intrinsic.util.BlockUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Visualizer {

    private Intrinsic plugin;
    private List<Region> visualizing;
    private long timerInterval = 15L;

    class VisualizerTask implements Runnable {

        @Override
        public void run() {
            if(visualizing.size() <= 0) return;

            Iterator<Region> regionItr = visualizing.iterator();

            while(regionItr.hasNext()) {
                Region next = regionItr.next();
                next.setVisualizingTimeLeft(next.getVisualizingTimeLeft() - timerInterval);

                showOnce(next);

                // Reset the time for, visualization time and remove the region from visualizing
                if(next.getVisualizingTimeLeft() > 0) continue;

                next.resetVisualizingTime();
                regionItr.remove();
            }
        }
    }

    public Visualizer(Intrinsic plugin) {
        this.plugin = plugin;
        this.visualizing = new ArrayList<>();

        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new VisualizerTask(), 20L * 10, timerInterval);
    }
    /**
     * Visualize a regions border for a specific amount of time
     * @param region
     */
    public void show(Region region, long ms) {
        if(!visualizing.contains(region)) {
            visualizing.add(region);
        }

        region.setVisualizingTimeFor(ms);
        region.setVisualizingTimeLeft(ms);
    }

    /**
     * Visualize a regions border
     * @param region
     */
    public void showOnce(Region region) {
        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.RED, 1);
        for(Block b : region.getBorderBlocks()) {
            int x = b.getX(), y = b.getY() + 1, z = b.getZ();
            region.getLocation().getWorld().spawnParticle(Particle.REDSTONE, x, y, z, 0, dustOptions);
        }
    }


}
