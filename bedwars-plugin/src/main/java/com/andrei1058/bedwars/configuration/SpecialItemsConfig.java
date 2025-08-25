package com.andrei1058.bedwars.configuration;

import com.andrei1058.bedwars.api.configuration.ConfigManager;
import com.andrei1058.bedwars.api.configuration.ConfigPath;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class SpecialItemsConfig extends ConfigManager {

    public SpecialItemsConfig(Plugin plugin, String name, String dir) {
        super(plugin, name, dir);

        YamlConfiguration yml = getYml();
        yml.options().header("BedWars0721-XP Special Items Configuration" +
                "\n" +
                "This file is for configuring special items used in the game.");

        yml.addDefault(ConfigPath.SPECIAL_ITEMS_WARP_POWDER_TELEPORT_TIME, 3);
        yml.addDefault(ConfigPath.SPECIAL_ITEMS_WARP_POWDER_DELAY, 15);

        yml.addDefault(ConfigPath.SPECIAL_ITEMS_PROTECTION_WALL_DELAY, 150);
        yml.addDefault(ConfigPath.SPECIAL_ITEMS_PROTECTION_WALL_WIDTH, 5);
        yml.addDefault(ConfigPath.SPECIAL_ITEMS_PROTECTION_WALL_HEIGHT, 3);
        yml.addDefault(ConfigPath.SPECIAL_ITEMS_PROTECTION_WALL_DISTANCE, 2);
        yml.addDefault(ConfigPath.SPECIAL_ITEMS_PROTECTION_WALL_MATERIAL, "CUT_SANDSTONE");

        yml.addDefault(ConfigPath.SPECIAL_ITEMS_RESCUE_PLATFORM_DELAY, 15);
        yml.addDefault(ConfigPath.SPECIAL_ITEMS_RESCUE_PLATFORM_BREAK_TIME, 150);
        yml.addDefault(ConfigPath.SPECIAL_ITEMS_RESCUE_PLATFORM_MATERIAL, "SLIME_BLOCK");

        yml.options().copyDefaults(true);
        save();
    }
} 