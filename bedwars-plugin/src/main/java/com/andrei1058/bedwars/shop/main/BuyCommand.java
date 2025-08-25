/*
 * BedWars1058 - A bed wars mini-game.
 * Copyright (C) 2021 Andrei Dascălu
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Contact e-mail: andrew.dascalu@gmail.com
 */

package com.andrei1058.bedwars.shop.main;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.shop.IBuyItem;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.language.Language;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 代表一个在购买时会执行预设命令的商店物品。
 * 这允许商店物品触发插件内部或外部的各种操作。
 */
public class BuyCommand implements IBuyItem {

    private final List<String> asPlayer = new ArrayList<>();
    private final List<String> asConsole = new ArrayList<>();
    private final String upgradeIdentifier;

    /**
     * 从配置文件中加载一个购买命令项。
     * @param path              在配置文件中的路径。
     * @param yml               配置文件实例。
     * @param upgradeIdentifier 升级标识符，用于跟踪物品升级。
     */
    public BuyCommand(String path, YamlConfiguration yml, String upgradeIdentifier) {
        BedWars.debug("Loading BuyCommand: " + path);
        this.upgradeIdentifier = upgradeIdentifier;
        // 加载控制台执行的命令
        for (String cmd : yml.getStringList(path + ".as-console")) {
            if (cmd.startsWith("/")) {
                cmd = cmd.replaceFirst("/", "");
            }
            asConsole.add(cmd);
        }
        // 加载玩家执行的命令
        for (String cmd : yml.getStringList(path + ".as-player")) {
            if (!cmd.startsWith("/")) {
                cmd = "/" + cmd;
            }
            asPlayer.add(cmd);
        }
    }

    @Override
    public boolean isLoaded() {
        return true;
    }

    /**
     * 当玩家购买此物品时执行命令。
     * @param player 购买的玩家。
     * @param arena  玩家所在的竞技场。
     */
    @Override
    public void give(Player player, IArena arena) {
        BedWars.debug("Giving BuyCMD: " + getUpgradeIdentifier() + " to: " + player.getName());
        String playerName = player.getName();
        String playerUUID = player.getUniqueId().toString();
        ITeam team = arena.getTeam(player);
        String teamName = team == null ? "null" : team.getName();
        String teamDisplay = team == null ? "null" : team.getDisplayName(Language.getPlayerLanguage(player));
        String teamColor = team == null ? ChatColor.WHITE.toString() : team.getColor().chat().toString();
        String arenaIdentifier = arena.getArenaName();
        String arenaWorld = arena.getWorldName();
        String arenaDisplay = arena.getDisplayName();
        String arenaGroup = arena.getGroup();

        // 替换占位符并以玩家身份执行命令
        for (String playerCmd : asPlayer) {
            player.chat(playerCmd.replace("{player}", playerName)
                    .replace("{player_uuid}", playerUUID)
                    .replace("{team}", teamName).replace("{team_display}", teamDisplay)
                    .replace("{team_color}", teamColor).replace("{arena}", arenaIdentifier)
                    .replace("{arena_world}", arenaWorld).replace("{arena_display}", arenaDisplay)
                    .replace("{arena_group}", arenaGroup));
        }
        // 替换占位符并以控制台身份执行命令
        for (String consoleCmd : asConsole) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), consoleCmd
                    .replace("{player}", playerName).replace("{player_uuid}", playerUUID)
                    .replace("{team}", teamName).replace("{team_display}", teamDisplay)
                    .replace("{team_color}", teamColor).replace("{arena}", arenaIdentifier)
                    .replace("{arena_world}", arenaWorld).replace("{arena_display}", arenaDisplay)
                    .replace("{arena_group}", arenaGroup));
        }
    }

    @Override
    public String getUpgradeIdentifier() {
        return upgradeIdentifier;
    }

    // 这些方法对于命令物品没有实际意义，但需要实现接口。
    @Override
    public ItemStack getItemStack() {
        return null;
    }

    @Override
    public void setItemStack(ItemStack itemStack) {
        // 命令物品没有 ItemStack
    }

    @Override
    public boolean isAutoEquip() {
        return false;
    }

    @Override
    public void setAutoEquip(boolean autoEquip) {
        // 命令物品不能被装备
    }

    @Override
    public boolean isPermanent() {
        return false;
    }

    @Override
    public void setPermanent(boolean permanent) {
        // 命令物品不是永久性的
    }

    @Override
    public boolean isUnbreakable() {
        return false;
    }

    @Override
    public void setUnbreakable(boolean unbreakable) {
        // 命令物品没有耐久度
    }
}
