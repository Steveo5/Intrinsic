package com.hotmail.intrinsic.listener;

import com.hotmail.intrinsic.Intrinsic;
import com.hotmail.intrinsic.Region;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class RegionDestroyListener implements Listener {

    @EventHandler
    public void onRegionDestroy(BlockBreakEvent evt) {
        Region r = Intrinsic.getRegionContainer().getRegionAt(evt.getBlock().getLocation());

        if(r != null) {
            evt.getPlayer().sendMessage(ChatColor.RED + "You have removed the region");
            Intrinsic.getRegionContainer().destroyRegion(r);
        }
    }

}
