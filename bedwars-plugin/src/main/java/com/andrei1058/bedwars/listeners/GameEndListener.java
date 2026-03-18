package com.andrei1058.bedwars.listeners;

import com.andrei1058.bedwars.api.events.gameplay.GameEndEvent;
import com.andrei1058.bedwars.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class GameEndListener implements Listener {

    @EventHandler
    public void cleanInventoriesAndDroppedItems(@NotNull GameEndEvent event) {
        if (event.getArena().getPlayers().isEmpty()) {
            return;
        }

        for (UUID p : event.getAliveWinners()) {
            Player player = Bukkit.getPlayer(p);
            if (player == null) {
                continue;
            }
            if (event.getArena() instanceof Arena) {
                ((Arena) event.getArena()).sendVictoryCommandItems(player);
            }
        }

        // clear dropped items
        World game = event.getArena().getWorld();
        for (Entity item : game.getEntities()) {
            if (item instanceof Item) {
                item.remove();
            }
        }
    }
}
