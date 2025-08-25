/**
 * @author jiuxian_baka
 * @since 2.0
 * <p>
 * This handles the rescue platform special item.
 * <p>
 * The rescue platform is a special item that creates a temporary platform under the player.
 * It is used to prevent players from falling into the void.
 */
package com.andrei1058.bedwars.special;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.configuration.ConfigPath;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 救援平台
 * <p>
 * 该类用于处理游戏中的救援平台特殊道具.
 * 救援平台会在玩家脚下生成一个临时平台，防止玩家掉入虚空.
 * </p>
 */
public class RescuePlatform {

    private static final Set<Location> platformBlockLocations = new HashSet<>();

    /**
     * 创建一个新的救援平台实例.
     * <p>
     * 该构造函数会立即在玩家脚下生成一个平台.
     * 调用前应先使用 {@link #canCreatePlatform(Player)} 进行检查.
     * </p>
     *
     * @param player    救援平台的目标玩家.
     * @param breakTime 平台自动消失的时间 (以tick为单位).
     */
    public RescuePlatform(Player player, double breakTime) {
        // 获取玩家脚下-1格的方块作为中心
        Block centerBlock = player.getLocation().getBlock().getRelative(0, -1, 0);
        // 救援平台的图案
        int[][] platformPattern = {{0, 1, 0, 1, 0}, {1, 1, 1, 1, 1}, {0, 1, 1, 1, 0}, {1, 1, 1, 1, 1}, {0, 1, 0, 1, 0}};
        // 用于存储生成的平台方块
        List<Block> currentPlatformBlocks = new ArrayList<>();
        Material platformMaterial = Material.valueOf(BedWars.specialItemsConfig.getYml().getString(ConfigPath.SPECIAL_ITEMS_RESCUE_PLATFORM_MATERIAL));
        // 遍历图案并生成平台
        for (int x = 0; x < platformPattern.length; x++) {
            for (int z = 0; z < (platformPattern[x]).length; z++) {
                Block block = centerBlock.getRelative(x - 2, 0, z - 2);
                if (platformPattern[x][z] == 1) {
                    // 设置为平台方块
                    block.setType(platformMaterial);
                    currentPlatformBlocks.add(block);
                    platformBlockLocations.add(block.getLocation());
                } else {
                    // 设置为空气
                    block.setType(Material.AIR);
                }
            }
        }
        // 创建一个延迟任务来移除平台
        (new BukkitRunnable() {
            public void run() {
                // 遍历所有记录的方块
                for (Block block : currentPlatformBlocks) {
                    // 如果方块是平台物块，就把它变回空气
                    if (block != null && block.getType() == platformMaterial) {
                        block.setType(Material.AIR);
                    }
                    platformBlockLocations.remove(block.getLocation());
                }
            }
        }).runTaskLater(BedWars.plugin, (long) breakTime); // 150 ticks = 7.5 seconds
    }

    /**
     * 检查是否可以在玩家下方创建平台.
     * <p>
     * 它会检查预设的平台图案区域内是否都是空气方块，以及是否有任何方块在保护区域内.
     * </p>
     *
     * @param player 要检查的玩家.
     * @return 如果可以创建平台则返回 true, 否则返回 false.
     */
    public static boolean canCreatePlatform(Player player) {
        com.andrei1058.bedwars.api.arena.IArena arena = com.andrei1058.bedwars.arena.Arena.getArenaByPlayer(player);
        if (arena == null) return false;
        
        // 获取玩家脚下-1格的方块作为中心
        Block centerBlock = player.getLocation().getBlock().getRelative(0, -1, 0);
        // 救援平台的图案，1代表平台方块，0代表空气
        int[][] platformPattern = {{0, 1, 0, 1, 0}, {1, 1, 1, 1, 1}, {0, 1, 1, 1, 0}, {1, 1, 1, 1, 1}, {0, 1, 0, 1, 0}};
        // 遍历图案
        for (int x = 0; x < platformPattern.length; x++) {
            for (int z = 0; z < (platformPattern[x]).length; z++) {
                // 计算实际的方块位置
                Block block = centerBlock.getRelative(x - 2, 0, z - 2);
                // 如果图案要求是平台方块，检查是否可以放置
                if (platformPattern[x][z] == 1) {
                    // 检查是否为空气方块
                    if (!block.getType().equals(Material.AIR)) {
                        return false;
                    }
                    // 检查是否在保护区域内
                    if (arena.isProtected(block.getLocation())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * 创建救援平台.
     * <p>
     * 该方法会在玩家脚下根据预设图案生成一个由粘液块组成的平台.
     * 平台会在一定时间后自动消失.
     * </p>
     *
     * @param player 救援平台的目标玩家.
     * @param breakTime 平台自动消失的时间 (以tick为单位).
     * @deprecated 请改用 {@link #RescuePlatform(Player, double)} 构造函数.
     */
    @Deprecated
    public static void createRescuePlatform(Player player, double breakTime) {
        new RescuePlatform(player, breakTime);
    }

    public static boolean isRescuePlatformBlock(Block checkBlock) {
        if (checkBlock == null) {
            return false;
        }
        return platformBlockLocations.contains(checkBlock.getLocation());
    }
}
