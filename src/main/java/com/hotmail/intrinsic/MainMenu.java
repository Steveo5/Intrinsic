package com.hotmail.intrinsic;

import com.hotmail.intrinsic.menubuilder.Button;
import com.hotmail.intrinsic.menubuilder.MenuBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MainMenu extends MenuBuilder {
    public MainMenu(String name) {
        super(name);

        title("Dashboard").size(9);

        button(new Button(0, Material.ENDER_EYE, "&3Show boundary") {
            @Override
            public void onClick(Button button, Player player) {
                Intrinsic.adapt(player).getLastViewed().visualize();
                player.sendMessage(ChatColor.GREEN + "Showing region borders");
                player.closeInventory();
            }
        });

        button(new Button(1, Material.PAPER, "&3Whitelist") {
            @Override
            public void onClick(Button button, Player player) {
                Intrinsic.getMenus().get("whitelist-menu").show(player);
            }
        });

        button(new Button(8, Material.BARRIER, "&3Close") {
            @Override
            public void onClick(Button button, Player player) {
                player.closeInventory();
            }
        });
    }
}
