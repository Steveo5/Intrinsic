package com.hotmail.intrinsic;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class RegionType {

    private String name;
    private ItemStack block;
    private int radius;

    public RegionType(String name, Material mat, int radius) {
        this.name = name;
        this.block = new ItemStack(mat);
        this.radius = radius;
    }

    public RegionType(String name, ItemStack block, int radius) {
        this.name = name;
        this.block = block;
        this.radius = radius;
    }

    public int getRadius() { return this.radius; }

    public ItemStack getBlock() {
        return this.block;
    }

    public String getName() {
        return this.name;
    }

}
