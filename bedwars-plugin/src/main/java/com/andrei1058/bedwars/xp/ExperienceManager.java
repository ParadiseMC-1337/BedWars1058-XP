package com.andrei1058.bedwars.xp;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.configuration.ConfigPath;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import static com.andrei1058.bedwars.BedWars.xpConfig;

/**
 * 一个工具类，用于处理与经验值相关的转换和计算。
 */
public class ExperienceManager {

    /**
     * 根据物品的材质（货币类型）获取对应的经验值。
     *
     * @param material 货币的材质，例如铁、金、钻石、绿宝石。
     * @return 对应的经验值。如果材质不是有效的货币类型，则返回0。
     */
    public static int getExperienceFromMaterial(Material material) {
        if (material == null) return 0;

        Material expBottleMaterial = Material.valueOf(BedWars.getForCurrentVersion("EXP_BOTTLE", "EXP_BOTTLE", "EXPERIENCE_BOTTLE"));

        if (material == Material.IRON_INGOT) {
            return xpConfig.getInt(ConfigPath.XP_CONFIG_PATH_IRON);
        } else if (material == Material.GOLD_INGOT) {
            return xpConfig.getInt(ConfigPath.XP_CONFIG_PATH_GOLD);
        } else if (material == expBottleMaterial) {
            // 这可能是经验瓶本身的价值，或者是一个特殊购买项
            return xpConfig.getInt(ConfigPath.XP_CONFIG_PATH_XP_BOTTLE);
        } else if (material == Material.DIAMOND) {
            return xpConfig.getInt(ConfigPath.XP_CONFIG_PATH_DIAMOND);
        } else if (material == Material.EMERALD) {
            return xpConfig.getInt(ConfigPath.XP_CONFIG_PATH_EMERALD);
        } else {
            return 0;
        }
    }
} 