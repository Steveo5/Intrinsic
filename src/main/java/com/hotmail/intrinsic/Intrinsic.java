package com.hotmail.intrinsic;

import com.hotmail.intrinsic.listener.*;
import com.hotmail.intrinsic.menubuilder.MenuBuilder;
import com.hotmail.intrinsic.menubuilder.MenuBuilderListener;
import com.hotmail.intrinsic.storage.MysqlConnector;
import com.hotmail.intrinsic.util.StringUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.*;
import java.util.zip.GZIPOutputStream;

import static org.bukkit.Bukkit.getPluginManager;

public class Intrinsic extends JavaPlugin {

    private static List<RegionType> regionTypes = new ArrayList<>();
    private static FileConfiguration cfg;
    private static RegionContainer regionContainer;
    private static MysqlConnector storage;
    private static Visualizer visualizer;
    protected static HashMap<String, IntrinsicPlayer> intrinsicPlayers = new HashMap<>();

    private static HashMap<String, MenuBuilder> menus = new HashMap<>();

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        cfg = this.getConfig();
        regionContainer = new RegionContainer();
        storage = new MysqlConnector(this);
        visualizer = new Visualizer(this);

        if(!storage.testConnection()) {
            getLogger().log(Level.SEVERE, "MySQL connection failed, plugin will shutdown and nothing is protected!");
        } else {
            getLogger().log(Level.INFO, "MySQL Connection succeeded!");
        }

        loadRegionTypes();
        getLogger().log(Level.ALL, regionTypes.size() + " total regions have been loaded");

        getPluginManager().registerEvents(new RegionCreateListener(this), this);
        getPluginManager().registerEvents(new RegionLoadListener(), this);
        getPluginManager().registerEvents(new RegionDestroyListener(), this);
        getPluginManager().registerEvents(new RegionListener(), this);
        getPluginManager().registerEvents(new PlayerListener(), this);
        new MenuBuilderListener(this);
        this.getCommand("intrinsic").setExecutor(new CommandListener(this));

        try {
            setupLogger();
        } catch (IOException e) {
            e.printStackTrace();
        }

        loadMenu(new MainMenu("main-menu"));
        loadMenu(new WhitelistMenu("whitelist-menu"));
    }

    @Override
    public void onDisable() {
        storage.onDisable();
    }

    private void loadRegionTypes() {
        for(String regionName : cfg.getConfigurationSection("region-types").getKeys(false)) {
            ConfigurationSection regionSection = cfg.getConfigurationSection("region-types." + regionName);
            Material material = Material.valueOf(regionSection.getString("material").toUpperCase());
            int radius = regionSection.getInt("radius");

            ItemStack item = new ItemStack(material);
            ItemMeta im = item.getItemMeta();

            if(regionSection.contains("name")) im.setDisplayName(StringUtil.colorize(regionSection.getString("name")));
            if(regionSection.contains("lore")) im.setLore(StringUtil.listFromString(regionSection.getString("lore")));

            item.setItemMeta(im);

            RegionType regionType = new RegionType(regionName, item, radius);

            regionTypes.add(regionType);
        }
    }

    public static List<RegionType> getRegionTypes() {
        return regionTypes;
    }

    /**
     * Check if there is a RegionType that matches an ItemStack
     * @param item to check if the ItemStack of both are similar meaning
     * the block the player is about to place is matching this region type block
     * @return false if not found
     */
    public static boolean hasRegionType(ItemStack item) {
        for(RegionType regionType : getRegionTypes()) {
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

    public static MysqlConnector getStorage() {
        return storage;
    }

    public static RegionContainer getRegionContainer() {
        return regionContainer;
    }

    private void setupLogger() throws IOException, SecurityException {

        FileHandler fh;

        File lf = new File(getDataFolder() + File.separator + "logs" + File.separator + "intrinsic.log");

        lf.getParentFile().mkdirs();
        if(!lf.exists()) lf.createNewFile();

        if (lf.length() > cfg.getLong("log-file-size")) {
            LocalDateTime ct = LocalDateTime.now();
            byte[] buffer = new byte[1024];
            GZIPOutputStream gzos = new GZIPOutputStream(new FileOutputStream(
                    getDataFolder() +  File.separator + "logs" + File.separator + "intrinsic-" + ct.toLocalDate() + ".log.gz"));
            FileInputStream in = new FileInputStream(lf);

            int len;
            while ((len = in.read(buffer)) > 0) {
                gzos.write(buffer, 0, len);
            }

            in.close();

            gzos.finish();
            gzos.close();
            lf.delete();
            lf.createNewFile();
        }

        fh = new FileHandler(getDataFolder() + File.separator + "logs" + File.separator + "intrinsic.log");
        getLogger().addHandler(fh);
        SimpleFormatter fmt = new SimpleFormatter();
        fh.setFormatter(fmt);

    }

    public static Visualizer getVisualizer() {
        return visualizer;
    }

    public static FileConfiguration getIntrinsicConfig() {
        return cfg;
    }

    public static void loadMenu(MenuBuilder menu) {
        menus.put(menu.getName(), menu);
    }

    public static HashMap<String, MenuBuilder> getMenus() {
        return menus;
    }

    public static IntrinsicPlayer adapt(Player player) {
        // Wrap the player with an IntrinsicPlayer
        for(IntrinsicPlayer iPlayer : getOnlineIntrinsicPlayers())
            if(iPlayer.getBase().getUniqueId().equals(player.getUniqueId())) return iPlayer;

        return new IntrinsicPlayer(player);
    }

    public static Collection<IntrinsicPlayer> getOnlineIntrinsicPlayers() {
        return intrinsicPlayers.values();
    }

    public static void addOnlineIntrinsicPlayer(IntrinsicPlayer player) {
        intrinsicPlayers.put(player.getBase().getUniqueId().toString(), player);
    }

    public static void removeIntrinsicPlayer(IntrinsicPlayer player) {
        intrinsicPlayers.remove(player.getBase().getUniqueId().toString());
    }

    /**
     * Reload the config and load everything new again
     */
    public void reload() {
        this.reloadConfig();
        regionTypes.clear();
        loadRegionTypes();
    }

}
