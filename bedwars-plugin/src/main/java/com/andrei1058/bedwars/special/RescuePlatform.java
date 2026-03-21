/**
 * @author jiuxian_baka
 * @since 2.0
 *
 * This handles the rescue platform special item.
 */
package com.andrei1058.bedwars.special;

import com.andrei1058.bedwars.BedWars;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RescuePlatform {

    private static final Set<Location> platformBlockLocations = new HashSet<>();

    public RescuePlatform(Player player, double breakTime, Material platformMaterial) {
        Block centerBlock = player.getLocation().getBlock().getRelative(0, -1, 0);
        int[][] platformPattern = {
                {0, 1, 0, 1, 0},
                {1, 1, 1, 1, 1},
                {0, 1, 1, 1, 0},
                {1, 1, 1, 1, 1},
                {0, 1, 0, 1, 0}
        };
        List<Block> currentPlatformBlocks = new ArrayList<>();

        for (int x = 0; x < platformPattern.length; x++) {
            for (int z = 0; z < platformPattern[x].length; z++) {
                if (platformPattern[x][z] != 1) {
                    continue;
                }

                Block block = centerBlock.getRelative(x - 2, 0, z - 2);
                block.setType(platformMaterial);
                currentPlatformBlocks.add(block);
                platformBlockLocations.add(block.getLocation());
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Block block : currentPlatformBlocks) {
                    if (block != null && block.getType() == platformMaterial) {
                        block.setType(Material.AIR);
                    }
                    if (block != null) {
                        platformBlockLocations.remove(block.getLocation());
                    }
                }
            }
        }.runTaskLater(BedWars.plugin, (long) breakTime);
    }

    public static boolean canCreatePlatform(Player player) {
        com.andrei1058.bedwars.api.arena.IArena arena = com.andrei1058.bedwars.arena.Arena.getArenaByPlayer(player);
        if (arena == null) {
            return false;
        }

        Block centerBlock = player.getLocation().getBlock().getRelative(0, -1, 0);
        int[][] platformPattern = {
                {0, 1, 0, 1, 0},
                {1, 1, 1, 1, 1},
                {0, 1, 1, 1, 0},
                {1, 1, 1, 1, 1},
                {0, 1, 0, 1, 0}
        };

        for (int x = 0; x < platformPattern.length; x++) {
            for (int z = 0; z < platformPattern[x].length; z++) {
                if (platformPattern[x][z] != 1) {
                    continue;
                }

                Block block = centerBlock.getRelative(x - 2, 0, z - 2);
                if (block.getType() != Material.AIR) {
                    return false;
                }
                if (arena.isProtected(block.getLocation())) {
                    return false;
                }
            }
        }
        return true;
    }

    @Deprecated
    public static void createRescuePlatform(Player player, double breakTime) {
        new RescuePlatform(player, breakTime, Material.SLIME_BLOCK);
    }

    public static boolean isRescuePlatformBlock(Block checkBlock) {
        return checkBlock != null && platformBlockLocations.contains(checkBlock.getLocation());
    }
}
