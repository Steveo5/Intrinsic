package com.hotmail.intrinsic.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class StringUtil {
    public static String colorize(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }

    public static ItemStack itemFromString(Material material, String title) {
        ItemStack item = new ItemStack(material);
        ItemMeta im = item.getItemMeta();
        im.setDisplayName(title);
        item.setItemMeta(im);
        return item;
    }
}
