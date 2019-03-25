package com.hotmail.intrinsic.menubuilder;

import com.hotmail.intrinsic.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class MenuBuilder {

    private int size;
    private String title;
    private Inventory inv;
    private HashMap<Integer, Button> buttons;
    private String name;

    public MenuBuilder(String name) {
        this.name = name;
        buttons = new HashMap<>();
    }

    /**
     * Get the inventory created by this menu builder
     * @return the Bukkit Inventory object
     */
    public Inventory getInventory() {
        return inv;
    }

    public String getName() { return name; }

    /**
     * Set the inventory size for this menu
     * @param size of the Inventory for this menu, must be in increments of 9
     * @return reference to self
     */
    public MenuBuilder size(int size) {
        this.size = size;
        return this;
    }

    /**
     * Get the inventory size
     * @return int size of inventory
     */
    public int size() {
        return size;
    }

    /**
     * Set the title for this menu/inventory
     * @param title displayed when this inventory is opened
     * @return reference to self
     */
    public MenuBuilder title(String title) {
        this.title = title;
        return this;
    }

    /**
     * Gets the title of the inventory
     * @return String title of this inventory
     */
    public String title() {
        return title;
    }

    /**
     * Add a button to the menu at the specified position
     * @param position corresponding to the actual inventory position
     * @param button to place at this specific position
     * @return reference to self
     */
    public MenuBuilder button(int position, Button button) {
        buttons.put(position, button);
        return this;
    }

    public MenuBuilder button(int position, Button button, ButtonListener listener) {
        button(position, button);
        button.addListener(listener);
        return this;
    }

    /**
     * Check if theres a button at the position
     * @param position
     * @return
     */
    public boolean hasButton(int position) {
        return buttons.containsKey(position);
    }

    /**
     * Gets a button at a specific slot
     * @param position
     * @return
     */
    public Button getButton(int position) {
        return buttons.get(position);
    }

    /**
     * Get all the buttons in the list
     * @return
     */
    public Collection<Button> getButtons() {
        return buttons.values();
    }

    /**
     * Removes a button and updates the inventory at
     * a specified position
     * @param position
     */
    public void removeButton(int position) {
        buttons.remove(position);
        update();
    }

    public void build() {
        inv = Bukkit.createInventory(null, size(), StringUtil.colorize(title()));
    }

    /**
     * Show the inventory for a player
     * @param player to show this menu for
     */
    public void show(Player player) {
        build();
        update();
        List<Button> buttonList = new ArrayList<>(buttons.values());

        // Call the onEnable listener
        for(Button b : buttonList) {
            for(ButtonListener listener : b.getListeners())
                if(b.hasListener()) listener.onEnable(b, player);
        }

        // Listen for events in this menu
        MenuBuilderListener.listen(this);
        player.openInventory(inv);
    }

    public void update() {
        if(inv == null) return;
        inv.clear();

        // Populate the inventory with buttons
        for(Button b : buttons.values()) inv.setItem(b.getPosition(), b.getIcon());
    }

    /**
     * Get all the ButtonListeners for all buttons in this MenuBuilder
     * @return list of ButtonListeners
     */
    public List<ButtonListener> getListeners() {
        List<ButtonListener> listeners = new ArrayList<>();

        // Sort the ButtonListeners into InputListeners
        for (Button btn : getButtons()) {
            if (!btn.hasListener()) continue;
            listeners.addAll(btn.getListeners());
        }

        return listeners;
    }

    /**
     * Get all ButtonListeners for every MenuBuilders buttons
     * @return list of ButtonListeners
     */
    public static List<ButtonListener> getAllInputListeners() {
        List<ButtonListener> listeners = new ArrayList<>();

        for(MenuBuilder menuBuilder : MenuBuilderListener.getMenus()) {
            listeners.addAll(menuBuilder.getListeners());
        }

        return listeners;
    }
}