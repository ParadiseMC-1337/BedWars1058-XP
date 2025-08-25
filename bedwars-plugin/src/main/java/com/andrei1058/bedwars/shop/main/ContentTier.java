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
import com.andrei1058.bedwars.api.arena.shop.IBuyItem;
import com.andrei1058.bedwars.api.arena.shop.IContentTier;
import com.andrei1058.bedwars.api.configuration.ConfigPath;
import com.andrei1058.bedwars.shop.ShopManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static com.andrei1058.bedwars.BedWars.nms;

/**
 * 代表商店内容的单个层级（Tier）。
 * <p>
 * 例如，"剑"这个 {@link CategoryContent} 可能有多个层级：
 * Tier 1: 木剑, Tier 2: 石剑, Tier 3: 铁剑。
 * 这个类定义了每一层级的具体属性，如价格、货币、获得的物品和预览图标。
 */
@SuppressWarnings("WeakerAccess")
public class ContentTier implements IContentTier {

    private int value, price;
    private ItemStack itemStack;
    private Material currency;
    private List<IBuyItem> buyItemsList = new ArrayList<>();
    private boolean loaded = false;

    /**
     * 为一个商店内容创建一个层级。
     * @param path       在配置文件中的路径。
     * @param tierName   层级的名称 (例如 "tier1", "tier2")。
     * @param identifier 所属内容的唯一标识符。
     * @param yml        配置文件实例。
     */
    public ContentTier(String path, String tierName, String identifier, YamlConfiguration yml) {
        BedWars.debug("Loading content tier" + path);

        if (yml.get(path + ConfigPath.SHOP_CONTENT_TIER_ITEM_MATERIAL) == null) {
            BedWars.plugin.getLogger().severe("tier-item material not set at " + path);
            return;
        }

        // 从 "tier1" 中解析出数字 1
        try {
            value = Integer.parseInt(tierName.replace("tier", ""));
        } catch (Exception e) {
            BedWars.plugin.getLogger().severe(path + " doesn't end with a number. It's not recognized as a tier!");
            return;
        }

        if (yml.get(path + ConfigPath.SHOP_CONTENT_TIER_SETTINGS_COST) == null) {
            BedWars.plugin.getLogger().severe("Cost not set for " + path);
            return;
        }
        price = yml.getInt(path + ConfigPath.SHOP_CONTENT_TIER_SETTINGS_COST);

        if (yml.get(path + ConfigPath.SHOP_CONTENT_TIER_SETTINGS_CURRENCY) == null) {
            BedWars.plugin.getLogger().severe("Currency not set for " + path);
            return;
        }

        if (yml.getString(path + ConfigPath.SHOP_CONTENT_TIER_SETTINGS_CURRENCY).isEmpty()) {
            BedWars.plugin.getLogger().severe("Invalid currency at " + path);
            return;
        }

        // 解析货币类型
        switch (yml.getString(path + ConfigPath.SHOP_CONTENT_TIER_SETTINGS_CURRENCY).toLowerCase()) {
            case "iron":
            case "gold":
            case "diamond":
            case "vault":
            case "emerald":
                currency = CategoryContent.getCurrency(yml.getString(path + ConfigPath.SHOP_CONTENT_TIER_SETTINGS_CURRENCY).toLowerCase());
                break;
            default:
                BedWars.plugin.getLogger().severe("Invalid currency at " + path);
                currency = Material.IRON_INGOT;
                break;
        }

        // 创建预览物品
        itemStack = BedWars.nms.createItemStack(yml.getString(path + ConfigPath.SHOP_CONTENT_TIER_ITEM_MATERIAL),
                yml.get(path + ConfigPath.SHOP_CONTENT_TIER_ITEM_AMOUNT) == null ? 1 : yml.getInt(path + ConfigPath.SHOP_CONTENT_TIER_ITEM_AMOUNT),
                (short) (yml.get(path + ConfigPath.SHOP_CONTENT_TIER_ITEM_DATA) == null ? 0 : yml.getInt(path + ConfigPath.SHOP_CONTENT_TIER_ITEM_DATA)));


        if (yml.get(path + ConfigPath.SHOP_CONTENT_TIER_ITEM_ENCHANTED) != null) {
            if (yml.getBoolean(path + ConfigPath.SHOP_CONTENT_TIER_ITEM_ENCHANTED)) {
                itemStack = ShopManager.enchantItem(itemStack);
            }
        }

        // 为预览物品设置药水效果相关的 NBT 标签
        // potion display color based on NBT tag
        if (yml.getString(path + ".tier-item.potion-display") != null && !yml.getString(path + ".tier-item.potion-display").isEmpty()) {
            itemStack = nms.setTag(itemStack, "Potion", yml.getString(path + ".tier-item.potion-display"));
        }
        // 1.16+ custom color
        if (yml.getString(path + ".tier-item.potion-color") != null && !yml.getString(path + ".tier-item.potion-color").isEmpty()) {
            itemStack = nms.setTag(itemStack, "CustomPotionColor", yml.getString(path + ".tier-item.potion-color"));
        }

        if (itemStack != null) {
            itemStack.setItemMeta(ShopManager.hideItemStuff(itemStack.getItemMeta()));
        }

        // 加载购买此层级后实际获得的物品或执行的命令
        IBuyItem bi;
        if (yml.get(path + "." + ConfigPath.SHOP_CONTENT_BUY_ITEMS_PATH) != null) {
            for (String s : yml.getConfigurationSection(path + "." + ConfigPath.SHOP_CONTENT_BUY_ITEMS_PATH).getKeys(false)) {
                bi = new BuyItem(path + "." + ConfigPath.SHOP_CONTENT_BUY_ITEMS_PATH + "." + s, yml, identifier, this);
                if (bi.isLoaded()) buyItemsList.add(bi);
            }
        }
        if (yml.get(path + "." + ConfigPath.SHOP_CONTENT_BUY_CMDS_PATH) != null) {
            bi = new BuyCommand(path + "." + ConfigPath.SHOP_CONTENT_BUY_CMDS_PATH, yml, identifier);
            if (bi.isLoaded()) buyItemsList.add(bi);
        }

        if (buyItemsList.isEmpty()) {
            Bukkit.getLogger().warning("Loaded 0 buy content for: " + path);
        }

        loaded = true;
    }

    /**
     * 获取层级价格。
     */
    public int getPrice() {
        return price;
    }

    /**
     * 获取层级货币。
     */
    public Material getCurrency() {
        return currency;
    }

    /**
     * 设置层级货币。
     */
    public void setCurrency(Material currency) {
        this.currency = currency;
    }

    /**
     * 设置层级价格。
     */
    public void setPrice(int price) {
        this.price = price;
    }

    /**
     * 设置层级预览物品。
     */
    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    /**
     * 设置购买后收到的物品列表。
     */
    public void setBuyItemsList(List<IBuyItem> buyItemsList) {
        this.buyItemsList = buyItemsList;
    }

    /**
     * 获取在商店中显示的预览物品。
     */
    public ItemStack getItemStack() {
        return itemStack;
    }

    /**
     * 获取层级等级（例如 1, 2, 3）。
     */
    public int getValue() {
        return value;
    }

    /**
     * 检查层级是否已成功加载。
     */
    public boolean isLoaded() {
        return loaded;
    }

    /**
     * 获取购买此层级后将收到的物品或执行的命令的列表。
     */
    public List<IBuyItem> getBuyItemsList() {
        return buyItemsList;
    }
}
