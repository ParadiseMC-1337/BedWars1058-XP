package com.andrei1058.bedwars.listeners;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.configuration.ConfigPath;
import com.andrei1058.bedwars.arena.Arena;
import com.andrei1058.bedwars.configuration.ArenaConfig;
import com.andrei1058.bedwars.xp.ExperienceManager;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import static com.andrei1058.bedwars.BedWars.plugin;

public class PickupItemListener implements Listener {


    @EventHandler
    public void onPickupItem(PlayerPickupItemEvent event){

        YamlConfiguration yml = new ArenaConfig(BedWars.plugin, event.getPlayer().getWorld().getName(), plugin.getDataFolder().getPath() + "/Arenas").getYml();
        ItemStack stack = event.getItem().getItemStack();
        Player player = event.getPlayer();
        Item item = event.getItem();
        IArena arena = Arena.getArenaByPlayer(event.getPlayer());
        int xp = ExperienceManager.getExperienceFromMaterial(stack.getType()) * stack.getAmount();
        if (arena == null || !arena.isPlayer(event.getPlayer()) || arena.isSpectator(event.getPlayer())) return;
        if (yml.getBoolean(ConfigPath.ARENA_ENABLE_XP) && xp != 0 && !event.isCancelled()){
            event.setCancelled(true);
            player.playSound(player.getLocation(), Sound.valueOf(BedWars.getForCurrentVersion("ORB_PICKUP", "ENTITY_EXPERIENCE_ORB_PICKUP", "ENTITY_EXPERIENCE_ORB_PICKUP")), 0.6f, 1.3f);
            player.setLevel(player.getLevel() + xp);
            item.remove();
        }
    }
}
