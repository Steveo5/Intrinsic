package com.hotmail.intrinsic.listener;

import com.hotmail.intrinsic.Intrinsic;
import com.hotmail.intrinsic.Region;
import com.hotmail.intrinsic.RegionType;
import com.hotmail.intrinsic.storage.IntersectingCallback;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.logging.Level;

public class RegionCreateListener implements Listener {

    private Intrinsic plugin;

    public RegionCreateListener(Intrinsic plugin) {
        this.plugin = plugin;
    }


    @EventHandler
    public void onRegionCreate(BlockPlaceEvent evt) {
        if(Intrinsic.hasRegionType(evt.getItemInHand())) {
            Player p = evt.getPlayer();
            RegionType regionType = Intrinsic.getRegionType(evt.getItemInHand());

            // Finally create our new region
            Region region = Intrinsic.getRegionContainer().createRegion(regionType, evt.getBlock().getChunk(), p);
            Chunk rLoc = region.getCenter();
            p.sendMessage(ChatColor.GREEN + "This area is now protected, right click the block to customise it");
            plugin.getLogger().log(Level.ALL, "Player " + p.getDisplayName() + " (" + p.getUniqueId().toString() + ") placed a region at x:" + rLoc.getX() + " z:" + rLoc.getZ());
        }
    }

    @EventHandler
    public void onRightClickBlock(PlayerInteractEvent evt) {
        if(evt.getHand() == EquipmentSlot.HAND && evt.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Intrinsic.getStorage().getIntersecting(evt.getClickedBlock().getChunk(), new IntersectingCallback() {
                @Override
                public void run() {

                }
            });
        }
    }
}
