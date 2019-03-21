package com.hotmail.intrinsic.listener;

import com.hotmail.intrinsic.Intrinsic;
import com.hotmail.intrinsic.Region;
import com.hotmail.intrinsic.storage.IntersectingCallback;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.List;

public class RegionLoadListener implements Listener {


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent evt) {
        this.loadNearbyInterceptignRegions(evt.getPlayer().getLocation().getChunk(), 1);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent evt) {
        Location from = evt.getFrom();
        Location to = evt.getTo();

        if ((evt.getFrom().getBlockX() == evt.getTo().getBlockX()) &&
                (evt.getFrom().getBlockZ() == evt.getTo().getBlockZ()) &&
                (evt.getFrom().getBlockY() == evt.getTo().getBlockY())) {
            return;
        }

        if(from.getChunk().getX() != to.getChunk().getX() ||
                from.getChunk().getZ() != to.getChunk().getZ()) {
            this.onPlayerMoveChunk(evt);
        }
    }

    public void onPlayerMoveChunk(PlayerMoveEvent evt) {
        Chunk to = evt.getTo().getChunk();
        this.loadNearbyInterceptignRegions(to, 1);
    }

    private void loadNearbyInterceptignRegions(Chunk chunk, int radius) {
        for(int x = chunk.getX() - radius; x < chunk.getX() + radius; x++) {
            for(int z = chunk.getZ() - radius; z < chunk.getZ() + radius; z++) {
                Chunk toLoad = chunk.getWorld().getChunkAt(x, z);
                this.loadInterceptingChunks(toLoad);
            }
        }
    }

    /**
     * Loads all chunks that intercept another chunk
     * @param chunk
     */
    private void loadInterceptingChunks(Chunk chunk) {
        Intrinsic.getStorage().getIntersecting(chunk, new IntersectingCallback() {
            @Override
            public void run() {
                for(Region r : this.regions) {
                    if(Intrinsic.getRegionContainer().loadRegion(r)) {
                        System.out.println("Loading region " + r.getLocation().toString());
                        System.out.println("Loaded chunks " + Intrinsic.getRegionContainer().getLoadedRegions().size());
                    } else {
                        System.out.println("Region " + r.getLocation().toString() + " failed to load, already loaded!");
                    }
                }
            }
        });
    }


}
