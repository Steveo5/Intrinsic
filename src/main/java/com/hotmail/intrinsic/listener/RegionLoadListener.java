package com.hotmail.intrinsic.listener;

import com.hotmail.intrinsic.Intrinsic;
import com.hotmail.intrinsic.storage.IntersectingCallback;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class RegionLoadListener implements Listener {


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent evt) {
        Chunk c = evt.getPlayer().getLocation().getChunk();

        // Try and load any regions found in a radius around the player
        Intrinsic.getStorage().getIntersecting(c, 1, new IntersectingCallback() {
            @Override
            public void run() {
                rs.load();
            }
        });
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent evt) {
        Chunk from = evt.getFrom().getChunk();
        Chunk to = evt.getTo().getChunk();

        if(from.getX() != to.getX() || from.getZ() != to.getZ()) this.onPlayerMoveChunk(evt);
    }

    public void onPlayerMoveChunk(PlayerMoveEvent evt) {
        Chunk to = evt.getTo().getChunk();

        // Try and load any regions found in a radius around the player
        Intrinsic.getStorage().getIntersecting(to, 1, new IntersectingCallback() {
            @Override
            public void run() {
                rs.load();
            }
        });
    }

}
