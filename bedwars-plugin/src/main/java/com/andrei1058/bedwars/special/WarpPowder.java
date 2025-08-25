package com.andrei1058.bedwars.special;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.arena.Arena;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles the Warp Powder special item logic.
 * <p>
 * This class manages delayed teleportation for players, including particle effects
 * and providing a mechanism to access and cancel the teleportation task.
 * Each instance represents a single, cancellable teleportation attempt.
 * </p>
 */
public class WarpPowder {

    private static final Map<UUID, WarpPowder> warpTasks = new HashMap<>();

    private final Player player;
    private final IArena arena;
    private final BukkitTask task;
    private boolean cancelled = false;
    private ItemStack stack;
    /**
     * Creates and starts a new warp powder teleportation task for a player.
     * <p>
     * If the player already has an active warp task, it will be cancelled before starting the new one.
     * </p>
     * @param player       The player to teleport.
     * @param delayInTicks The delay in ticks before teleportation.
     * @param arena        The arena the player is in.
     */
    public WarpPowder(Player player, long delayInTicks, IArena arena, ItemStack stack) {
        // Cancel any existing warp task for this player
        if (isWarping(player)) {
            getWarpTask(player).cancel();
        }

        this.player = player;
        this.arena = arena;
        this.stack = stack;
        final Location startLoc = player.getLocation();
        final Location targetLoc = arena.getTeam(player).getSpawn();
        final String particleName = BedWars.getForCurrentVersion("FIREWORKS_SPARK", "FIREWORK_SHOOT", "FIREWORK_SHOOT");
        BedWars.debug("WarpPowder task created for " + player.getName() + " in arena " + arena.getArenaName() + " with a delay of " + delayInTicks + " ticks.");
        this.task = new BukkitRunnable() {
            final int circleElements = 40;
            final double radius = 1.0;
            long ticksRan = 0;

            @Override
            public void run() {
                // Task ends if player is offline, no longer in the arena, or time is up
                if (!player.isOnline() || !arena.isPlayer(player) || ticksRan > delayInTicks) {
                    if (player.isOnline() && arena.isPlayer(player)) {
                        // If the task completed successfully, teleport the player
                        if (ticksRan > delayInTicks) {
                            player.teleport(targetLoc);
                            BedWars.debug("WarpPowder: " + player.getName() + " teleported to team spawn.");
                        }
                    } else {
                        BedWars.debug("WarpPowder task for " + player.getName() + " cancelled due to player leaving/disconnecting.");
                    }
                    // Cancel this task and remove it from the map
                    WarpPowder.this.cancel();
                    return;
                }

                // Particle effects at start and target locations
                for (int i = 0; i < circleElements; i++) {
                    double angle = 2 * Math.PI * i / circleElements;
                    double x = radius * Math.cos(angle);
                    double z = radius * Math.sin(angle);

                    Location particleStart = startLoc.clone().add(x, 0.5, z);
                    Location particleTarget = targetLoc.clone().add(x, 0.5, z);

                    BedWars.nms.displayParticle(player, particleName, particleStart, 1);
                    BedWars.nms.displayParticle(player, particleName, particleTarget, 1);
                }
                ticksRan++;
            }
        }.runTaskTimer(BedWars.plugin, 0L, 1L);

        warpTasks.put(player.getUniqueId(), this);
    }

    /**
     * Cancels the teleportation task.
     * This will stop the runnable and remove the task from the active tasks map.
     */
    public void cancel() {
        if (!cancelled) {
            task.cancel();
            cancelled = true;
            BedWars.debug("WarpPowder task for " + player.getName() + " was cancelled.");
        }
        warpTasks.remove(player.getUniqueId());
    }

    public void cancel(boolean addItem) {
        if (!cancelled) {
            task.cancel();
            cancelled = true;
            BedWars.debug("WarpPowder task for " + player.getName() + " was cancelled.");
            if (addItem) {
                stack.setAmount(stack.getAmount() + 1);
                arena.getWarpPowderCooldowns().put(player.getUniqueId(), 0L);
            }
        }
        warpTasks.remove(player.getUniqueId());
    }

    /**
     * Gets the active warp task for a given player.
     *
     * @param player The player.
     * @return The {@link WarpPowder} instance, or null if there is no active task.
     */
    public static WarpPowder getWarpTask(Player player) {
        return warpTasks.get(player.getUniqueId());
    }

    /**
     * Checks if the current task has been cancelled.
     *
     * @return true if cancelled, false otherwise.
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Checks if a player is currently in a warp process.
     *
     * @param player The player.
     * @return true if the player has an active warp task, false otherwise.
     */
    public static boolean isWarping(Player player) {
        return warpTasks.containsKey(player.getUniqueId());
    }
}
