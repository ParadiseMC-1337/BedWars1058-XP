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
import com.andrei1058.bedwars.api.events.shop.ShopOpenEvent;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.arena.Arena;
import com.andrei1058.bedwars.shop.ShopCache;
import com.andrei1058.bedwars.shop.quickbuy.PlayerQuickBuyCache;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 代表商店的主界面 (GUI)。
 * 这个类负责组合所有的商品类别 ({@link ShopCategory})、快速购买按钮和分隔符，
 * 并为玩家构建和显示完整的商店窗口。
 */
@SuppressWarnings("WeakerAccess")
public class ShopIndex {

    private int invSize = 54;
    private String namePath, separatorNamePath, separatorLorePath;
    private List<ShopCategory> categoryList = new ArrayList<>();
    private QuickBuyButton quickBuyButton;
    public ItemStack separatorSelected, separatorStandard;

    public static List<UUID> indexViewers = new ArrayList<>();


    /**
     * 创建一个商店主界面实例。
     *
     * @param namePath          商店GUI标题的语言路径。
     * @param quickBuyButton    快速购买按钮 ({@link QuickBuyButton})。
     * @param separatorNamePath 分隔符物品名称的语言路径。
     * @param separatorLorePath 分隔符物品Lore的语言路径。
     * @param separatorSelected 用于标记当前选中类别的分隔符物品。
     * @param separatorStandard 标准的分隔符物品。
     */
    public ShopIndex(String namePath, QuickBuyButton quickBuyButton, String separatorNamePath, String separatorLorePath, ItemStack separatorSelected, ItemStack separatorStandard) {
        this.namePath = namePath;
        this.separatorLorePath = separatorLorePath;
        this.separatorNamePath = separatorNamePath;
        this.quickBuyButton = quickBuyButton;
        this.separatorStandard = separatorStandard;
        this.separatorSelected = separatorSelected;
    }

    /**
     * 为玩家打开商店主界面。
     *
     * @param player        目标玩家。
     * @param quickBuyCache 玩家的快速购买缓存。
     * @param callEvent     是否触发 {@link ShopOpenEvent} 事件。
     */
    public void open(Player player, PlayerQuickBuyCache quickBuyCache, boolean callEvent) {

        if (quickBuyCache == null) return;

        if (callEvent) {
            ShopOpenEvent event = new ShopOpenEvent(player, Arena.getArenaByPlayer(player));
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) return;
        }

        Inventory inv = Bukkit.createInventory(null, invSize, Language.getMsg(player, getNamePath()));

        // 添加快速购买按钮
        inv.setItem(getQuickBuyButton().getSlot(), getQuickBuyButton().getItemStack(player));

        // 添加所有商品类别
        for (ShopCategory sc : getCategoryList()) {
            inv.setItem(sc.getSlot(), sc.getItemStack(player));
        }

        // 添加分隔符
        addSeparator(player, inv);

        // 默认选中快速购买类别，并添加选中标记
        inv.setItem(getQuickBuyButton().getSlot() + 9, getSelectedItem(player));
        //noinspection ConstantConditions
        ShopCache.getShopCache(player.getUniqueId()).setSelectedCategory(getQuickBuyButton().getSlot());

        // 将玩家的快速购买项添加到GUI中
        quickBuyCache.addInInventory(inv, ShopCache.getShopCache(player.getUniqueId()));

        player.openInventory(inv);
        if (!indexViewers.contains(player.getUniqueId())) {
            indexViewers.add(player.getUniqueId());
        }
    }


    /**
     * 在GUI中添加分隔符行。
     * @param player 目标玩家，用于获取本地化文本。
     * @param inv    要添加分隔符的库存。
     */
    public void addSeparator(Player player, Inventory inv) {
        ItemStack i = separatorStandard.clone();
        ItemMeta im = i.getItemMeta();
        if (im != null) {
            im.setDisplayName(Language.getMsg(player, separatorNamePath));
            im.setLore(Language.getList(player, separatorLorePath));
            i.setItemMeta(im);
        }

        for (int x = 9; x < 18; x++) {
            inv.setItem(x, i);
        }
    }

    /**
     * 获取用于指示当前所选类别的物品。
     * @param player 目标玩家，用于获取本地化文本。
     */
    public ItemStack getSelectedItem(Player player) {
        ItemStack i = separatorSelected.clone();
        ItemMeta im = i.getItemMeta();
        if (im != null) {
            im.setDisplayName(Language.getMsg(player, separatorNamePath));
            im.setLore(Language.getList(player, separatorLorePath));
            i.setItemMeta(im);
        }
        return i;
    }

    /**
     * 添加一个商店类别到主界面。
     */
    public void addShopCategory(ShopCategory sc) {
        categoryList.add(sc);
        BedWars.debug("Adding shop category: " + sc + " at slot " + sc.getSlot());
    }

    /**
     * 获取商店界面的标题语言路径。
     */
    public String getNamePath() {
        return namePath;
    }

    /**
     * 获取商店界面的大小。
     */
    public int getInvSize() {
        return invSize;
    }

    /**
     * 获取所有商店类别的列表。
     */
    public List<ShopCategory> getCategoryList() {
        return categoryList;
    }

    /**
     * 获取快速购买按钮。
     */
    public QuickBuyButton getQuickBuyButton() {
        return quickBuyButton;
    }

    /**
     * 获取当前正在查看商店主界面的玩家列表。
     */
    public static List<UUID> getIndexViewers() {
        return new ArrayList<>(indexViewers);
    }
}
