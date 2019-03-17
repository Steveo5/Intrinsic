package com.hotmail.intrinsic;

import com.hotmail.intrinsic.listener.RegionCreateListener;
import com.hotmail.intrinsic.storage.MysqlConnector;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.Bukkit.getPluginManager;

public class Intrinsic extends JavaPlugin {

    private static List<RegionType> regionTypes = new ArrayList<RegionType>();
    private static FileConfiguration cfg;
    private static IntrinsicLogger logger;
    private static RegionContainer regionContainer;
    private static MysqlConnector storage;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        cfg = this.getConfig();
        logger = new IntrinsicLogger(this);
        regionContainer = new RegionContainer();
        storage = new MysqlConnector();

        RegionType small = new RegionType("small-protection", Material.STONE, 5);
        regionTypes.add(small);

        getPluginManager().registerEvents(new RegionCreateListener(), this);
    }

    public static List<RegionType> getRegionTypes() {
        return regionTypes;
    }

    /**
     * Check if there is a RegionType that matches an ItemStack
     * @param item
     * @return false if not found
     */
    public static boolean hasRegionType(ItemStack item) {
        for(RegionType regionType : regionTypes) {
            if(regionType.getBlock().isSimilar(item)) return true;
        }

        return false;
    }

    public static RegionType getRegionType(ItemStack item) {
        for(RegionType regionType : regionTypes) {
            if(regionType.getBlock().isSimilar(item)) return regionType;
        }

        return null;
    }

    public static IntrinsicLogger getIntrinsicLogger() {
        return logger;
    }

    public static MysqlConnector getStorage() {
        return storage;
    }

    public static RegionContainer getRegionContainer() {
        return regionContainer;
    }

}
