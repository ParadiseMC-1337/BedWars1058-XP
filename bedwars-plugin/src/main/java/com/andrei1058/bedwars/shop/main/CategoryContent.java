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
import com.andrei1058.bedwars.api.arena.shop.ICategoryContent;
import com.andrei1058.bedwars.api.arena.shop.IContentTier;
import com.andrei1058.bedwars.api.configuration.ConfigPath;
import com.andrei1058.bedwars.api.events.shop.ShopBuyEvent;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.arena.Arena;
import com.andrei1058.bedwars.configuration.ArenaConfig;
import com.andrei1058.bedwars.configuration.Sounds;
import com.andrei1058.bedwars.shop.ShopCache;
import com.andrei1058.bedwars.shop.quickbuy.PlayerQuickBuyCache;
import com.andrei1058.bedwars.shop.quickbuy.QuickBuyElement;
import com.andrei1058.bedwars.xp.ExperienceManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

import static com.andrei1058.bedwars.BedWars.nms;
import static com.andrei1058.bedwars.BedWars.plugin;
import static com.andrei1058.bedwars.api.language.Language.getMsg;

/**
 * 代表商店类别中的一个可购买内容。
 * 一个内容可以有多个层级（tier），例如：木剑 -> 石剑 -> 铁剑。
 * 这个类负责处理购买逻辑、检查货币、与玩家缓存交互以及在GUI中显示。
 */
@SuppressWarnings("WeakerAccess")
public class CategoryContent implements ICategoryContent {

    private int slot;
    private boolean loaded = false;
    private List<IContentTier> contentTiers = new ArrayList<>();
    private String contentName;
    private String itemNamePath, itemLorePath;
    private String identifier;
    private boolean permanent = false, downgradable = false, unbreakable = false;
    private byte weight = 0;
    private ShopCategory father;

    /**
     * 从配置文件加载一个新的商店内容。
     * @param path         在配置文件中的路径。
     * @param name         内容的名称（标识符）。
     * @param categoryName 所属类别的名称。
     * @param yml          配置文件实例。
     * @param father       所属的 {@link ShopCategory} 父对象。
     */
    public CategoryContent(String path, String name, String categoryName, YamlConfiguration yml, ShopCategory father) {
        BedWars.debug("Loading CategoryContent " + path);
        this.contentName = name;
        this.father = father;

        if (path == null || name == null || categoryName == null || yml == null) return;

        if (yml.get(path + "." + ConfigPath.SHOP_CATEGORY_CONTENT_CONTENT_SLOT) == null) {
            plugin.getLogger().severe("Content slot not set at " + path);
            return;
        }

        if (yml.get(path + "." + ConfigPath.SHOP_CATEGORY_CONTENT_CONTENT_TIERS) == null) {
            plugin.getLogger().severe("No tiers set for " + path);
            return;
        }

        if (yml.getConfigurationSection(path + "." + ConfigPath.SHOP_CATEGORY_CONTENT_CONTENT_TIERS).getKeys(false).isEmpty()) {
            plugin.getLogger().severe("No tiers set for " + path);
            return;
        }

        if (yml.get(path + "." + ConfigPath.SHOP_CATEGORY_CONTENT_CONTENT_TIERS + ".tier1") == null) {
            plugin.getLogger().severe("tier1 not found for " + path);
            return;
        }

        // 加载内容的属性（是否永久、可降级、不可破坏、权重）
        if (yml.get(path + "." + ConfigPath.SHOP_CATEGORY_CONTENT_IS_PERMANENT) != null) {
            permanent = yml.getBoolean(path + "." + ConfigPath.SHOP_CATEGORY_CONTENT_IS_PERMANENT);
        }

        if (yml.get(path + "." + ConfigPath.SHOP_CATEGORY_CONTENT_IS_DOWNGRADABLE) != null) {
            downgradable = yml.getBoolean(path + "." + ConfigPath.SHOP_CATEGORY_CONTENT_IS_DOWNGRADABLE);
        }

        if (yml.get(path + "." + ConfigPath.SHOP_CATEGORY_CONTENT_IS_UNBREAKABLE) != null) {
            unbreakable = yml.getBoolean(path + "." + ConfigPath.SHOP_CATEGORY_CONTENT_IS_UNBREAKABLE);
        }

        if (yml.get(path + "." + ConfigPath.SHOP_CATEGORY_CONTENT_WEIGHT) != null) {
            weight = (byte) yml.getInt(path + "." + ConfigPath.SHOP_CATEGORY_CONTENT_WEIGHT);
        }

        this.slot = yml.getInt(path + "." + ConfigPath.SHOP_CATEGORY_CONTENT_CONTENT_SLOT);

        // 加载所有层级
        ContentTier ctt;
        for (String s : yml.getConfigurationSection(path + "." + ConfigPath.SHOP_CATEGORY_CONTENT_CONTENT_TIERS).getKeys(false)) {
            ctt = new ContentTier(path + "." + ConfigPath.SHOP_CATEGORY_CONTENT_CONTENT_TIERS + "." + s, s, path, yml);
            /*if (ctt.isLoaded())*/
            contentTiers.add(ctt);
        }

        // 初始化语言文件路径
        itemNamePath = Messages.SHOP_CONTENT_TIER_ITEM_NAME.replace("%category%", categoryName).replace("%content%", contentName);
        for (Language lang : Language.getLanguages()) {
            if (!lang.exists(itemNamePath)) {
                lang.set(itemNamePath, "&cName not set");
            }
        }
        itemLorePath = Messages.SHOP_CONTENT_TIER_ITEM_LORE.replace("%category%", categoryName).replace("%content%", contentName);
        for (Language lang : Language.getLanguages()) {
            if (!lang.exists(itemLorePath)) {
                lang.set(itemLorePath, "&cLore not set");
            }
        }

        identifier = path;

        loaded = true;

    }

    /**
     * 当玩家点击此物品时执行购买逻辑。
     * @param player     点击的玩家。
     * @param shopCache  玩家的商店缓存。
     * @param slot       物品在GUI中的槽位。
     */
    public void execute(Player player, ShopCache shopCache, int slot) {

        IContentTier ct;

        // 检查权重，防止购买低级物品
        if (shopCache.getCategoryWeight(father) > weight) return;

        if (shopCache.getContentTier(getIdentifier()) > contentTiers.size()) {
            Bukkit.getLogger().severe("Wrong tier order at: " + getIdentifier());
            return;
        }

        // 检查是否可以重复购买
        if (shopCache.getContentTier(getIdentifier()) == contentTiers.size()) {
            if (isPermanent() && shopCache.hasCachedItem(this)) {
                player.sendMessage(getMsg(player, Messages.SHOP_ALREADY_BOUGHT));
                Sounds.playSound(ConfigPath.SOUNDS_INSUFF_MONEY, player);
                return;
            }
            // 当前为最高层级
            ct = contentTiers.get(shopCache.getContentTier(getIdentifier()) - 1);
        } else {
            // 如果是首次购买，获取第一级；否则获取下一级
            if (!shopCache.hasCachedItem(this)) {
                ct = contentTiers.get(0);
            } else {
                ct = contentTiers.get(shopCache.getContentTier(identifier));
            }
        }

        YamlConfiguration yml = new ArenaConfig(BedWars.plugin, player.getWorld().getName(), plugin.getDataFolder().getPath() + "/Arenas").getYml();
        int xpPrice = ExperienceManager.getExperienceFromMaterial(ct.getCurrency()) * ct.getPrice();
        String currencyMsgPath = (!yml.getBoolean(ConfigPath.ARENA_ENABLE_XP) || xpPrice == 0) ? getCurrencyMsgPath(ct) : Messages.MEANING_EXP;

        // 检查货币是否足够
        int money = calculateMoney(player, ct.getCurrency());
        boolean checkMoney = (!yml.getBoolean(ConfigPath.ARENA_ENABLE_XP) || xpPrice == 0) ? money < ct.getPrice() : player.getLevel() < xpPrice;
        if (checkMoney) {
            int amount = (!yml.getBoolean(ConfigPath.ARENA_ENABLE_XP) || xpPrice == 0) ? ct.getPrice() - money : xpPrice - player.getLevel();
            player.sendMessage(getMsg(player, Messages.SHOP_INSUFFICIENT_MONEY).replace("{currency}", getMsg(player, currencyMsgPath)).
                    replace("{amount}", String.valueOf(amount)));
            Sounds.playSound(ConfigPath.SOUNDS_INSUFF_MONEY, player);
            return;
        }

        ShopBuyEvent event;
        // 调用商店购买事件
        Bukkit.getPluginManager().callEvent(event = new ShopBuyEvent(player, Arena.getArenaByPlayer(player), this));

        if (event.isCancelled()){
            return;
        }

        // 扣除货币
        takeMoney(player, ct.getCurrency(), ct.getPrice());

        // 如果可能，升级物品层级
        shopCache.upgradeCachedItem(this, slot);


        // 给予物品
        giveItems(player, shopCache, Arena.getArenaByPlayer(player));

        // 播放音效
        Sounds.playSound(ConfigPath.SOUNDS_BOUGHT, player);

        // 发送购买成功消息
        if (itemNamePath == null || Language.getPlayerLanguage(player).getYml().get(itemNamePath) == null) {
            ItemStack displayItem = ct.getItemStack();
            if (displayItem.getItemMeta() != null && displayItem.getItemMeta().hasDisplayName()) {
                player.sendMessage(getMsg(player, Messages.SHOP_NEW_PURCHASE).replace("{item}", displayItem.getItemMeta().getDisplayName()));
            }
        } else {
            player.sendMessage(getMsg(player, Messages.SHOP_NEW_PURCHASE).replace("{item}", ChatColor.stripColor(getMsg(player, itemNamePath))).replace("{color}", "").replace("{tier}", ""));
        }

        // 设置类别权重
        shopCache.setCategoryWeight(father, weight);
    }

    /**
     * 将层级物品给予玩家。
     */
    public void giveItems(Player player, ShopCache shopCache, IArena arena) {
        for (IBuyItem bi : contentTiers.get(shopCache.getContentTier(getIdentifier()) - 1).getBuyItemsList()) {
            bi.give(player, arena);
        }
    }

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public ItemStack getItemStack(Player player) {
        ShopCache sc = ShopCache.getShopCache(player.getUniqueId());
        return sc == null ? null : getItemStack(player, sc);
    }

    @Override
    public boolean hasQuick(Player player) {
        PlayerQuickBuyCache pqbc = PlayerQuickBuyCache.getQuickBuyCache(player.getUniqueId());
        return pqbc != null && hasQuick(pqbc);
    }

    /**
     * 获取为特定玩家生成的物品堆栈，包含动态的lore和名称。
     * @param player    玩家。
     * @param shopCache 玩家的商店缓存。
     * @return        生成的物品堆栈。
     */
    public ItemStack getItemStack(Player player, ShopCache shopCache) {
        IContentTier ct;
        // 根据玩家当前的购买层级，决定显示哪个层级的信息
        if (shopCache.getContentTier(identifier) == contentTiers.size()) {
            ct = contentTiers.get(contentTiers.size() - 1);
        } else {
            if (shopCache.hasCachedItem(this)) {
                ct = contentTiers.get(shopCache.getContentTier(identifier));
            } else {
                ct = contentTiers.get(shopCache.getContentTier(identifier) - 1);
            }
        }

        ItemStack i = ct.getItemStack();
        ItemMeta im = i.getItemMeta();

        if (im != null) {
            im = i.getItemMeta().clone();
            boolean canAfford;

            PlayerQuickBuyCache qbc = PlayerQuickBuyCache.getQuickBuyCache(player.getUniqueId());
            boolean hasQuick = qbc != null && hasQuick(qbc);


            YamlConfiguration yml = new ArenaConfig(plugin, player.getWorld().getName(), plugin.getDataFolder().getPath() + "/Arenas").getYml();
            int xpPrice = ExperienceManager.getExperienceFromMaterial(ct.getCurrency()) * ct.getPrice();
            boolean useXp = yml.getBoolean(ConfigPath.ARENA_ENABLE_XP) && xpPrice > 0;

            int displayPrice = useXp ? xpPrice : ct.getPrice();
            if (useXp){
                canAfford = player.getLevel() >= displayPrice;
            } else {
                canAfford = calculateMoney(player, ct.getCurrency()) >= displayPrice;
            }

            String color = getMsg(player, canAfford ? Messages.SHOP_CAN_BUY_COLOR : Messages.SHOP_CANT_BUY_COLOR);
            String currencyMsgPath = useXp ? Messages.MEANING_EXP : getCurrencyMsgPath(ct);
            String translatedCurrency = getMsg(player, currencyMsgPath);
            ChatColor cColor = useXp ? ChatColor.GREEN : getCurrencyColor(ct.getCurrency());

            int tierI = ct.getValue();
            String tier = getRomanNumber(tierI);
            String buyStatus;

            // 根据不同条件生成不同的购买状态lore
            if (isPermanent() && shopCache.hasCachedItem(this) && shopCache.getCachedItem(this).getTier() == getContentTiers().size()) {
                if (!(nms.isArmor(i))){
                    buyStatus = getMsg(player, Messages.SHOP_LORE_STATUS_MAXED);
                }else {
                    buyStatus = getMsg(player, Messages.SHOP_LORE_STATUS_ARMOR);
                }
            } else if (!canAfford) {
                buyStatus = getMsg(player, Messages.SHOP_LORE_STATUS_CANT_AFFORD).replace("{currency}", translatedCurrency);
            } else {
                buyStatus = getMsg(player, Messages.SHOP_LORE_STATUS_CAN_BUY);
            }


            im.setDisplayName(getMsg(player, itemNamePath).replace("{color}", color).replace("{tier}", tier));

            List<String> lore = new ArrayList<>();
            for (String s : Language.getList(player, itemLorePath)) {
                if (s.contains("{quick_buy}")) {
                    if (hasQuick) {
                        if (ShopIndex.getIndexViewers().contains(player.getUniqueId())) {
                            s = getMsg(player, Messages.SHOP_LORE_QUICK_REMOVE);
                        } else {
                            continue;
                        }
                    } else {
                        s = getMsg(player, Messages.SHOP_LORE_QUICK_ADD);
                    }
                }
                s = s.replace("{tier}", tier).replace("{color}", color).replace("{cost}", cColor + String.valueOf(displayPrice))
                        .replace("{currency}", cColor + translatedCurrency).replace("{buy_status}", buyStatus);
                lore.add(s);
            }

            im.setLore(lore);
            i.setItemMeta(im);
        }
        return i;
    }

    /**
     * 检查玩家是否将此物品添加到了快速购买。
     */
    public boolean hasQuick(PlayerQuickBuyCache c) {
        for (QuickBuyElement q : c.getElements()) {
            if (q.getCategoryContent() == this) return true;
        }
        return false;
    }

    /**
     * 计算玩家拥有的特定货币数量。
     * @param player   玩家。
     * @param currency 货币类型。
     * @return 货币数量。
     */
    public static int calculateMoney(Player player, Material currency) {
        if (currency == Material.AIR) {
            return (int) BedWars.getEconomy().getMoney(player);
        }

        int amount = 0;
        for (ItemStack is : player.getInventory().getContents()) {
            if (is == null) continue;
            if (is.getType() == currency) amount += is.getAmount();
        }
        return amount;
    }

    /**
     * 根据字符串获取货币的 Material 类型。
     */
    public static Material getCurrency(String currency) {
        Material material;
        switch (currency) {
            default:
                material = Material.IRON_INGOT;
                break;
            case "gold":
                material = Material.GOLD_INGOT;
                break;
            case "diamond":
                material = Material.DIAMOND;
                break;
            case "emerald":
                material = Material.EMERALD;
                break;
            case "vault":
                material = Material.AIR;
                break;
        }
        return material;
    }

    /**
     * 获取货币对应的颜色代码。
     */
    public static ChatColor getCurrencyColor(Material currency) {
        ChatColor c = ChatColor.DARK_GREEN;
        if (currency.toString().toLowerCase().contains("diamond")) {
            c = ChatColor.AQUA;
        } else if (currency.toString().toLowerCase().contains("gold")) {
            c = ChatColor.GOLD;
        } else if (currency.toString().toLowerCase().contains("iron")) {
            c = ChatColor.WHITE;
        }
        return c;
    }

    /**
     * 获取货币的语言文件路径（单数/复数）。
     */
    public static String getCurrencyMsgPath(IContentTier contentTier) {
        String c;

        if (contentTier.getCurrency().toString().toLowerCase().contains("iron")) {
            c = contentTier.getPrice() == 1 ? Messages.MEANING_IRON_SINGULAR : Messages.MEANING_IRON_PLURAL;
        } else if (contentTier.getCurrency().toString().toLowerCase().contains("gold")) {
            c = contentTier.getPrice() == 1 ? Messages.MEANING_GOLD_SINGULAR : Messages.MEANING_GOLD_PLURAL;
        } else if (contentTier.getCurrency().toString().toLowerCase().contains("emerald")) {
            c = contentTier.getPrice() == 1 ? Messages.MEANING_EMERALD_SINGULAR : Messages.MEANING_EMERALD_PLURAL;
        } else if (contentTier.getCurrency().toString().toLowerCase().contains("diamond")) {
            c = contentTier.getPrice() == 1 ? Messages.MEANING_DIAMOND_SINGULAR : Messages.MEANING_DIAMOND_PLURAL;
        } else {
            c = contentTier.getPrice() == 1 ? Messages.MEANING_VAULT_SINGULAR : Messages.MEANING_VAULT_PLURAL;
        }
        return c;
    }

    /**
     * 将整数转换为罗马数字字符串。
     */
    public static String getRomanNumber(int n) {
        String s;
        switch (n) {
            default:
                s = String.valueOf(n);
                break;
            case 1:
                s = "I";
                break;
            case 2:
                s = "II";
                break;
            case 3:
                s = "III";
                break;
            case 4:
                s = "IV";
                break;
            case 5:
                s = "V";
                break;
            case 6:
                s = "VI";
                break;
            case 7:
                s = "VII";
                break;
            case 8:
                s = "VIII";
                break;
            case 9:
                s = "IX";
                break;
            case 10:
                s = "X";
                break;
        }
        return s;
    }


    /**
     * 从玩家背包中扣除指定数量的货币。
     * @param player   玩家。
     * @param currency 货币类型。
     * @param amount   要扣除的数量。
     */
    public static void takeMoney(Player player, Material currency, int amount) {
        if (currency == Material.AIR) {
            if (!BedWars.getEconomy().isEconomy()) {
                player.sendMessage("§4§lERROR: This requires Vault Support! Please install Vault plugin!");
                return;
            }
            BedWars.getEconomy().buyAction(player, amount);
            return;
        }

        YamlConfiguration yml = new ArenaConfig(plugin, player.getWorld().getName(), plugin.getDataFolder().getPath() + "/Arenas").getYml();
        int xp = ExperienceManager.getExperienceFromMaterial(currency) * amount;


        if (!yml.getBoolean(ConfigPath.ARENA_ENABLE_XP) || xp == 0) {

        int cost = amount;
        for (ItemStack i : player.getInventory().getContents()) {
            if (i == null) continue;
            if (i.getType() == currency) {
                if (i.getAmount() < cost) {
                    cost -= i.getAmount();
                    nms.minusAmount(player, i, i.getAmount());
                    player.updateInventory();
                } else {
                    nms.minusAmount(player, i, cost);
                    player.updateInventory();
                    break;
                }
            }
            }
        } else {
            player.setLevel(player.getLevel() - xp);
        }

    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    /**
     * 检查此内容是否已成功加载。
     */
    public boolean isLoaded() {
        return loaded;
    }

    public boolean isPermanent() {
        return permanent;
    }

    public boolean isDowngradable() {
        return downgradable;
    }

    public String getIdentifier() {
        return identifier;
    }

    public List<IContentTier> getContentTiers() {
        return contentTiers;
    }
}
