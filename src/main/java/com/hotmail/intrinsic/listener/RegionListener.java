package com.hotmail.intrinsic.listener;

import com.hotmail.intrinsic.Region;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Collection;

public class RegionListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent evt) {

    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent evt) {

    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent evt) {

    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent evt) {
        if(evt.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if(evt.getClickedBlock().getType() == Material.CHEST ||
                    evt.getClickedBlock().getType() == Material.ENDER_CHEST ||
                    evt.getClickedBlock().getType() == Material.ENDER_CHEST) {
                this.onPlayerInteractChest(evt);
            }
        }
    }

    public void onPlayerInteractChest(PlayerInteractEvent evt) {

    }

    @EventHandler
    public void onPlayerMoveRegion(PlayerMoveEvent evt) {
        Chunk from = evt.getFrom().getChunk();
        Chunk to = evt.getTo().getChunk();

    }

    public void onPlayerEnterRegions(PlayerMoveEvent evt, Collection<Region> regions) {

    }

    public void onPlayerLeaveRegions(PlayerMoveEvent evt, Collection<Region> regions) {

    }
}
