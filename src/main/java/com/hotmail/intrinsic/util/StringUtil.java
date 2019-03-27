package com.hotmail.intrinsic.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StringUtil {
    public static String colorize(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }

    public static ItemStack itemFromString(Material material, String title) {
        ItemStack item = new ItemStack(material);
        ItemMeta im = item.getItemMeta();
        im.setDisplayName(colorize(title));
        item.setItemMeta(im);
        return item;
    }

    public static List<String> listFromString(String str) {
        str = colorize(str);
        return Arrays.asList(str.split("\\|"));
    }

    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException | NullPointerException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }
}
