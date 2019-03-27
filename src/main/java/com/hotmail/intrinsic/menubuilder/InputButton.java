package com.hotmail.intrinsic.menubuilder;

import com.hotmail.intrinsic.util.StringUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class InputButton extends Button {
    public InputButton(int position, ItemStack icon) {
        super(position, icon);

        // Set default listeners
        InputListener listener = new InputListener() {
            @Override
            public void onInput(Player player, String message) { InputButton.this.onInput(player, message); }
        };

        addListener(listener);
    }

    public InputButton(int position, Material material, String itemTitle) {
        this(position, StringUtil.itemFromString(material, itemTitle));
    }

    public void onInput(Player player, String message) {}

}
