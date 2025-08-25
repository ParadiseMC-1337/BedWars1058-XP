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

package com.andrei1058.bedwars.listeners.dropshandler;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.configuration.ConfigPath;
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent;
import com.andrei1058.bedwars.api.language.Messages;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.andrei1058.bedwars.BedWars.nms;
import static com.andrei1058.bedwars.api.language.Language.getMsg;

/**
 * 处理玩家死亡后的物品掉落逻辑。
 */
public class PlayerDrops {

    private PlayerDrops() {
    }

    /**
     * 处理玩家死亡时的物品掉落行为。
     *
     * @param arena       竞技场实例。
     * @param victim      死者。
     * @param killer      击杀者，可能为 null。
     * @param victimsTeam 死者所在的队伍。
     * @param killersTeam 击杀者所在的队伍。
     * @param cause       玩家的死亡原因。
     * @param inventory   死者的背包物品列表。
     * @return 如果插件处理了掉落逻辑，则返回 true，这会阻止原版的掉落行为。
     */
    public static boolean handlePlayerDrops(IArena arena, Player victim, Player killer, ITeam victimsTeam, ITeam killersTeam, PlayerKillEvent.PlayerKillCause cause, List<ItemStack> inventory) {
        // 检查竞技场配置是否允许原版死亡掉落。如果是，则不执行自定义逻辑。
        if (arena.getConfig().getBoolean(ConfigPath.ARENA_NORMAL_DEATH_DROPS)) {
            return false;
        }
        // 如果玩家是被其他玩家推下虚空或高处导致死亡。
        if (cause == PlayerKillEvent.PlayerKillCause.PLAYER_PUSH || cause == PlayerKillEvent.PlayerKillCause.PLAYER_PUSH_FINAL) {
            // 在玩家死亡地点掉落物品。
            dropItems(victim, inventory);
            return true;
        }
        // 如果没有击杀者（例如，玩家自杀或死于环境伤害）。
        if (killer == null) {
            // 在死亡地点掉落物品。
            dropItems(victim, inventory);
            return true;
        }
        // 如果是被可消失的实体（如铁傀儡、银鱼）杀死。
        if (cause.isDespawnable()) {
            // 在死亡地点掉落物品。
            dropItems(victim, inventory);
            return true;
        }
        // 如果玩家在PVP中途退出游戏。
        if (cause.isPvpLogOut()) {
            // 在玩家断开连接的地点掉落物品。
            dropItems(victim, inventory);
            return true;
        }

        // 如果是最终击杀。
        if (cause.isFinalKill()) {
            // 处理玩家末影箱中的物品。
            if (victimsTeam != null) {
                Location dropsLocation = new Location(victim.getWorld(), victimsTeam.getKillDropsLocation().getBlockX(), victimsTeam.getKillDropsLocation().getY(), victimsTeam.getKillDropsLocation().getZ());
                // 将末影箱中的所有物品掉落在队伍设置的掉落点。
                victim.getEnderChest().forEach(item -> {
                    if (item != null) {
                        victim.getWorld().dropItemNaturally(dropsLocation, item);
                    }
                });
                victim.getEnderChest().clear();
            }
        }

        // 处理受害者的背包物品，前提是受害者有队伍且不是自杀。
        if (victimsTeam != null && !(victimsTeam.equals(killersTeam) && victim.equals(killer))) {
            // 如果受害者队伍的床已被摧毁（最终死亡）。
            if (victimsTeam.isBedDestroyed()) {
                Location dropLocation;
                // 如果有击杀者，则掉落在队伍设置的掉落点，否则掉落在玩家死亡处
                if (killer != null && arena.getTeam(killer) != null) {
                    Vector v = victimsTeam.getKillDropsLocation();
                    dropLocation = new Location(arena.getWorld(), v.getX(), v.getY(), v.getZ());
                } else {
                    dropLocation = victim.getLocation();
                }

                for (ItemStack i : inventory) {
                    if (i == null) continue;
                    if (i.getType() == Material.AIR) continue;
                    // 跳过盔甲、弓、剑和工具。
                    if (nms.isArmor(i) || nms.isBow(i) || nms.isSword(i) || nms.isTool(i)) continue;
                    // 跳过商店的永久升级物品。
                    if (!nms.getShopUpgradeIdentifier(i).trim().isEmpty()) continue;
                    // 将物品掉落在计算好的位置
                    dropLocation.getWorld().dropItemNaturally(dropLocation, i);
                }
                // 在循环外掉落经验瓶
                int level = victim.getLevel();
                if (level > 0) {
                    Material expBottleMaterial = Material.valueOf(BedWars.getForCurrentVersion("EXP_BOTTLE", "EXP_BOTTLE", "EXPERIENCE_BOTTLE"));
                    victim.getLocation().getWorld().dropItemNaturally(victim.getLocation(), new ItemStack(expBottleMaterial, level / 10));
                }
            } else {
                // 如果床未被摧毁（普通死亡），则将资源给予击杀者。
                // 如果击杀者正在重生，则不给予物品。
                if (!arena.isPlayer(killer)) return true;
                if (arena.isReSpawning(killer)) return true;
                // 使用Map来统计拾取的各种资源的数量。
                Map<Material, Integer> materialDrops = new HashMap<>();
                for (ItemStack i : inventory) {
                    if (i == null) continue;
                    if (i.getType() == Material.AIR) continue;
                    // 只处理钻石、绿宝石、铁和金。
                    if (i.getType() == Material.DIAMOND || i.getType() == Material.EMERALD || i.getType() == Material.IRON_INGOT || i.getType() == Material.GOLD_INGOT) {

                        // 直接将物品添加到击杀者的背包。
                        killer.getInventory().addItem(i);

                        // 统计掉落物品的数量。
                        if (materialDrops.containsKey(i.getType())) {
                            materialDrops.replace(i.getType(), materialDrops.get(i.getType()) + i.getAmount());
                        } else {
                            materialDrops.put(i.getType(), i.getAmount());
                        }


                    }
                }

                killer.setLevel(killer.getLevel() + victim.getLevel());

                // 遍历统计结果，向击杀者发送奖励消息。
                for (Map.Entry<Material, Integer> entry : materialDrops.entrySet()) {
                    String msg = "";
                    int amount = entry.getValue();
                    switch (entry.getKey()) {
                        case DIAMOND:
                            msg = getMsg(killer, Messages.PLAYER_DIE_REWARD_DIAMOND).replace("{meaning}", amount == 1 ?
                                    getMsg(killer, Messages.MEANING_DIAMOND_SINGULAR) : getMsg(killer, Messages.MEANING_DIAMOND_PLURAL));
                            break;
                        case EMERALD:
                            msg = getMsg(killer, Messages.PLAYER_DIE_REWARD_EMERALD).replace("{meaning}", amount == 1 ?
                                    getMsg(killer, Messages.MEANING_EMERALD_SINGULAR) : getMsg(killer, Messages.MEANING_EMERALD_PLURAL));
                            break;
                        case IRON_INGOT:
                            msg = getMsg(killer, Messages.PLAYER_DIE_REWARD_IRON).replace("{meaning}", amount == 1 ?
                                    getMsg(killer, Messages.MEANING_IRON_SINGULAR) : getMsg(killer, Messages.MEANING_IRON_PLURAL));
                            break;
                        case GOLD_INGOT:
                            msg = getMsg(killer, Messages.PLAYER_DIE_REWARD_GOLD).replace("{meaning}", amount == 1 ?
                                    getMsg(killer, Messages.MEANING_GOLD_SINGULAR) : getMsg(killer, Messages.MEANING_GOLD_PLURAL));
                            break;
                    }
                    killer.sendMessage(msg.replace("{amount}", String.valueOf(amount)));
                }
                materialDrops.clear();
            }

        }
        return true;
    }

    /**
     * 在玩家的位置掉落背包中的特定资源（铁、金、钻石、绿宝石）。
     *
     * @param player    需要掉落物品的玩家。
     * @param inventory 玩家的背包物品列表。
     */
    private static void dropItems(Player player, List<ItemStack> inventory) {
        for (ItemStack i : inventory) {
            if (i == null) continue;
            if (i.getType() == Material.AIR) continue;
            // 只掉落铁、金、钻石和绿宝石。
            if (i.getType() == Material.DIAMOND || i.getType() == Material.EMERALD || i.getType() == Material.IRON_INGOT || i.getType() == Material.GOLD_INGOT) {
                player.getLocation().getWorld().dropItemNaturally(player.getLocation(), i);
            }
        }

        int level = player.getLevel();
        if (level > 0) {
            Material expBottleMaterial = Material.valueOf(BedWars.getForCurrentVersion("EXP_BOTTLE", "EXP_BOTTLE", "EXPERIENCE_BOTTLE"));
            player.getLocation().getWorld().dropItemNaturally(player.getLocation(), new ItemStack(expBottleMaterial, level / 10));
        }
    }
}
