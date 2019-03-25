package com.hotmail.intrinsic.menubuilder;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.Iterator;

public class MenuBuilderListener implements Listener {

    private static HashSet<MenuBuilder> menus;

    public MenuBuilderListener(Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        menus = new HashSet<>();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent evt) {
        if(!(evt.getWhoClicked() instanceof Player)) return;

        Player player = (Player)evt.getWhoClicked();
        int slot = evt.getRawSlot();

        // Loop over all the menus we know of
        for(MenuBuilder next : menus) {
            if(!next.getInventory().equals(evt.getInventory())) continue;
            // Check if the inventory has a button at the slot
            if(!next.hasButton(slot)) continue;

            Button b = next.getButton(slot);

            if(!b.hasListener()) continue;

            for(ButtonListener listener : b.getListeners()) {
                // Run the click event
                listener.onClick(b, player);

                // Check if this is an input button
                if (!(listener instanceof InputListener)) continue;

                InputListener inputListener = (InputListener) listener;
                inputListener.waitInput(player);
                player.closeInventory();
            }

            evt.setCancelled(true);
        }
    }

    @EventHandler
    public void onMessage(AsyncPlayerChatEvent evt) {
        Iterator<MenuBuilder> invItr = menus.iterator();
        // Loop over all the menus we know of
        while(invItr.hasNext()) {
            MenuBuilder next = invItr.next();
            // Check if any button is waiting input
            for(Button btn : next.getButtons()) {
                if(btn.hasListener()) {
                    for(ButtonListener listener : btn.getListeners()) {
                        if(listener instanceof InputListener) {
                            InputListener inputListener = (InputListener) listener;
                            // Check the button is waiting for our event player
                            if (inputListener.isWaiting() && inputListener.waiting().getUniqueId().equals(evt.getPlayer().getUniqueId())) {
                                // Call the event for the button
                                inputListener.onInput(evt.getPlayer(), evt.getMessage());
                                // Stop waiting for input on the button
                                inputListener.stopWaiting(evt.getPlayer());
                                evt.setCancelled(true);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Start listening on this menu for click events etc
     * @param menu
     */
    public static void listen(MenuBuilder menu) {
        menus.add(menu);
    }

}