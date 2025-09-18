package io.github.dmhomes.dialog;

import io.github.dmhomes.DMHomesPlugin;
import io.github.dmhomes.exceptions.DMHomesException;
import io.papermc.paper.dialog.Dialog;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Manages home creation using Paper Dialog API
 */
@RequiredArgsConstructor
public final class HomeCreationDialog {

    private final DMHomesPlugin plugin;
    
    // Store pending callbacks for text input handling
    private Consumer<String> pendingCallback;
    private Consumer<String> pendingRenameCallback;
    private Runnable pendingDeletionCallback;

    /**
     * Opens the home creation dialog for a player
     * @param player the player
     */
    public void openDialog(final @NotNull Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        
        // Check if player can create more homes
        if (!this.plugin.getHomeManager().canCreateHome(player)) {
            player.sendMessage(this.plugin.getMessageManager().getMessage("error-max-homes"));
            return;
        }
        
        try {
            final HomeDialogBuilder dialogBuilder = new HomeDialogBuilder(this.plugin);
            
            // Store callback for click handler BEFORE creating dialog
            this.setPendingCallback(homeName -> this.handleHomeCreation(player, homeName));
            
            final Dialog dialog = dialogBuilder.createHomeCreationDialog(player, homeName -> {
                this.handleHomeCreation(player, homeName);
            });
            
            player.showDialog(dialog);
            
        } catch (final Exception exception) {
            this.plugin.getLogger().severe("Failed to show home creation dialog for player " + player.getName() + ": " + exception.getMessage());
            player.sendMessage(Component.text("Wystąpił błąd podczas otwierania dialogu tworzenia domu!", NamedTextColor.RED));
        }
    }

    /**
     * Opens the home deletion confirmation dialog
     * @param player the player
     * @param homeName the home name to delete
     */
    public void openDeletionDialog(final @NotNull Player player, final @NotNull String homeName) {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(homeName, "Home name cannot be null");
        
        try {
            final HomeDialogBuilder dialogBuilder = new HomeDialogBuilder(this.plugin);
            final Dialog dialog = dialogBuilder.createHomeDeletionDialog(player, homeName, () -> {
                this.handleHomeDeletion(player, homeName);
            });
            
            // Store callback for click handler
            this.setPendingDeletionCallback(() -> this.handleHomeDeletion(player, homeName));
            
            player.showDialog(dialog);
            
        } catch (final Exception exception) {
            this.plugin.getLogger().severe("Failed to show home deletion dialog for player " + player.getName() + ": " + exception.getMessage());
            player.sendMessage(Component.text("Wystąpił błąd podczas otwierania dialogu usuwania domu!", NamedTextColor.RED));
        }
    }

    /**
     * Opens the home rename dialog
     * @param player the player
     * @param oldHomeName the current home name
     */
    public void openRenameDialog(final @NotNull Player player, final @NotNull String oldHomeName) {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(oldHomeName, "Old home name cannot be null");
        
        try {
            final HomeDialogBuilder dialogBuilder = new HomeDialogBuilder(this.plugin);
            final Dialog dialog = dialogBuilder.createHomeRenameDialog(player, oldHomeName, newHomeName -> {
                this.handleHomeRename(player, oldHomeName, newHomeName);
            });
            
            // Store callback for click handler
            this.setPendingRenameCallback(newHomeName -> this.handleHomeRename(player, oldHomeName, newHomeName));
            
            player.showDialog(dialog);
            
        } catch (final Exception exception) {
            this.plugin.getLogger().severe("Failed to show home rename dialog for player " + player.getName() + ": " + exception.getMessage());
            player.sendMessage(Component.text("Wystąpił błąd podczas otwierania dialogu zmiany nazwy domu!", NamedTextColor.RED));
        }
    }

    /**
     * Handles the home creation from dialog input
     * @param player the player
     * @param homeName the home name
     */
    private void handleHomeCreation(final @NotNull Player player, final @NotNull String homeName) {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(homeName, "Home name cannot be null");
        
        // Clean the home name
        final String cleanName = homeName.trim();
        
        // Validate home name
        if (cleanName.isEmpty() || cleanName.length() > 16) {
            player.sendMessage(this.plugin.getMessageManager().getMessage("error-invalid-name"));
            return;
        }
        
        try {
            final boolean success = this.plugin.getHomeManager()
                .createHome(player, cleanName, player.getLocation());
            
            if (success) {
                // Send success message with nice formatting
                player.sendMessage(Component.empty());
                player.sendMessage(Component.text("✓ Dom został utworzony pomyślnie!")
                    .color(NamedTextColor.GREEN)
                    .decorate(TextDecoration.BOLD));
                player.sendMessage(Component.text("Nazwa: ")
                    .color(NamedTextColor.GRAY)
                    .append(Component.text(cleanName).color(NamedTextColor.WHITE)));
                player.sendMessage(Component.text("Lokalizacja: ")
                    .color(NamedTextColor.GRAY)
                    .append(Component.text(player.getLocation().getBlockX() + ", " + 
                        player.getLocation().getBlockY() + ", " + 
                        player.getLocation().getBlockZ()).color(NamedTextColor.WHITE)));
                player.sendMessage(Component.empty());
            } else {
                // Check specific failure reasons
                if (this.plugin.getHomeManager().hasHome(player, cleanName)) {
                    player.sendMessage(Component.text("✗ Dom o tej nazwie już istnieje!")
                        .color(NamedTextColor.RED));
                } else if (!this.plugin.getHomeManager().canCreateHome(player)) {
                    player.sendMessage(this.plugin.getMessageManager()
                        .getMessage("error-max-homes"));
                } else {
                    player.sendMessage(this.plugin.getMessageManager()
                        .getMessage("error-invalid-name"));
                }
            }
        } catch (final DMHomesException exception) {
            this.plugin.getLogger().warning("Failed to create home for player " 
                + player.getName() + ": " + exception.getMessage());
            player.sendMessage(Component.text("✗ Wystąpił błąd podczas tworzenia domu!")
                .color(NamedTextColor.RED));
        }
    }

    /**
     * Handles home deletion from dialog
     * @param player the player
     * @param homeName the home name to delete
     */
    private void handleHomeDeletion(final @NotNull Player player, final @NotNull String homeName) {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(homeName, "Home name cannot be null");
        
        try {
            final boolean success = this.plugin.getHomeManager().deleteHome(player, homeName);
            
            if (success) {
                player.sendMessage(Component.text("✓ Dom '" + homeName + "' został usunięty!")
                    .color(NamedTextColor.GREEN));
            } else {
                player.sendMessage(Component.text("✗ Nie udało się usunąć domu '" + homeName + "'!")
                    .color(NamedTextColor.RED));
            }
        } catch (final DMHomesException exception) {
            this.plugin.getLogger().warning("Failed to delete home for player " 
                + player.getName() + ": " + exception.getMessage());
            player.sendMessage(Component.text("✗ Wystąpił błąd podczas usuwania domu!")
                .color(NamedTextColor.RED));
        }
    }

    /**
     * Handles home renaming from dialog
     * @param player the player
     * @param oldHomeName the old home name
     * @param newHomeName the new home name
     */
    private void handleHomeRename(final @NotNull Player player, final @NotNull String oldHomeName, final @NotNull String newHomeName) {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(oldHomeName, "Old home name cannot be null");
        Objects.requireNonNull(newHomeName, "New home name cannot be null");
        
        // Validate new home name
        if (newHomeName.isEmpty() || newHomeName.length() > 16) {
            player.sendMessage(this.plugin.getMessageManager().getMessage("error-invalid-name"));
            return;
        }
        
        try {
            final boolean success = this.plugin.getHomeManager().renameHome(player, oldHomeName, newHomeName);
            
            if (success) {
                player.sendMessage(this.plugin.getMessageManager()
                    .getMessage("home-renamed", "old_name", oldHomeName, "home_name", newHomeName));
            } else {
                if (this.plugin.getHomeManager().hasHome(player, newHomeName)) {
                    player.sendMessage(this.plugin.getMessageManager()
                        .getMessage("error-home-exists", "home_name", newHomeName));
                } else {
                    player.sendMessage(this.plugin.getMessageManager()
                        .getMessage("error-generic", "error", "Failed to rename home"));
                }
                this.plugin.getTeleportationManager().playErrorSound(player);
            }
        } catch (final DMHomesException exception) {
            player.sendMessage(this.plugin.getMessageManager()
                .getMessage("error-generic", "error", "Failed to rename home"));
            this.plugin.getTeleportationManager().playErrorSound(player);
        }
    }

    /**
     * Gets the dialog builder instance
     * @return the dialog builder
     */
    public HomeDialogBuilder getDialogBuilder() {
        return new HomeDialogBuilder(this.plugin);
    }

    /**
     * Sets the pending callback for home creation
     * @param callback the callback to set
     */
    public void setPendingCallback(final Consumer<String> callback) {
        this.pendingCallback = callback;
    }

    /**
     * Sets the pending callback for home renaming
     * @param callback the callback to set
     */
    public void setPendingRenameCallback(final Consumer<String> callback) {
        this.pendingRenameCallback = callback;
    }

    /**
     * Sets the pending callback for home deletion
     * @param callback the callback to set
     */
    public void setPendingDeletionCallback(final Runnable callback) {
        this.pendingDeletionCallback = callback;
    }

    /**
     * Gets the pending callback for home creation
     * @return the pending callback
     */
    public Consumer<String> getPendingCallback() {
        return this.pendingCallback;
    }

    /**
     * Gets the pending callback for home renaming
     * @return the pending rename callback
     */
    public Consumer<String> getPendingRenameCallback() {
        return this.pendingRenameCallback;
    }

    /**
     * Gets the pending callback for home deletion
     * @return the pending deletion callback
     */
    public Runnable getPendingDeletionCallback() {
        return this.pendingDeletionCallback;
    }

    /**
     * Clears the pending callbacks
     */
    public void clearPendingCallbacks() {
        this.pendingCallback = null;
        this.pendingRenameCallback = null;
        this.pendingDeletionCallback = null;
    }
}