package com.hotmail.intrinsic;

import com.hotmail.intrinsic.listener.RegionCreateListener;
import com.hotmail.intrinsic.storage.MysqlConnector;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.*;
import java.util.zip.GZIPOutputStream;

import static org.bukkit.Bukkit.getPluginManager;

public class Intrinsic extends JavaPlugin {

    private static List<RegionType> regionTypes = new ArrayList<RegionType>();
    private static FileConfiguration cfg;
    private static Logger logger;
    private static RegionContainer regionContainer;
    private static MysqlConnector storage;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        cfg = this.getConfig();
        regionContainer = new RegionContainer();
        storage = new MysqlConnector(this);

        if(!storage.testConnection()) {
            getLogger().log(Level.SEVERE, "MySQL connection failed, plugin will shutdown and nothing is protected!");
        } else {
            getLogger().log(Level.INFO, "MySQL Connection succeeded!");
        }

        RegionType small = new RegionType("small-protection", Material.STONE, 0);
        regionTypes.add(small);

        getPluginManager().registerEvents(new RegionCreateListener(this), this);

        try {
            setupLogger();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        storage.onDisable();
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

}
