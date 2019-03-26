package com.hotmail.intrinsic;

import com.hotmail.intrinsic.menubuilder.Button;
import com.hotmail.intrinsic.menubuilder.MenuBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class WhitelistMenu extends MenuBuilder {
    public WhitelistMenu(String name) {
        super(name);

        title("Whitelist").size(45);

        button(new Button(44, Material.PAPER, "&3Back") {
            @Override
            public void onClick(Button button, Player player) {
                Intrinsic.getMenus().get("main-menu").show(player);
            }
        });

        button(new Button(35, Material.SKELETON_SKULL, "&3Add") {

        });
    }
}
