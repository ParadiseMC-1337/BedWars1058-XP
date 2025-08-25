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

package com.andrei1058.bedwars.shop;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.arena.Arena;
import com.andrei1058.bedwars.shop.main.CategoryContent;
import com.andrei1058.bedwars.shop.main.ShopCategory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * 缓存每个玩家的商店购买信息。
 * <p>
 * 这包括玩家已购买的物品层级（tiers），以及在重生时需要处理的永久物品。
 * 每个玩家在加入竞技场时都会有一个对应的 ShopCache 实例。
 */
public class ShopCache {

    private UUID player;
    private List<CachedItem> cachedItems = new LinkedList<>();
    private int selectedCategory;
    // 用于记录某个类别中购买的物品权重，防止购买低级物品覆盖高级物品（例如，不能在有铁剑后购买石剑）
    private HashMap<ShopCategory, Byte> categoryWeight = new HashMap<>();

    private static List<ShopCache> shopCaches = new ArrayList<>();

    /**
     * 为一个玩家创建一个新的商店缓存。
     * @param player 玩家的 UUID。
     */
    public ShopCache(UUID player) {
        this.player = player;
        this.selectedCategory = ShopManager.getShop().getQuickBuyButton().getSlot();
        shopCaches.add(this);
    }

    /**
     * 获取缓存对应的玩家 UUID。
     */
    public UUID getPlayer() {
        return player;
    }

    /**
     * 设置玩家当前在商店中选择的类别。
     * @param slot 类别在商店界面中的槽位。
     */
    public void setSelectedCategory(int slot) {
        this.selectedCategory = slot;
    }

    /**
     * 获取玩家当前选择的类别槽位。
     */
    public int getSelectedCategory() {
        return selectedCategory;
    }

    /**
     * 获取某个物品内容的当前购买层级。
     * @param identifier 物品内容的唯一标识符。
     * @return 当前层级，如果从未购买过则为1。
     */
    public int getContentTier(String identifier) {
        CachedItem ci = getCachedItem(identifier);
        return ci == null ? 1 : ci.getTier();
    }

    /**
     * 根据玩家 UUID 获取对应的商店缓存实例。
     * @param player 玩家的 UUID。
     * @return 如果存在则返回 {@link ShopCache} 实例，否则返回 null。
     */
    public static ShopCache getShopCache(UUID player) {
        for (ShopCache sc : new ArrayList<>(shopCaches)) {
            if (sc.player.equals(player)) return sc;
        }
        return null;
    }

    /**
     * 销毁缓存数据，在玩家离开竞技场时调用。
     */
    public void destroy() {
        shopCaches.remove(this);
        cachedItems.clear();
        cachedItems = null;
        categoryWeight = null;
    }

    /**
     * 在玩家重生时处理永久性和可降级的物品。
     */
    public void managePermanentsAndDowngradables(Arena arena) {
        BedWars.debug("Restore permanents on death for: " + player);
        for (CachedItem ci : cachedItems){
            ci.manageDeath(arena);
        }
    }

    /**
     * 用于追踪商店物品和玩家购买的层级的内部类。
     */
    @SuppressWarnings("WeakerAccess")
    public class CachedItem {
        private CategoryContent cc;
        private int tier = 1;

        /**
         * 构造一个新的缓存物品。
         * @param cc 对应的商店内容。
         */
        public CachedItem(CategoryContent cc) {
            this.cc = cc;
            cachedItems.add(this);
            BedWars.debug("New Cached item " + cc.getIdentifier() + " for player " + player);
        }

        /**
         * 获取当前层级。
         */
        public int getTier() {
            return tier;
        }

        /**
         * 获取对应的商店内容。
         */
        public CategoryContent getCc() {
            return cc;
        }

        /**
         * 在玩家死亡时处理。
         * 如果物品是永久的，则重新给予。
         * 如果物品是可降级的，则降低一级。
         */
        public void manageDeath(Arena arena) {
            if (!cc.isPermanent()) return;
            if (cc.isDowngradable() && tier > 1) tier--;
            BedWars.debug("ShopCache Item Restore: " + cc.getIdentifier() + " for " + player);
            //noinspection ConstantConditions
            cc.giveItems(Bukkit.getPlayer(player), getShopCache(player), arena);
        }

        /**
         * 升级物品层级。
         * @param slot 物品在商店中的槽位，用于更新显示。
         */
        public void upgrade(int slot) {
            tier++;
            Player p = Bukkit.getPlayer(player);
            // 移除背包中旧层级的同类物品
            for (ItemStack i : p.getInventory().getContents()) {
                if (i == null) continue;
                if (i.getType() == Material.AIR) continue;
                if (BedWars.nms.getShopUpgradeIdentifier(i).equals(cc.getIdentifier())) {
                    p.getInventory().remove(i);
                }
            }
            updateItem(slot, p);
            p.updateInventory();
        }

        /**
         * 更新商店界面中该物品的显示。
         */
        public void updateItem(int slot, Player p) {
            if (p.getOpenInventory() != null) {
                if (p.getOpenInventory().getTopInventory() != null) {
                    p.getOpenInventory().getTopInventory().setItem(slot, cc.getItemStack(Bukkit.getPlayer(player), getShopCache(player)));
                }
            }
        }
    }

    /**
     * 根据标识符获取玩家的缓存物品。
     */
    public CachedItem getCachedItem(String identifier) {
        for (CachedItem ci : cachedItems) {
            if (ci.getCc().getIdentifier().equals(identifier)) return ci;
        }
        return null;
    }

    /**
     * 检查玩家是否缓存了某个商店内容。
     */
    public boolean hasCachedItem(CategoryContent cc) {
        for (CachedItem ci : cachedItems) {
            if (ci.getCc() == cc) return true;
        }
        return false;
    }

    /**
     * 根据商店内容对象获取玩家的缓存物品。
     */
    public CachedItem getCachedItem(CategoryContent cc) {
        for (CachedItem ci : cachedItems) {
            if (ci.getCc() == cc) return ci;
        }
        return null;
    }

    /**
     * 升级缓存物品的层级。
     * 如果物品首次购买，则创建一个新的缓存项。
     */
    public void upgradeCachedItem(CategoryContent cc, int slot) {
        CachedItem ci = getCachedItem(cc.getIdentifier());
        if (ci == null) {
            ci = new CachedItem(cc);
            ci.updateItem(slot, Bukkit.getPlayer(player));
        } else {
            // 检查是否还有下一级可以升级
            if (cc.getContentTiers().size() > ci.getTier()) {
                BedWars.debug("Cached item upgrade for " + cc.getIdentifier() + " player " + player);
                ci.upgrade(slot);
            }
        }
    }

    /**
     * 设置某个商店类别的权重。
     * 用于防止购买低级物品替换高级物品。
     * @param sc     商店类别。
     * @param weight 权重值。
     */
    public void setCategoryWeight(ShopCategory sc, byte weight) {
        if (categoryWeight.containsKey(sc)) {
            categoryWeight.replace(sc, weight);
        } else {
            categoryWeight.put(sc, weight);
        }
    }

    /**
     * 获取某个商店类别的权重。
     */
    public byte getCategoryWeight(ShopCategory sc) {
        return categoryWeight.getOrDefault(sc, (byte) 0);
    }

    /**
     * 获取所有永久性的缓存物品。
     */
    public List<CachedItem> getCachedPermanents() {
        List<CachedItem> ci = new ArrayList<>();
        for (CachedItem c : cachedItems){
            if (c.getCc().isPermanent()){
                ci.add(c);
            }
        }
        return ci;
    }

    /**
     * 获取所有缓存物品的列表。
     */
    public List<CachedItem> getCachedItems() {
        return cachedItems;
    }
}
