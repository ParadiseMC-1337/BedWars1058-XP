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

import com.andrei1058.bedwars.api.language.Language;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * 代表商店界面中的"快速购买"按钮。
 * 这个类存储了按钮的槽位、基础物品以及用于显示名称和Lore的语言路径。
 */
public class QuickBuyButton {

    private int slot;
    private ItemStack itemStack;
    private String namePath, lorePath;

    /**
     * 创建一个新的快速购买按钮。
     *
     * @param slot      按钮在GUI中的槽位。
     * @param itemStack 按钮的基础 ItemStack 预览。
     * @param namePath  按钮名称的语言文件路径。
     * @param lorePath  按钮 lore 的语言文件路径。
     */
    public QuickBuyButton(int slot, ItemStack itemStack, String namePath, String lorePath) {
        this.slot = slot;
        this.itemStack = itemStack;
        this.namePath = namePath;
        this.lorePath = lorePath;
    }

    /**
     * 获取为特定玩家生成的、带有本地化文本的快速购买按钮物品。
     * @param player 目标玩家。
     * @return 带有正确名称和 lore 的 ItemStack。
     */
    public ItemStack getItemStack(Player player) {
        ItemStack i = itemStack.clone();
        ItemMeta im = i.getItemMeta();
        if (im != null) {
            im.setDisplayName(Language.getMsg(player, namePath));
            im.setLore(Language.getList(player, lorePath));
            i.setItemMeta(im);
        }
        return i;
    }

    /**
     * 获取快速购买按钮所在的槽位。
     */
    public int getSlot() {
        return slot;
    }
}
