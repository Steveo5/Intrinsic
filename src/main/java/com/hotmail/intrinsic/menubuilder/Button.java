package com.hotmail.intrinsic.menubuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.hotmail.intrinsic.util.StringUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Button {

    private int position;
    private ItemStack item;
    private List<ButtonListener> listeners;

    public Button(int position, ItemStack icon) {
        this.position = position;
        this.item = icon;
        listeners = new ArrayList<>();

        // Set default listeners
        ButtonListener listener = new ButtonListener() {
            @Override
            public void onEnable(Button button, Player player) { Button.this.onEnable(button, player); }

            @Override
            public void onClick(Button button, Player player) { Button.this.onClick(button, player); }
        };

        addListener(listener);
    }

    /**
     * Sets the buttons icon title
     * @param title
     * @return
     */
    public Button title(String title) {
        ItemMeta im = item.getItemMeta();
        im.setDisplayName(StringUtil.colorize(title));
        item.setItemMeta(im);
        return this;
    }

    /**
     * Set the buttons icon lore
     * @param lore
     * @return
     */
    public Button lore(String lore) {
        ItemMeta im = item.getItemMeta();
        im.setLore(Arrays.asList(StringUtil.colorize(lore).split("\\|")));
        item.setItemMeta(im);
        return this;
    }

    public int getPosition() {
        return position;
    }

    public ItemStack getIcon() {
        return item;
    }

    /**
     * Check if this button has a certain listener type
     * @return
     */
    public boolean hasListener() {
        return listeners != null && listeners.size() > 0;

    }

    /**
     * Get a listener for the button
     * @return
     */
    public List<ButtonListener> getListeners() {
        return listeners;
    }

    /**
     * Make this button start listening on a certain listener
     * @param listener
     */
    public void addListener(ButtonListener listener) {
        this.listeners.add(listener);
    }

    public void onEnable(Button button, Player player) {}
    public void onClick(Button button, Player player) {}

}
