package io.github.dmhomes.listeners;

import io.github.dmhomes.DMHomesPlugin;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Listener for player events that affect teleportation
 */
@RequiredArgsConstructor
public final class PlayerListener implements Listener {

    private final DMHomesPlugin plugin;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(final @NotNull PlayerMoveEvent event) {
        Objects.requireNonNull(event, "Event cannot be null");
        
        final Player player = event.getPlayer();
        
        // Movement cancellation is handled in TeleportationManager
        // This is just for monitoring if needed in the future
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDamage(final @NotNull EntityDamageEvent event) {
        Objects.requireNonNull(event, "Event cannot be null");
        
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        final Player player = (Player) event.getEntity();
        
        // Check if player has active teleportation and cancel on damage is enabled
        if (this.plugin.getTeleportationManager().hasTeleportation(player)) {
            final boolean cancelOnDamage = this.plugin.getConfigManager().getConfig()
                .getBoolean("teleportation.cancel-on-damage", true);
            
            if (cancelOnDamage) {
                this.plugin.getTeleportationManager().cancelTeleportation(player, "damage");
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(final @NotNull PlayerQuitEvent event) {
        Objects.requireNonNull(event, "Event cannot be null");
        
        final Player player = event.getPlayer();
        
        // Cancel any active teleportation
        this.plugin.getTeleportationManager().cancelTeleportation(player);
        
        // Save player data
        this.plugin.getHomeDataManager().unloadPlayer(player.getUniqueId());
    }
}