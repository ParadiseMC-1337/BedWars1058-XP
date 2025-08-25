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

package com.andrei1058.bedwars.api.server;

import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.arena.team.TeamColor;
import com.andrei1058.bedwars.api.entity.Despawnable;
import com.andrei1058.bedwars.api.exceptions.InvalidEffectException;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class VersionSupport {

    private static String name2;
    public static String PLUGIN_TAG_GENERIC_KEY = "BedWars1058";
    public static String PLUGIN_TAG_TIER_KEY = "tierIdentifier";

    private Effect eggBridge;

    private static final ConcurrentHashMap<UUID, Despawnable> despawnables = new ConcurrentHashMap<>();
    private final Plugin plugin;

    public VersionSupport(Plugin plugin, String versionName) {
        name2 = versionName;
        this.plugin = plugin;
    }

    protected void loadDefaultEffects() {
        try {
            setEggBridgeEffect("MOBSPAWNER_FLAMES");
        } catch (InvalidEffectException e) {
            e.printStackTrace();
        }
    }

    /**
     * Register a new command as bukkit command
     */
    public abstract void registerCommand(String name, Command clasa);

    /**
     * Send title, subtitle. null for empty
     */
    public abstract void sendTitle(Player p, String title, String subtitle, int fadeIn, int stay, int fadeOut);

    /**
     * Send action-bar message
     */
    public abstract void playAction(Player p, String text);

    /**
     * Check if bukkit command is registered
     */
    public abstract boolean isBukkitCommandRegistered(String command);

    /**
     * Get in had item-stack
     */
    public abstract ItemStack getItemInHand(Player p);

    /**
     * Hide an entity
     */
    public abstract void hideEntity(Entity e, Player p);


    /**
     * Check if item-stack is armor
     */
    public abstract boolean isArmor(ItemStack itemStack);

    /**
     * Check if item-stack is a tool
     */
    public abstract boolean isTool(ItemStack itemStack);

    /**
     * Check if item-stack is sword
     */
    public abstract boolean isSword(ItemStack itemStack);

    /**
     * Check if item-stack is axe
     */
    public abstract boolean isAxe(ItemStack itemStack);

    /**
     * Check if item-stack is bow
     */
    public abstract boolean isBow(ItemStack itemStack);

    /**
     * Check if itemstack is Projectile
     */
    public abstract boolean isProjectile(ItemStack itemStack);

    /**
     * Check if itemstack is Invisibility Potion
     */
    public abstract boolean isInvisibilityPotion(ItemStack itemStack);

    /**
     * Check if type is a Glass type material
     */
    public boolean isGlass(Material type) {
        return type != Material.AIR && (type == Material.GLASS || type.toString().contains("_GLASS"));
    }

    /**
     * Register custom entities
     */
    public abstract void registerEntities();

    /**
     * Spawn shop NPC
     */
    public abstract void spawnShop(Location loc, String name1, List<Player> players, IArena arena);

    /**
     * Get item-stack damage amount
     */
    public abstract double getDamage(ItemStack i);

    /**
     * Spawn silverfish for a team
     */
    public abstract void spawnSilverfish(Location loc, ITeam team, double speed, double health, int despawn, double damage);

    /**
     * Spawn a iron-golem for a team
     */
    public abstract void spawnIronGolem(Location loc, ITeam team, double speed, double health, int despawn);

    /**
     * Is despawnable entity
     */
    public boolean isDespawnable(Entity e) {
        return despawnables.get(e.getUniqueId()) != null;
    }

    /**
     * Change item amount
     */
    public abstract void minusAmount(Player p, ItemStack i, int amount);

    /**
     * Set tnt source
     */
    public abstract void setSource(TNTPrimed tnt, Player owner);

    /**
     * Void damage with cause
     */
    public abstract void voidKill(Player p);

    /**
     * Hide player armor to a player
     */
    public abstract void hideArmor(Player victim, Player receiver);

    /**
     * Show a player armor
     */
    public abstract void showArmor(Player victim, Player receiver);

    /**
     * Spawn ender dragon
     */
    public abstract void spawnDragon(Location l, ITeam team);

    /**
     * Color a bed 1.12+
     */
    public abstract void colorBed(ITeam team);

    /**
     * Modify block blast resistance.
     */
    public abstract void registerTntWhitelist(float endStoneBlast, float glassBlast);

    /**
     * Egg bridge particles
     */
    public Effect eggBridge() {
        return eggBridge;
    }

    @SuppressWarnings("WeakerAccess")
    public void setEggBridgeEffect(String eggBridge) throws InvalidEffectException {
        try {
            this.eggBridge = Effect.valueOf(eggBridge);
        } catch (Exception e) {
            throw new InvalidEffectException(eggBridge);
        }
    }

    /**
     * Set block data
     * For 1.13 support
     */
    public abstract void setBlockTeamColor(Block block, TeamColor teamColor);

    /**
     * Disable collisions in 1.9+
     */
    public abstract void setCollide(Player p, IArena a, boolean value);

    /**
     * Add custom data to an ItemStack
     */
    public abstract ItemStack addCustomData(ItemStack i, String data);

    public abstract ItemStack setTag(ItemStack itemStack, String key, String value);

    /**
     * Get a custom item tag.
     *
     * @return null if not present.
     */
    @SuppressWarnings("unused")
    public abstract String getTag(ItemStack itemStack, String key);

    /**
     * Check if an item has a BedWars1058 NBTTag
     */
    public abstract boolean isCustomBedWarsItem(ItemStack i);

    /**
     * Get the NBTTag from a BedWars1058 item
     */
    public abstract String getCustomData(ItemStack i);

    /**
     * Color an item if possible with the team's color
     */
    public abstract ItemStack colourItem(ItemStack itemStack, ITeam bedWarsTeam);

    public abstract ItemStack createItemStack(String material, int amount, short data);

    /**
     * Check if is a player head
     */
    public boolean isPlayerHead(String material, int data) {
        return material.equalsIgnoreCase("PLAYER_HEAD");
    }

    /**
     * Get fireball material
     */
    public abstract Material materialFireball();

    /**
     * Player head material
     */
    public abstract Material materialPlayerHead();

    /**
     * Get snowball material
     */
    public abstract Material materialSnowball();

    /**
     * Get gold  helmet material
     */
    public abstract Material materialGoldenHelmet();

    /**
     * Get gold chest plate
     */
    public abstract Material materialGoldenChestPlate();

    /**
     * Get gold leggings
     */
    public abstract Material materialGoldenLeggings();

    /**
     * Get netherite  helmet material
     */
    public abstract Material materialNetheriteHelmet();

    /**
     * Get netherite chest plate
     */
    public abstract Material materialNetheriteChestPlate();

    /**
     * Get netherite leggings
     */
    public abstract Material materialNetheriteLeggings();

    /**
     * Get elytra - supports: 1.12.2+
     */
    public abstract Material materialElytra();

    /**
     * Cake material
     */
    public abstract Material materialCake();

    /**
     * Crafting table material
     */
    public abstract Material materialCraftingTable();

    /**
     * Enchanting table material
     */
    public abstract Material materialEnchantingTable();

    /**
     * Check if bed
     */
    public boolean isBed(Material material) {
        return material.toString().contains("_BED");
    }

    /**
     * Item Data compare
     * This will always return true on versions major or equal 1.13
     */
    public boolean itemStackDataCompare(ItemStack i, short data) {
        return true;
    }

    /**
     * Set block data
     * For versions before 1.13
     */
    public void setJoinSignBackgroundBlockData(BlockState b, byte data) {

    }

    /**
     * Change the block behind the join sign.
     */
    public abstract void setJoinSignBackground(BlockState b, Material material);

    /**
     * Wool material
     */
    public abstract Material woolMaterial();

    /**
     * Get an ItemStack identifier
     * will return null text if it does not have an identifier
     */
    public abstract String getShopUpgradeIdentifier(ItemStack itemStack);

    /**
     * Set an upgrade identifier
     */
    public abstract ItemStack setShopUpgradeIdentifier(ItemStack itemStack, String identifier);

    /**
     * Get player head with skin.
     *
     * @param copyTagFrom will copy nbt tag from this item.
     */
    public abstract ItemStack getPlayerHead(Player player, @Nullable ItemStack copyTagFrom);

    /**
     * This will send the player spawn packet after a player re-spawn.
     * <p>
     * Show the target player to players and spectators in the arena.
     */
    public abstract void sendPlayerSpawnPackets(Player player, IArena arena);

    /**
     * Get inventory name.
     */
    public abstract String getInventoryName(InventoryEvent e);

    /**
     * Make item unbreakable.
     */
    public abstract void setUnbreakable(ItemMeta itemMeta);

    /**
     * Get list of entities that are going to despawn based on a timer.
     */
    public ConcurrentHashMap<UUID, Despawnable> getDespawnablesList() {
        return despawnables;
    }


    public static String getName() {
        return name2;
    }

    public abstract int getVersion();

    public Plugin getPlugin() {
        return plugin;
    }

    public abstract void registerVersionListeners();

    /**
     * Get main level name.
     */
    public abstract String getMainLevel();

    public byte getCompressedAngle(float value) {
        return (byte) ((value * 256.0F) / 360.0F);
    }

    public void spigotShowPlayer(Player victim, Player receiver) {
        receiver.showPlayer(victim);
    }

    public void spigotHidePlayer(Player victim, Player receiver) {
        receiver.hidePlayer(victim);
    }

    /**
     * Make fireball go straight.
     *
     * @param fireball fireball instance;
     * @param vector   fireball direction to normalize.
     * @return modified fireball.
     */
    public abstract Fireball setFireballDirection(Fireball fireball, Vector vector);

    public abstract void playRedStoneDot(Player player);

    public abstract void clearArrowsFromPlayerBody(Player player);

    public abstract void placeTowerBlocks(Block b, IArena a, TeamColor color, int x, int y,int z);

    public abstract void placeLadder(Block b, int x, int y, int z, IArena a, int ladderdata);

    public abstract void playVillagerEffect(Player player, Location location);

    /**
     * Display particle effect at location
     * For 1.8 versions, this will send packets to all players in the world
     * For newer versions, this will use the spawnParticle method
     */
    public abstract void displayParticle(Player player, String particle, Location location, int amount);

    /**
     * Check if the item is a modded item (from forge mods)
     * Used for MohistMC Forge support
     */
    public boolean isModdedItem(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return false;
        }
        
        // 在 1.13+ 版本中，尝试获取物品的命名空间键
        try {
            // 首先尝试使用 name() 方法，然后转换为小写
            String materialName = itemStack.getType().name().toLowerCase();
            
            // 如果材质名称包含下划线，可能是模组物品的内部表示
            // 但这种方法不够准确，所以我们还是使用更简单的检查
            String materialString = itemStack.getType().toString();
            
            // 检查是否包含命名空间分隔符且不是 minecraft 命名空间
            return materialString.contains(":") && !materialString.startsWith("minecraft:");
        } catch (Exception e) {
            // 如果出现任何异常，假设它是原版物品
            return false;
        }
    }

    /**
     * Create item stack with raw NBT support
     * Supports both vanilla and modded items
     */
    public abstract ItemStack createItemStackWithNBT(String material, int amount, short data, String rawNBT);

    /**
     * Apply raw NBT string to an existing item stack
     * For advanced item configuration
     */
    public abstract ItemStack applyRawNBT(ItemStack itemStack, String rawNBT);

    /**
     * Get the full NBT as string from an item stack
     * Useful for debugging and configuration
     */
    public abstract String getRawNBT(ItemStack itemStack);

    /**
     * Check if server is running MohistMC or similar Forge hybrid
     */
    public boolean isMohistMC() {
        try {
            Class.forName("com.mohistmc.api.ServerAPI");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Create a modded item stack if running on MohistMC
     * Falls back to vanilla behavior otherwise
     */
    public ItemStack createModdedItemStack(String modId, String itemName, int amount, short data) {
        if (isMohistMC()) {
            try {
                // 使用正确的命名空间格式: modid:itemname
                String namespacedKey = modId.toLowerCase() + ":" + itemName.toLowerCase();
                Material material = Material.matchMaterial(namespacedKey);
                
                if (material != null) {
                    getPlugin().getLogger().info("Successfully created modded item: " + namespacedKey);
                    return new ItemStack(material, amount, data);
                } else {
                    // 如果命名空间格式失败，尝试不带命名空间的格式（某些模组可能注册为普通材质）
                    material = Material.matchMaterial(itemName.toUpperCase());
                    if (material != null) {
                        getPlugin().getLogger().info("Found modded item without namespace: " + itemName);
                        return new ItemStack(material, amount, data);
                    }
                }
            } catch (Exception e) {
                getPlugin().getLogger().warning("Error creating modded item " + modId + ":" + itemName + " - " + e.getMessage());
            }
            
            // 如果都失败了，回退到基岩
            getPlugin().getLogger().warning("Modded item " + modId + ":" + itemName + " not found, falling back to BEDROCK");
            return new ItemStack(Material.BEDROCK, amount);
        }
        // 不在 MohistMC 上运行，返回基岩作为占位符
        return new ItemStack(Material.BEDROCK, amount);
    }
}
