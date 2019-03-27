package com.hotmail.intrinsic.listener;

import com.hotmail.intrinsic.Intrinsic;
import com.hotmail.intrinsic.util.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandListener implements CommandExecutor {

    private Intrinsic plugin;

    public CommandListener(Intrinsic plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!sender.isOp() && !sender.hasPermission("intrinsic.admin")) {
            sender.sendMessage(ChatColor.RED + command.getPermissionMessage());
        } else if(command.getName().equals("reload")) {
            reloadCmd(sender);
        } else if(command.getName().equals("near")) {
            if(isInGame(sender)) nearCommand((Player) sender, args);
        }
        return true;
    }

    /**
     * Check if the sender is an in-game player and if not send them a message
     * @param sender
     * @return false if they aren't in game
     */
    private boolean isInGame(CommandSender sender) {
        if(!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You must be in-game to do this");
            return false;
        }

        return true;
    }

    private void reloadCmd(CommandSender sender) {
        plugin.reload();
        sender.sendMessage(ChatColor.GREEN + "Intrinsic has been reloaded");
    }

    private void nearCommand(Player sender, String[] args) {
        // Radius in chunks, 0 default as in just the chunk they are standing in
        int radius = 0;

        if(args.length > 0) {
            if(!StringUtil.isInteger(args[0])) {
                sender.sendMessage(ChatColor.RED + "Usage: /region near <radius>");
                return;
            } else {
                radius = Integer.parseInt(args[0]);
            }
        }

        //TODO find near
    }
}
