package com.hotmail.intrinsic;

import com.hotmail.intrinsic.menubuilder.Button;
import com.hotmail.intrinsic.menubuilder.InputButton;
import com.hotmail.intrinsic.menubuilder.MenuBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public class WhitelistMenu extends MenuBuilder {
    public WhitelistMenu(String name) {
        super(name);

        title("Whitelist").size(45);

        button(new Button(44, Material.PAPER, "&3Back") {
            @Override
            public void onClick(Button button, Player player) {
                Intrinsic.getMenus().get("main-menu").show(player);
            }

            @Override
            public void onEnable(Button button, Player player) {
                IntrinsicPlayer iPlayer = Intrinsic.adapt(player);
                initializeWhitelistButtons(iPlayer);
            }
        });

        button(new InputButton(35, Material.STRUCTURE_VOID, "&3Add") {
            @Override
            public void onInput(Player player, String message) {
                IntrinsicPlayer iPlayer = Intrinsic.adapt(player);
                iPlayer.getLastViewed().addWhitelist(iPlayer.getBase().getUniqueId());
                Intrinsic.getMenus().get("whitelist-menu").show(player);
            }
        });
    }

    private void initializeWhitelistButtons(IntrinsicPlayer iPlayer) {
        for(int i=0 ; i < iPlayer.getLastViewed().getWhitelist().size() ; i++) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(iPlayer.getLastViewed().getWhitelist().get(i));

            Button btn = new Button(i, Material.SKELETON_SKULL, offlinePlayer.getName(), "Click to remove") {
                @Override
                public void onClick(Button button, Player player) {
                    Intrinsic.adapt(player).getLastViewed().removeWhitelisted(UUID.fromString(data().get(0)));
                }
            };

            // Transfer through the UUID of player for this button
            button(btn.data(offlinePlayer.getUniqueId().toString()));
        }
    }
}
