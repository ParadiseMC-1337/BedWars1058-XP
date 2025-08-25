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
import com.andrei1058.bedwars.api.configuration.ConfigPath;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.shop.ShopCache;
import com.andrei1058.bedwars.shop.ShopManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.andrei1058.bedwars.BedWars.nms;

/**
 * 代表商店中的一个商品类别，例如"方块"、"武器"等。
 * 这个类负责加载该类别下的所有内容 ({@link CategoryContent})，
 * 并处理当玩家点击该类别时，打开对应的商品列表界面的逻辑。
 */
public class ShopCategory {

    private int slot;
    private ItemStack itemStack;
    private String itemNamePath, itemLorePath, invNamePath;
    private boolean loaded = false;
    private final List<CategoryContent> categoryContentList = new ArrayList<>();
    public static List<UUID> categoryViewers = new ArrayList<>();
    private final String name;

    /**
     * 从给定的配置文件路径加载一个商店类别。
     * @param path 在配置文件中的路径。
     * @param yml  配置文件实例。
     */
    public ShopCategory(String path, YamlConfiguration yml) {
        BedWars.debug("Loading shop category: " + path);
        this.name = path;

        if (yml.get(path + ConfigPath.SHOP_CATEGORY_ITEM_MATERIAL) == null) {
            BedWars.plugin.getLogger().severe("Category material not set at: " + path);
            return;
        }

        if (yml.get(path + ConfigPath.SHOP_CATEGORY_SLOT) == null) {
            BedWars.plugin.getLogger().severe("Category slot not set at: " + path);
            return;
        }
        slot = yml.getInt(path + ConfigPath.SHOP_CATEGORY_SLOT);

        // 检查槽位是否有效且未被占用
        if (slot < 1 || slot > 8) {
            BedWars.plugin.getLogger().severe("Slot must be n > 1 and n < 9 at: " + path);
            return;
        }

        for (ShopCategory sc : ShopManager.shop.getCategoryList()){
            if (sc.getSlot() == slot){
                BedWars.plugin.getLogger().severe("Slot is already in use at: " + path);
                return;
            }
        }

        // 创建类别预览物品
        itemStack = BedWars.nms.createItemStack(yml.getString(path + ConfigPath.SHOP_CATEGORY_ITEM_MATERIAL),
                yml.get(path + ConfigPath.SHOP_CATEGORY_ITEM_AMOUNT) == null ? 1 : yml.getInt(path + ConfigPath.SHOP_CATEGORY_ITEM_AMOUNT),
                (short) (yml.get(path + ConfigPath.SHOP_CATEGORY_ITEM_DATA) == null ? 0 : yml.getInt(path + ConfigPath.SHOP_CATEGORY_ITEM_DATA)));


        if (yml.get(path + ConfigPath.SHOP_CATEGORY_ITEM_ENCHANTED) != null) {
            if (yml.getBoolean(path + ConfigPath.SHOP_CATEGORY_ITEM_ENCHANTED)) {
                itemStack = ShopManager.enchantItem(itemStack);
            }
        }

        // 为预览物品设置药水相关的 NBT 标签
        // potion display color based on NBT tag
        if (yml.getString(path + ".category-item.potion-display") != null && !yml.getString(path + ".category-item.potion-display").isEmpty()) {
            itemStack = nms.setTag(itemStack, "Potion", yml.getString(path + ".category-item.potion-display"));
        }
        // 1.16+ custom color
        if (yml.getString(path + ".category-item.potion-color") != null && !yml.getString(path + ".category-item.potion-color").isEmpty()) {
            itemStack = nms.setTag(itemStack, "CustomPotionColor", yml.getString(path + ".category-item.potion-color"));
        }

        if (itemStack.getItemMeta() != null) {
            itemStack.setItemMeta(ShopManager.hideItemStuff(itemStack.getItemMeta()));
        }

        // 初始化语言文件路径
        itemNamePath = Messages.SHOP_CATEGORY_ITEM_NAME.replace("%category%", path);
        itemLorePath = Messages.SHOP_CATEGORY_ITEM_LORE.replace("%category%", path);
        invNamePath = Messages.SHOP_CATEGORY_INVENTORY_NAME.replace("%category%", path);
        loaded = true;

        // 加载该类别下的所有内容
        CategoryContent cc;
        for (String s : yml.getConfigurationSection(path + "." + ConfigPath.SHOP_CATEGORY_CONTENT_PATH).getKeys(false)) {
            cc = new CategoryContent(path + ConfigPath.SHOP_CATEGORY_CONTENT_PATH + "." + s, s, path, yml, this);
            if (cc.isLoaded()) {
                categoryContentList.add(cc);
                BedWars.debug("Adding CategoryContent: " + s + " to Shop Category: " + path);
            }
        }
    }

    /**
     * 为玩家打开此类别界面。
     * @param player    目标玩家。
     * @param index     主商店界面实例。
     * @param shopCache 玩家的商店缓存。
     */
    public void open(Player player, ShopIndex index, ShopCache shopCache){
        if (player.getOpenInventory().getTopInventory() == null) return;
        ShopIndex.indexViewers.remove(player.getUniqueId());

        Inventory inv = Bukkit.createInventory(null, index.getInvSize(), Language.getMsg(player, invNamePath));

        // 重新添加主界面的按钮和分隔符
        inv.setItem(index.getQuickBuyButton().getSlot(), index.getQuickBuyButton().getItemStack(player));

        for (ShopCategory sc : index.getCategoryList()) {
            inv.setItem(sc.getSlot(), sc.getItemStack(player));
        }

        index.addSeparator(player, inv);

        // 添加表示当前选定类别的指示器
        inv.setItem(getSlot() + 9, index.getSelectedItem(player));

        shopCache.setSelectedCategory(getSlot());

        // 添加该类别下的所有商品内容
        for (CategoryContent cc : getCategoryContentList()) {
            inv.setItem(cc.getSlot(), cc.getItemStack(player, shopCache));
        }

        player.openInventory(inv);
        if (!categoryViewers.contains(player.getUniqueId())){
            categoryViewers.add(player.getUniqueId());
        }
    }

    /**
     * 获取为特定玩家生成的、带有本地化文本的类别预览物品。
     * @param player 目标玩家。
     * @return 带有正确名称和 lore 的 ItemStack。
     */
    public ItemStack getItemStack(Player player) {
        ItemStack i = itemStack.clone();
        ItemMeta im = i.getItemMeta();
        if (im != null) {
            im.setDisplayName(Language.getMsg(player, itemNamePath));
            im.setLore(Language.getList(player, itemLorePath));
            i.setItemMeta(im);
        }
        return i;
    }

    /**
     * 检查类别是否已成功加载。
     */
    public boolean isLoaded() {
        return loaded;
    }

    /**
     * 获取类别在商店主界面中的槽位。
     */
    public int getSlot() {
        return slot;
    }

    /**
     * 获取该类别下的所有商品内容列表。
     */
    public List<CategoryContent> getCategoryContentList() {
        return categoryContentList;
    }

    /**
     * 根据标识符从所有类别中获取一个商品内容。
     * @param identifier 商品内容的唯一标识符。
     * @param shopIndex  主商店界面实例。
     * @return 如果找到则返回 {@link CategoryContent}，否则返回 null。
     */
    public static CategoryContent getCategoryContent(String identifier, ShopIndex shopIndex){
        for (ShopCategory sc : shopIndex.getCategoryList()){
            for (CategoryContent cc : sc.getCategoryContentList()){
                if (cc.getIdentifier().equals(identifier)) return cc;
            }
        }
        return null;
    }

    /**
     * 获取类别的名称（通常是其在配置文件中的路径）。
     */
    public String getName() {
        return name;
    }

    /**
     * 获取当前正在查看此类别界面的玩家列表。
     */
    public static List<UUID> getCategoryViewers() {
        return new ArrayList<>(categoryViewers);
    }
}
