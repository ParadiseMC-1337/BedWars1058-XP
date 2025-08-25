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

package com.andrei1058.bedwars.listeners;

import com.andrei1058.bedwars.api.BedWars;
import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.events.player.PlayerGeneratorCollectEvent;
import com.andrei1058.bedwars.api.server.ServerType;
import com.andrei1058.bedwars.support.version.common.VersionCommon;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * 此类专用于处理 Minecraft 1.8.8 至 1.11 版本的玩家物品拾取和丢弃事件。
 * 对于这些旧版本，使用的是 PlayerPickupItemEvent 和 PlayerDropItemEvent。
 */
public class PlayerDropPick_1_11Minus implements Listener {

    private static BedWars api;

    // 构造函数，用于初始化API实例
    public PlayerDropPick_1_11Minus(BedWars bedWars){
        api = bedWars;
    }

    /* 此类适用于 1.8.8 到 1.11（含）之间的版本 */

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPickup(PlayerPickupItemEvent e) {
        // 在多竞技场模式下，禁止在大厅世界拾取物品
        if (api.getServerType() == ServerType.MULTIARENA) {
            //noinspection ConstantConditions
            if (e.getPlayer().getLocation().getWorld().getName().equalsIgnoreCase(api.getLobbyWorld())) {
                e.setCancelled(true);
                return;
            }
        }

        IArena a = api.getArenaUtil().getArenaByPlayer(e.getPlayer());
        if (a == null) return;

        // 观战者不能拾取物品
        if (!a.isPlayer(e.getPlayer())) {
            e.setCancelled(true);
            return;
        }

        // 游戏未开始时不能拾取物品
        if (a.getStatus() != GameState.playing) {
            e.setCancelled(true);
            return;
        }

        // 正在重生的玩家不能拾取物品
        if (a.getRespawnSessions().containsKey(e.getPlayer())) {
            e.setCancelled(true);
            return;
        }

        // 对箭进行特殊处理以确保兼容性
        if (e.getItem().getItemStack().getType() == Material.ARROW){
            e.getItem().setItemStack(api.getVersionSupport().createItemStack(e.getItem().getItemStack().getType().toString(), e.getItem().getItemStack().getAmount(), (short) 0));
            return;
        }

        // 移除掉落的床物品
        if (VersionCommon.api.getVersionSupport().isBed(e.getItem().getItemStack().getType())) {
            e.setCancelled(true);
            e.getItem().remove();
        } else if (e.getItem().getItemStack().hasItemMeta()) {
            // 检查物品是否有自定义显示名称，这通常用于标记资源生成器生成的物品
            //noinspection ConstantConditions
            if (e.getItem().getItemStack().getItemMeta().hasDisplayName()) {
                if (e.getItem().getItemStack().getItemMeta().getDisplayName().contains("custom")) {
                    Material material = e.getItem().getItemStack().getType();
                    ItemMeta itemMeta = new ItemStack(material).getItemMeta();

                    // 如果玩家不在挂机状态，则触发并调用玩家收集资源事件
                    if (!api.getAFKUtil().isPlayerAFK(e.getPlayer())){
                        PlayerGeneratorCollectEvent event = new PlayerGeneratorCollectEvent(e.getPlayer(), e.getItem(), a);
                        Bukkit.getPluginManager().callEvent(event);
                        if (event.isCancelled()){
                            e.setCancelled(true);
                        } else {
                            // 移除自定义名称，使其变为普通物品
                            e.getItem().getItemStack().setItemMeta(itemMeta);
                        }
                    }
                    else {  // 如果玩家挂机，则取消事件
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        // 在多竞技场模式下，禁止在大厅世界丢弃物品
        if (api.getServerType() == ServerType.MULTIARENA) {
            //noinspection ConstantConditions
            if (e.getPlayer().getLocation().getWorld().getName().equalsIgnoreCase(api.getLobbyWorld())) {
                e.setCancelled(true);
                return;
            }
        }
        IArena a = api.getArenaUtil().getArenaByPlayer(e.getPlayer());
        if (a == null) return;

        // 观战者不能丢弃物品
        if (!a.isPlayer(e.getPlayer())) {
            e.setCancelled(true);
            return;
        }

        // 游戏未开始时不能丢弃物品
        if (a.getStatus() != GameState.playing) {
            e.setCancelled(true);
        } else {
            ItemStack i = e.getItemDrop().getItemStack();
            // 禁止丢弃指南针
            if (i.getType() == Material.COMPASS) {
                e.setCancelled(true);
                return;
            }
        }

        // 正在重生的玩家不能丢弃物品
        if (a.getRespawnSessions().containsKey(e.getPlayer())) {
            e.setCancelled(true);
        }
    }
}
