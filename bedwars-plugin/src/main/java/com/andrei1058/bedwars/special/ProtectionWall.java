package com.andrei1058.bedwars.special;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.configuration.ConfigPath;
import com.andrei1058.bedwars.api.language.Messages;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;

import static com.andrei1058.bedwars.api.language.Language.getMsg;

/**
 * 防守墙特殊物品
 * <p>
 * 该类用于处理游戏中的防守墙特殊道具。
 * 防守墙会在玩家面前生成一道临时的墙体，用于阻挡敌人的攻击。
 * </p>
 */
public class ProtectionWall {

    /**
     * 八个基本方向枚举
     */
    public enum CardinalDirection {
        NORTH,
        NORTH_EAST,
        EAST,
        SOUTH_EAST,
        SOUTH,
        SOUTH_WEST,
        WEST,
        NORTH_WEST,
        SELF
    }

    /**
     * 获取玩家朝向的基本方向
     * 
     * @param location 玩家位置
     * @return 基本方向
     */
    private static CardinalDirection getCardinalDirection(Location location) {
        double rotation = (location.getYaw() - 90) % 360;
        if (rotation < 0) {
            rotation += 360.0;
        }
        
        if (0 <= rotation && rotation < 22.5) {
            return CardinalDirection.NORTH;
        } else if (22.5 <= rotation && rotation < 67.5) {
            return CardinalDirection.NORTH_EAST;
        } else if (67.5 <= rotation && rotation < 112.5) {
            return CardinalDirection.EAST;
        } else if (112.5 <= rotation && rotation < 157.5) {
            return CardinalDirection.SOUTH_EAST;
        } else if (157.5 <= rotation && rotation < 202.5) {
            return CardinalDirection.SOUTH;
        } else if (202.5 <= rotation && rotation < 247.5) {
            return CardinalDirection.SOUTH_WEST;
        } else if (247.5 <= rotation && rotation < 292.5) {
            return CardinalDirection.WEST;
        } else if (292.5 <= rotation && rotation < 337.5) {
            return CardinalDirection.NORTH_WEST;
        } else {
            return CardinalDirection.NORTH;
        }
    }

    private static final Map<UUID, List<ProtectionWall>> activeWalls = new HashMap<>();

    // Getters
    @Getter
    private final Player player;
    @Getter
    private final IArena arena;
    @Getter
    private final ITeam team;
    private final List<Block> wallBlocks;
    private final BukkitTask task;
    @Getter
    private final Material wallMaterial;
    @Getter
    private int width;
    @Getter
    private final int height;
    @Getter
    private final int distance;
    @Getter
    private final double breakTime;
    @Getter
    private final boolean breakable;
    @Getter
    private boolean destroyed = false;

    /**
     * 创建一个新的防守墙实例
     * 
     * @param player 使用防守墙的玩家
     * @param arena 玩家所在的竞技场
     * @param width 墙体宽度
     * @param height 墙体高度
     * @param distance 墙体与玩家的距离
     * @param material 墙体材质
     */
    public ProtectionWall(Player player, IArena arena, int width, int height, int distance, Material material) {
        this.player = player;
        this.arena = arena;
        this.team = arena.getTeam(player);
        this.wallBlocks = new ArrayList<>();
        this.width = width;
        this.height = height;
        this.distance = distance;
        this.breakTime = 0; // 永不自动销毁
        this.breakable = true; // 强制可破坏
        this.wallMaterial = material;
        
        // 添加到活跃墙体列表
        activeWalls.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>()).add(this);
        
        // 创建墙体
        createWall();
        
        // 防守墙永不自动销毁，不启动定时任务
        this.task = null;

    }

    /**
     * 创建墙体
     */
    private void createWall() {
        if (width % 2 == 0) {
            width = width + 1;
            if (width % 2 == 0) {
                return;
            }
        }

        Location wallLocation = player.getLocation();
        Vector facingDirection = wallLocation.getDirection().clone();
        facingDirection.setY(0);
        facingDirection.normalize();
        wallLocation = wallLocation.add(facingDirection.multiply(distance));

        CardinalDirection face = getCardinalDirection(player.getLocation());
        int widthStart = (int) Math.floor(((double) width) / 2.0);

        for (int w = widthStart * (-1); w < width - widthStart; w++) {
            for (int h = 0; h < height; h++) {
                Location wallBlock = wallLocation.clone();

                switch (face) {
                    case SOUTH:
                    case NORTH:
                    case SELF:
                        wallBlock = wallBlock.add(0, h, w);
                        break;
                    case WEST:
                    case EAST:
                        wallBlock = wallBlock.add(w, h, 0);
                        break;
                    case SOUTH_EAST:
                        wallBlock = wallBlock.add(w, h, w);
                        break;
                    case SOUTH_WEST:
                        wallBlock = wallBlock.add(w, h, w * (-1));
                        break;
                    case NORTH_EAST:
                        wallBlock = wallBlock.add(w * (-1), h, w);
                        break;
                    case NORTH_WEST:
                        wallBlock = wallBlock.add(w * (-1), h, w * (-1));
                        break;
                    default:
                        wallBlock = null;
                        break;
                }

                if (wallBlock == null) {
                    continue;
                }

                Block placedBlock = wallBlock.getBlock();
                if (!placedBlock.getType().equals(Material.AIR)) {
                    continue;
                }

                // 检查是否在保护区域内
                if (arena.isProtected(placedBlock.getLocation())) {
                    continue;
                }

                placedBlock.setType(wallMaterial);
                wallBlocks.add(placedBlock);
                
                // 将方块添加到竞技场的已放置方块列表
                arena.addPlacedBlock(placedBlock);
            }
        }
    }

    /**
     * 检查是否可以创建防守墙
     * 
     * @param player 玩家
     * @param width 墙体宽度
     * @param height 墙体高度
     * @param distance 距离
     * @return 是否可以创建（至少有一个方块可以放置）
     */
    public static boolean canCreateWall(Player player, int width, int height, int distance) {
        com.andrei1058.bedwars.api.arena.IArena arena = com.andrei1058.bedwars.arena.Arena.getArenaByPlayer(player);
        if (arena == null) return false;
        
        int actualWidth = width;
        if (actualWidth % 2 == 0) {
            actualWidth = actualWidth + 1;
            if (actualWidth % 2 == 0) {
                return false;
            }
        }

        Location wallLocation = player.getLocation();
        Vector facingDirection = wallLocation.getDirection().clone();
        facingDirection.setY(0);
        facingDirection.normalize();
        wallLocation = wallLocation.add(facingDirection.multiply(distance));

        CardinalDirection face = getCardinalDirection(player.getLocation());
        int widthStart = (int) Math.floor(((double) actualWidth) / 2.0);

        for (int w = widthStart * (-1); w < actualWidth - widthStart; w++) {
            for (int h = 0; h < height; h++) {
                Location wallBlock = wallLocation.clone();

                switch (face) {
                    case SOUTH:
                    case NORTH:
                    case SELF:
                        wallBlock = wallBlock.add(0, h, w);
                        break;
                    case WEST:
                    case EAST:
                        wallBlock = wallBlock.add(w, h, 0);
                        break;
                    case SOUTH_EAST:
                        wallBlock = wallBlock.add(w, h, w);
                        break;
                    case SOUTH_WEST:
                        wallBlock = wallBlock.add(w, h, w * (-1));
                        break;
                    case NORTH_EAST:
                        wallBlock = wallBlock.add(w * (-1), h, w);
                        break;
                    case NORTH_WEST:
                        wallBlock = wallBlock.add(w * (-1), h, w * (-1));
                        break;
                    default:
                        wallBlock = null;
                        break;
                }

                if (wallBlock == null) {
                    continue;
                }

                Block placedBlock = wallBlock.getBlock();
                if (!placedBlock.getType().equals(Material.AIR)) {
                    continue;
                }

                // 检查是否在保护区域内
                if (arena.isProtected(placedBlock.getLocation())) {
                    continue;
                }

                // 如果找到至少一个可以放置的位置，返回true
                return true;
            }
        }
        
        return false;
    }

    /**
     * 检查指定方块是否为防守墙方块
     * 
     * @param block 要检查的方块
     * @return 是否为防守墙方块
     */
    public static boolean isProtectionWallBlock(Block block) {
        for (List<ProtectionWall> walls : activeWalls.values()) {
            for (ProtectionWall wall : walls) {
                if (wall.wallBlocks.contains(block)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取包含指定方块的防守墙
     * 
     * @param block 要检查的方块
     * @return 包含该方块的防守墙，如果没有则返回null
     */
    public static ProtectionWall getWallByBlock(Block block) {
        for (List<ProtectionWall> walls : activeWalls.values()) {
            for (ProtectionWall wall : walls) {
                if (wall.wallBlocks.contains(block)) {
                    return wall;
                }
            }
        }
        return null;
    }

    public List<Block> getWallBlocks() {
        return new ArrayList<>(wallBlocks);
    }

}