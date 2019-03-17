package com.hotmail.intrinsic.listener;

import com.hotmail.intrinsic.Intrinsic;
import com.hotmail.intrinsic.Region;
import com.hotmail.intrinsic.RegionType;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.logging.Level;

public class RegionCreateListener implements Listener {


    @EventHandler
    public void onRegionCreate(BlockPlaceEvent evt) {
        if(Intrinsic.hasRegionType(evt.getItemInHand())) {
            Player p = evt.getPlayer();
            RegionType regionType = Intrinsic.getRegionType(evt.getItemInHand());

            // Finally create our new region
            Region region = Intrinsic.getRegionContainer().createRegion(regionType, evt.getBlock().getLocation(), p);
            Location rLoc = region.getLocation();
            p.sendMessage(ChatColor.GREEN + "This area is now protected");
            Intrinsic.getIntrinsicLogger()
                    .logFile(Level.INFO, "Player " + p.getDisplayName() + " (" + p.getUniqueId().toString() + ") placed a region at x:" + rLoc.getBlockX() + " y:" + rLoc.getBlockY() + " z:" + rLoc.getBlockZ());
        }
    }

}
