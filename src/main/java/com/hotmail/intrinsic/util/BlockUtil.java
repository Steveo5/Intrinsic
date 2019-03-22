package com.hotmail.intrinsic.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

import java.util.ArrayList;
import java.util.List;

public class BlockUtil {

    /**
     * Get the block that the player is looking at within
     * a specific range
     * @param player
     * @param range
     * @return
     */
    public static Block getTargetBlock(Player player, int range) {
        BlockIterator iter = new BlockIterator(player, range);
        Block lastBlock = iter.next();
        while (iter.hasNext()) {
            lastBlock = iter.next();
            if (lastBlock.getType() == Material.AIR) {
                continue;
            }
            break;
        }
        return lastBlock;
    }

    /**
     * Get all the outside blocks that are surrounding a rectangle.
     * @param min location
     * @param max location
     * @return
     */
    public static List<Block> getBorderBlocks(Location min, Location max) {
        List<Block> blocks = new ArrayList<Block>();
        World w = min.getWorld();
        int minX = min.getBlockX();
        int maxX = max.getBlockX();
        int minY = min.getBlockY();
        int maxY = max.getBlockY();
        int minZ = min.getBlockZ();
        int maxZ = max.getBlockZ();

        for(int x = minX; x <= maxX; x++) {
            for(int y = minY; y <= maxY; y++) {
                for(int z = minZ; z <= maxZ; z++) {
                    if(x == minX || x == maxX || y == minY || y == maxX || z == minZ || z == maxZ) {
                        Block highest = w.getHighestBlockAt(x, z);
                        blocks.add(highest);
                    }
                }
            }
        }

        return blocks;
    }

}
