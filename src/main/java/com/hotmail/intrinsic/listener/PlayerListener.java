package com.hotmail.intrinsic.listener;

import com.hotmail.intrinsic.Intrinsic;
import com.hotmail.intrinsic.IntrinsicPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Objects;

public class PlayerListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerLogin(PlayerLoginEvent evt) {
        IntrinsicPlayer iPlayer = new IntrinsicPlayer(evt.getPlayer());
        Intrinsic.addOnlineIntrinsicPlayer(iPlayer);
    }

    @EventHandler
    public void onPlayerLogout(PlayerQuitEvent evt) {
        Intrinsic.removeIntrinsicPlayer(Objects.requireNonNull(Intrinsic.adapt(evt.getPlayer())));
    }
}
