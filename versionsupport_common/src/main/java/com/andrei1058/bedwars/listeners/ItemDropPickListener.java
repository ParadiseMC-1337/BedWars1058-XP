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

import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.events.player.PlayerGeneratorCollectEvent;
import com.andrei1058.bedwars.api.server.ServerType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import static com.andrei1058.bedwars.support.version.common.VersionCommon.api;

/**
 * 通用物品监听器，用于处理不同版本下的物品拾取和丢弃事件。
 * 通过内部类来为不同版本的 Minecraft 注册对应的事件处理器。
 */
public class ItemDropPickListener {

    // 适用于 1.11 及更早版本
    public static class PlayerDrop implements Listener {
        @EventHandler
        public void onDrop(PlayerDropItemEvent e){
            if (manageDrop(e.getPlayer(), e.getItemDrop())) e.setCancelled(true);
        }
    }

    // 适用于 1.11 及更早版本
    public static class PlayerPickup implements Listener {
        @SuppressWarnings("deprecation")
        @EventHandler
        public void onPickup(PlayerPickupItemEvent e){
            if (managePickup(e.getItem(), e.getPlayer())) e.setCancelled(true);
        }
    }

    // 适用于 1.13 及更新版本
    public static class EntityDrop implements Listener {
        @EventHandler
        public void onDrop(EntityDropItemEvent e){
            if (manageDrop(e.getEntity(), e.getItemDrop())) e.setCancelled(true);
        }
    }

    // 适用于 1.12 及更新版本
    public static class EntityPickup implements Listener {
        @EventHandler
        public void onPickup(EntityPickupItemEvent e){
            if (managePickup(e.getItem(), e.getEntity())) e.setCancelled(true);
        }
    }

    // 适用于 1.9 及更新版本
    public static class ArrowCollect implements Listener {
        @EventHandler
        public void onArrowPick(PlayerPickupArrowEvent e){
            // 观战者不能捡起箭
            if (api.getArenaUtil().isSpectating(e.getPlayer())) {
                e.setCancelled(true);
            }
        }
    }

    /**
     * 管理物品拾取逻辑。
     * @return 如果事件需要被取消，则返回 true。
     */
    private static boolean managePickup(Item item, LivingEntity player) {
        // 只处理玩家的拾取事件
        if (!(player instanceof Player)) return false;
        if (api.getServerType() == ServerType.MULTIARENA) {
            // 在多竞技场模式下，禁止在大厅世界拾取物品
            //noinspection ConstantConditions
            if (player.getLocation().getWorld().getName().equalsIgnoreCase(api.getLobbyWorld())) {
                return true;
            }
        }
        IArena a = api.getArenaUtil().getArenaByPlayer((Player) player);
        if (a == null) return false;
        // 观战者不能拾取物品
        if (!a.isPlayer((Player) player)) {
            return true;
        }
        // 游戏未开始时不能拾取物品
        if (a.getStatus() != GameState.playing) {
            return true;
        }
        // 正在重生的玩家不能拾取物品
        if (a.getRespawnSessions().containsKey(player)) {
            return true;
        }
        // 对箭进行特殊处理以确保兼容性
        if (item.getItemStack().getType() == Material.ARROW) {
            item.setItemStack(api.getVersionSupport().createItemStack(item.getItemStack().getType().toString(), item.getItemStack().getAmount(), (short) 0));
            return false;
        }

        // 移除掉落的床物品
        if (item.getItemStack().getType().toString().equals("BED")) {
            item.remove();
            return true;
        } else if (item.getItemStack().hasItemMeta()) {
            // 检查物品是否有自定义显示名称，这通常用于标记资源生成器生成的物品
            //noinspection ConstantConditions
            if (item.getItemStack().getItemMeta().hasDisplayName()) {
                if (item.getItemStack().getItemMeta().getDisplayName().contains("custom")) {
                    Material material = item.getItemStack().getType();
                    ItemMeta itemMeta = new ItemStack(material).getItemMeta();

                    // 如果玩家不在挂机状态，则触发并调用玩家收集资源事件
                    if (!api.getAFKUtil().isPlayerAFK(((Player) player).getPlayer())){
                        PlayerGeneratorCollectEvent event = new PlayerGeneratorCollectEvent((Player) player, item, a);
                        Bukkit.getPluginManager().callEvent(event);
                        if (event.isCancelled()) {
                            return true;
                        } else {
                            // 移除自定义名称，使其变为普通物品
                            item.getItemStack().setItemMeta(itemMeta);
                        }
                    }else return true; // 如果玩家挂机，则取消事件
                }
            }
        }
        return false;
    }

    /**
     * 管理物品丢弃逻辑。
     * @return 如果事件需要被取消，则返回 true。
     */
    private static boolean manageDrop(Entity player, Item item) {
        // 只处理玩家的丢弃事件
        if (!(player instanceof Player)) return false;
        if (api.getServerType() == ServerType.MULTIARENA) {
            // 在多竞技场模式下，禁止在大厅世界丢弃物品
            //noinspection ConstantConditions
            if (player.getLocation().getWorld().getName().equalsIgnoreCase(api.getLobbyWorld())) {
                return true;
            }
        }
        IArena a = api.getArenaUtil().getArenaByPlayer((Player) player);
        if (a == null) return false;

        // 观战者不能丢弃物品
        if (!a.isPlayer((Player) player)) {
            return true;
        }

        // 游戏未开始时不能丢弃物品
        if (a.getStatus() != GameState.playing) {
            return true;
        } else {
            ItemStack i = item.getItemStack();
            // 禁止丢弃指南针
            if (i.getType() == Material.COMPASS) {
                return true;
            }
        }

        // 正在重生的玩家不能丢弃物品
        return a.getRespawnSessions().containsKey(player);
    }
}
