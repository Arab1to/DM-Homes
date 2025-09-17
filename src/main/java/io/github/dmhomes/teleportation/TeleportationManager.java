package io.github.dmhomes.teleportation;

import io.github.dmhomes.DMHomesPlugin;
import io.github.dmhomes.data.Home;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages teleportation with warmup, countdown, and effects
 */
@RequiredArgsConstructor
public final class TeleportationManager {

    private final DMHomesPlugin plugin;
    private final Map<UUID, TeleportationTask> activeTeleportations = new ConcurrentHashMap<>();

    /**
     * Starts a teleportation to a home with warmup
     * @param player the player to teleport
     * @param home the home to teleport to
     */
    public void teleportToHome(final @NotNull Player player, final @NotNull Home home) {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(home, "Home cannot be null");

        // Cancel any existing teleportation
        this.cancelTeleportation(player);

        final int warmupTime = this.plugin.getConfigManager().getConfig()
            .getInt("teleportation.warmup-time", 5);

        if (warmupTime <= 0) {
            // Instant teleportation
            this.performTeleportation(player, home);
            return;
        }

        // Start warmup
        final TeleportationTask task = new TeleportationTask(player, home, warmupTime);
        this.activeTeleportations.put(player.getUniqueId(), task);
        task.start();

        // Play start sound
        this.playSound(player, "teleport-start");
    }

    /**
     * Cancels an active teleportation
     * @param player the player
     * @param reason the cancellation reason
     */
    public void cancelTeleportation(final @NotNull Player player, final @NotNull String reason) {
        final TeleportationTask task = this.activeTeleportations.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
            
            // Send cancellation message and title
            final Component message = this.plugin.getMessageManager().getMessage("teleportation.teleport-cancelled-" + reason);
            player.sendMessage(message);
            
            // Show cancellation title
            final Component title = this.plugin.getMessageManager().getMessage("teleportation.messages.teleport-cancelled-title");
            final Component subtitle = this.plugin.getMessageManager().getMessage("teleportation.messages.teleport-cancelled-subtitle-" + reason);
            
            final Title cancelTitle = Title.title(
                title,
                subtitle,
                Title.Times.times(Duration.ofMillis(250), Duration.ofSeconds(2), Duration.ofMillis(500))
            );
            
            player.showTitle(cancelTitle);
            
            // Play cancel sound
            this.playSound(player, "teleport-cancel");
        }
    }

    /**
     * Cancels an active teleportation without reason
     * @param player the player
     */
    public void cancelTeleportation(final @NotNull Player player) {
        final TeleportationTask task = this.activeTeleportations.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }

    /**
     * Checks if a player has an active teleportation
     * @param player the player
     * @return true if teleportation is active
     */
    public boolean hasTeleportation(final @NotNull Player player) {
        return this.activeTeleportations.containsKey(player.getUniqueId());
    }

    /**
     * Performs the actual teleportation
     * @param player the player
     * @param home the home
     */
    private void performTeleportation(final @NotNull Player player, final @NotNull Home home) {
        final Location location = home.getLocation();
        
        // Show black screen effect
        if (this.plugin.getConfigManager().getConfig().getBoolean("teleportation.blackscreen-effect", true)) {
            this.showBlackScreen(player);
        }

        // Teleport after 95 ticks (4.75 seconds) to sync with black screen
        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
            player.teleport(location);
            
            // Play end sound
            this.playSound(player, "teleport-end");
            
            // Send success message
            final Component message = this.plugin.getMessageManager()
                .getMessage("teleport-success", "home_name", home.getName());
            player.sendMessage(message);
            
            // Fade out black screen
            this.fadeOutBlackScreen(player);
            
        }, 95L);
    }

    /**
     * Shows the black screen effect
     * @param player the player
     */
    private void showBlackScreen(final @NotNull Player player) {
        final Component title = this.plugin.getMessageManager().getMessage("teleportation.messages.blackscreen-title");
        final Title blackscreenTitle = Title.title(
            title,
            Component.empty(),
            Title.Times.times(Duration.ofMillis(0), Duration.ofSeconds(1), Duration.ofMillis(0))
        );
        
        player.showTitle(blackscreenTitle);
    }

    /**
     * Fades out the black screen effect
     * @param player the player
     */
    private void fadeOutBlackScreen(final @NotNull Player player) {
        final int fadeDuration = this.plugin.getConfigManager().getConfig()
            .getInt("teleportation.blackscreen-duration", 50);
        
        // Clear title after fade duration
        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
            player.clearTitle();
        }, fadeDuration);
    }

    /**
     * Plays a sound for the player
     * @param player the player
     * @param soundKey the sound configuration key
     */
    private void playSound(final @NotNull Player player, final @NotNull String soundKey) {
        // Check if sounds are enabled
        if (!this.plugin.getConfigManager().getConfig().getBoolean("teleportation.sounds.enabled", true)) {
            return;
        }
        
        final String soundName = this.plugin.getConfigManager().getConfig()
            .getString("teleportation.sounds." + soundKey);
        
        if (soundName == null || soundName.isEmpty()) {
            return;
        }

        try {
            if (soundName.contains(":")) {
                // Custom sound (ItemsAdder or other) - play directly to player
                this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                    try {
                        // Use Bukkit's built-in method for custom sounds
                        player.playSound(player.getLocation(), soundName, 1.0f, 1.0f);
                    } catch (final Exception e) {
                        // Fallback to command if direct method fails
                        this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), 
                            "execute at " + player.getName() + " run playsound " + soundName + " master " + player.getName() + " ~ ~ ~ 1.0 1.0 0.0");
                    }
                });
            } else {
                // Vanilla sound
                try {
                    String cleanSoundName = soundName.toUpperCase();
                    if (cleanSoundName.startsWith("MINECRAFT:")) {
                        cleanSoundName = cleanSoundName.substring(10);
                    }
                    cleanSoundName = cleanSoundName.replace(".", "_");
                    
                    final Sound sound = Sound.valueOf(cleanSoundName);
                    player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
                } catch (final IllegalArgumentException e) {
                    // If vanilla sound parsing fails, try as custom sound
                    player.playSound(player.getLocation(), soundName, 1.0f, 1.0f);
                }
            }
        } catch (final Exception exception) {
            this.plugin.getLogger().warning("Failed to play sound: " + soundName);
            this.plugin.getLogger().warning("Error: " + exception.getMessage());
        }
    }

    /**
     * Teleportation task that handles warmup and countdown
     */
    private final class TeleportationTask extends BukkitRunnable {
        
        private final Player player;
        private final Home home;
        private final Location startLocation;
        private int timeLeft;
        private BukkitTask task;

        public TeleportationTask(final @NotNull Player player, final @NotNull Home home, final int warmupTime) {
            this.player = player;
            this.home = home;
            this.startLocation = player.getLocation().clone();
            this.timeLeft = warmupTime;
        }

        public void start() {
            this.task = this.runTaskTimer(TeleportationManager.this.plugin, 0L, 20L);
        }

        @Override
        public void run() {
            if (!this.player.isOnline()) {
                this.cancel();
                return;
            }

            // Check if player moved (if enabled)
            if (TeleportationManager.this.plugin.getConfigManager().getConfig()
                .getBoolean("teleportation.cancel-on-move", true)) {
                if (this.startLocation.distance(this.player.getLocation()) > 0.5) {
                    TeleportationManager.this.cancelTeleportation(this.player, "move");
                    return;
                }
            }

            if (this.timeLeft <= 0) {
                // Teleportation time!
                TeleportationManager.this.activeTeleportations.remove(this.player.getUniqueId());
                TeleportationManager.this.performTeleportation(this.player, this.home);
                this.cancel();
                return;
            }

            // Show countdown
            this.showCountdown();
            this.timeLeft--;
        }

        private void showCountdown() {
            final Component title = TeleportationManager.this.plugin.getMessageManager()
                .getMessage("teleportation.messages.warmup-title");
            final Component subtitle = TeleportationManager.this.plugin.getMessageManager()
                .getMessage("teleportation.messages.warmup-subtitle", "time", String.valueOf(this.timeLeft));

            final Title countdownTitle = Title.title(
                title,
                subtitle,
                Title.Times.times(Duration.ofMillis(0), Duration.ofMillis(1200), Duration.ofMillis(200))
            );

            this.player.showTitle(countdownTitle);
        }

        @Override
        public void cancel() {
            if (this.task != null) {
                this.task.cancel();
            }
            super.cancel();
        }
    }
}