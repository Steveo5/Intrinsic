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
        int minZ = min.getBlockZ();
        int maxZ = max.getBlockZ();

        for (int x = minX; x <= maxX; x++) {
            blocks.add(w.getHighestBlockAt(x, minZ));
            blocks.add(w.getHighestBlockAt(x, maxZ));
            blocks.add(w.getHighestBlockAt(x, minZ));
            blocks.add(w.getHighestBlockAt(x, maxZ));
        }

        for (int z = minZ; z <= maxZ; z++) {
            blocks.add(w.getHighestBlockAt(minX, z));
            blocks.add(w.getHighestBlockAt(minX, z));
            blocks.add(w.getHighestBlockAt(maxX, z));
            blocks.add(w.getHighestBlockAt(maxX, z));
        }

        return blocks;
    }

}
