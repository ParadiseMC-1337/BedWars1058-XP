package com.andrei1058.bedwars.special;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.language.Messages;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.andrei1058.bedwars.api.language.Language.getMsg;

public class WarpPowder {

    private static final Map<UUID, WarpPowder> warpTasks = new HashMap<>();

    private final Player player;
    private final IArena arena;
    private final BukkitTask task;
    private final ItemStack originalItem;
    private final ItemStack activeItem;
    private final int heldSlot;
    private boolean cancelled = false;
    private boolean completed = false;

    public WarpPowder(Player player, long delayInTicks, IArena arena, ItemStack originalItem, ItemStack activeItem, int heldSlot) {
        if (isWarping(player)) {
            getWarpTask(player).cancel();
        }

        this.player = player;
        this.arena = arena;
        this.originalItem = originalItem;
        this.activeItem = activeItem;
        this.heldSlot = heldSlot;

        final Location startLoc = player.getLocation();
        final Location targetLoc = arena.getTeam(player).getSpawn();
        final String particleName = BedWars.getForCurrentVersion("FIREWORKS_SPARK", "FIREWORK_SHOOT", "FIREWORK_SHOOT");

        this.task = new BukkitRunnable() {
            final int circleElements = 40;
            final double radius = 1.0;
            long ticksRan = 0;

            @Override
            public void run() {
                if (!player.isOnline() || !arena.isPlayer(player)) {
                    WarpPowder.this.cancel();
                    return;
                }

                if (ticksRan > delayInTicks) {
                    completed = true;
                    player.teleport(targetLoc);
                    WarpPowder.this.cancel();
                    return;
                }

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

    public void cancel() {
        cancel(false);
    }

    public void cancel(boolean refund) {
        if (!cancelled) {
            task.cancel();
            cancelled = true;
            if (player.isOnline()) {
                if (completed) {
                    if (originalItem != null) {
                        ItemStack remainingItem = originalItem.clone();
                        if (remainingItem.getAmount() > 1) {
                            remainingItem.setAmount(remainingItem.getAmount() - 1);
                            player.getInventory().setItem(heldSlot, remainingItem);
                        } else {
                            player.getInventory().setItem(heldSlot, null);
                        }
                    }
                    player.updateInventory();
                } else if (refund && originalItem != null) {
                    player.getInventory().setItem(heldSlot, originalItem.clone());
                    player.updateInventory();
                    arena.getWarpPowderCooldowns().put(player.getUniqueId(), 0L);
                    player.sendMessage(getMsg(player, Messages.SPECIAL_ITEMS_WARP_POWDER_CANCELLED));
                } else if (activeItem != null) {
                    ItemStack currentItem = player.getInventory().getItem(heldSlot);
                    if (currentItem != null && currentItem.getType() == activeItem.getType()) {
                        player.getInventory().setItem(heldSlot, null);
                        player.updateInventory();
                    }
                }
            }
        }
        warpTasks.remove(player.getUniqueId());
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public static WarpPowder getWarpTask(Player player) {
        return warpTasks.get(player.getUniqueId());
    }

    public static boolean isWarping(Player player) {
        return warpTasks.containsKey(player.getUniqueId());
    }
}
