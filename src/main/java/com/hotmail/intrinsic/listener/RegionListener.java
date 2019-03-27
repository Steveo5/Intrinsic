package com.hotmail.intrinsic.listener;

import com.hotmail.intrinsic.Intrinsic;
import com.hotmail.intrinsic.Region;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Collection;

public class RegionListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent evt) {
        if(Intrinsic.getRegionContainer().hasPermission(evt.getBlock().getChunk(), evt.getPlayer())) return;

        evt.getPlayer().sendMessage(ChatColor.RED + "This area is protected");
        evt.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent evt) {
        if(Intrinsic.getRegionContainer().hasPermission(evt.getBlock().getChunk(), evt.getPlayer())) return;

        evt.getPlayer().sendMessage(ChatColor.RED + "This area is protected");
        evt.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent evt) {
        if(!(evt.getDamager() instanceof Player)) return;

        Player p = (Player)evt.getDamager();
        if(Intrinsic.getRegionContainer().hasPermission(evt.getEntity().getLocation().getChunk(), p)) return;

        p.sendMessage(ChatColor.RED + "This area is protected");
        evt.setCancelled(true);
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
        if(Intrinsic.getRegionContainer().hasPermission(evt.getClickedBlock().getChunk(), evt.getPlayer())) return;

        evt.getPlayer().sendMessage(ChatColor.RED + "This area is protected");
        evt.setCancelled(true);
    }

    @EventHandler
    public void onPlayerMoveRegion(PlayerMoveEvent evt) {
        Chunk from = evt.getFrom().getChunk();
        Chunk to = evt.getTo().getChunk();

        if(from.getX() == to.getX() && from.getX() == to.getZ()) return;

        Region toRegion = Intrinsic.getRegionContainer().getIntersecting(to).highestPriority();
        if(toRegion == null) return;

        evt.getPlayer().spigot().sendMessage(
                ChatMessageType.ACTION_BAR, new TextComponent(
                        ChatColor.GREEN + "You have entered " + toRegion.getOwner().getName() + "s protection"
                )
        );
    }

    public void onPlayerEnterRegions(PlayerMoveEvent evt, Collection<Region> regions) {

    }

    public void onPlayerLeaveRegions(PlayerMoveEvent evt, Collection<Region> regions) {

    }

    @EventHandler
    public void onRightClickBlock(PlayerInteractEvent evt) {
        if(evt.getHand() != EquipmentSlot.HAND && evt.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Region region = Intrinsic.getRegionContainer().getRegionAt(evt.getClickedBlock().getLocation());

        if(region == null) return;
        if(!region.getOwner().equals(evt.getPlayer())) {
            evt.getPlayer().sendMessage(ChatColor.RED + "You do not own this region");
            evt.setCancelled(true);
        }

        Intrinsic.adapt(evt.getPlayer()).setLastViewed(region);
        Intrinsic.getMenus().get("main-menu").show(evt.getPlayer());
        evt.setCancelled(true);
    }
}
