package com.andrei1058.bedwars.configuration;

import com.andrei1058.bedwars.api.configuration.ConfigManager;
import com.andrei1058.bedwars.api.configuration.ConfigPath;
import org.bukkit.plugin.Plugin;
import org.bukkit.configuration.file.YamlConfiguration;

public class XpConfig extends ConfigManager {

    public XpConfig(Plugin plugin, String name, String dir) {
        super(plugin, name, dir);

        YamlConfiguration yml = getYml();
        yml.options().header("BedWars1058-XP by Stu.");
        yml.addDefault(ConfigPath.XP_CONFIG_PATH_IRON, 1);
        yml.addDefault(ConfigPath.XP_CONFIG_PATH_GOLD, 10);
        yml.addDefault(ConfigPath.XP_CONFIG_PATH_DIAMOND, 0);
        yml.addDefault(ConfigPath.XP_CONFIG_PATH_EMERALD, 100);
        yml.addDefault(ConfigPath.XP_CONFIG_PATH_XP_BOTTLE, 10);
        yml.options().copyDefaults(true);
        save();
    }
} 