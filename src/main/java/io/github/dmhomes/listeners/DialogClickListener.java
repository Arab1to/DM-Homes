package io.github.dmhomes.listeners;

import io.github.dmhomes.DMHomesPlugin;
import io.github.dmhomes.gui.MainHomesGUI;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Handles dialog button click events for home management
 */
public class DialogClickListener implements Listener {

    private final DMHomesPlugin plugin;

    public DialogClickListener(final @NotNull DMHomesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    @SuppressWarnings("UnstableApiUsage")
    public void onDialogClick(final @NotNull PlayerCustomClickEvent event) {
        final Key key = event.getIdentifier();
        final String keyString = key.asString();


        // Check if this is one of our dialog actions
        if (!keyString.startsWith("dmhomes:")) {
            return;
        }

        // Get player from the event using PlayerGameConnection
        Player player = null;
        try {
            final var connection = event.getCommonConnection();
            
            // Try to get player from PlayerGameConnection
            if (connection instanceof io.papermc.paper.connection.PlayerGameConnection gameConnection) {
                player = gameConnection.getPlayer();
            } else {
                this.plugin.getLogger().warning("Connection is not PlayerGameConnection: " + connection.getClass().getSimpleName());
                return;
            }
        } catch (final Exception exception) {
            this.plugin.getLogger().warning("Failed to get player from dialog event: " + exception.getMessage());
            return;
        }

        if (player == null) {
            this.plugin.getLogger().warning("Player is null from dialog event");
            return;
        }


        // Handle different dialog actions
        if (keyString.startsWith("dmhomes:create_home/")) {
            handleHomeCreation(player, event, keyString);
        } else if (keyString.startsWith("dmhomes:delete_home/")) {
            handleHomeDeletion(player, event, keyString);
        } else if (keyString.startsWith("dmhomes:rename_home/")) {
            handleHomeRename(player, event, keyString);
        }
    }

    private void handleHomeCreation(final @NotNull Player player, final @NotNull PlayerCustomClickEvent event, final @NotNull String keyString) {

        try {
            final UUID playerId = UUID.fromString(keyString.substring("dmhomes:create_home/".length()));

            // Verify this is the correct player
            if (!player.getUniqueId().equals(playerId)) {
                this.plugin.getLogger().warning("Player UUID mismatch in dialog");
                return;
            }

            // Get the dialog response view to read input
            final var responseView = event.getDialogResponseView();
            if (responseView == null) {
                this.plugin.getLogger().warning("Dialog response view is null");
                player.sendMessage(Component.text("✗ Nie udało się odczytać danych z dialogu!", NamedTextColor.RED));
                return;
            }


            // Get the home name from input
            final String homeName = responseView.getText("home_name");

            if (homeName == null || homeName.trim().isEmpty()) {
                player.sendMessage(Component.text("✗ Nazwa domu nie może być pusta!", NamedTextColor.RED));
                return;
            }

            final String cleanName = homeName.trim();

            // Validate home name
            if (cleanName.length() > 16) {
                player.sendMessage(Component.text("✗ Nazwa domu jest za długa! Maksymalnie 16 znaków.", NamedTextColor.RED));
                return;
            }

            if (!cleanName.matches("^[a-zA-Z0-9_]+$")) {
                player.sendMessage(Component.text("✗ Nazwa domu może zawierać tylko litery, cyfry i _", NamedTextColor.RED));
                return;
            }


            // Create the home directly here instead of using callback
            try {
                final boolean success = this.plugin.getHomeManager()
                        .createHome(player, cleanName, player.getLocation());


                if (success) {
                    // Send configurable success message
                    player.sendMessage(this.plugin.getMessageManager()
                        .getMessage("home-created", "home_name", cleanName));
                } else {
                    // Check specific failure reasons
                    if (this.plugin.getHomeManager().hasHome(player, cleanName)) {
                        player.sendMessage(this.plugin.getMessageManager()
                            .getMessage("error-home-exists", "home_name", cleanName));
                    } else if (!this.plugin.getHomeManager().canCreateHome(player)) {
                        player.sendMessage(this.plugin.getMessageManager()
                                .getMessage("error-max-homes"));
                    } else {
                        player.sendMessage(this.plugin.getMessageManager()
                                .getMessage("error-invalid-name"));
                    }
                    this.plugin.getTeleportationManager().playErrorSound(player);
                }
            } catch (final Exception exception) {
                player.sendMessage(this.plugin.getMessageManager()
                    .getMessage("error-generic", "error", "Failed to create home"));
                this.plugin.getTeleportationManager().playErrorSound(player);
            }

            // Clear any pending callbacks
            this.plugin.getHomeCreationDialog().clearPendingCallbacks();

        } catch (final Exception exception) {
            player.sendMessage(this.plugin.getMessageManager()
                .getMessage("error-generic", "error", "Dialog processing failed"));
            this.plugin.getTeleportationManager().playErrorSound(player);
        }
    }

    private void handleHomeDeletion(final @NotNull Player player, final @NotNull PlayerCustomClickEvent event, final @NotNull String keyString) {
        try {
            final UUID playerId = UUID.fromString(keyString.substring("dmhomes:delete_home/".length()));

            // Verify this is the correct player
            if (!player.getUniqueId().equals(playerId)) {
                return;
            }

            // Execute the callback
            final var homeCreationDialog = this.plugin.getHomeCreationDialog();
            final var callback = homeCreationDialog.getPendingDeletionCallback();
            if (callback != null) {
                callback.run();
                homeCreationDialog.clearPendingCallbacks();
                
                // Close any open GUI and refresh the main homes GUI
                this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                    final var gui = new MainHomesGUI(this.plugin, player);
                    gui.open();
                });
            }

        } catch (final Exception exception) {
            this.plugin.getLogger().severe("Error handling home deletion dialog click: " + exception.getMessage());
            player.sendMessage(Component.text("✗ Wystąpił błąd podczas przetwarzania dialogu!", NamedTextColor.RED));
        }
    }

    private void handleHomeRename(final @NotNull Player player, final @NotNull PlayerCustomClickEvent event, final @NotNull String keyString) {
        try {
            final UUID playerId = UUID.fromString(keyString.substring("dmhomes:rename_home/".length()));

            // Verify this is the correct player
            if (!player.getUniqueId().equals(playerId)) {
                return;
            }

            // Get the dialog response view to read input
            final var responseView = event.getDialogResponseView();
            if (responseView == null) {
                player.sendMessage(Component.text("✗ Nie udało się odczytać danych z dialogu!", NamedTextColor.RED));
                return;
            }

            // Get the new home name from input
            final String newHomeName = responseView.getText("new_home_name");
            if (newHomeName == null || newHomeName.trim().isEmpty()) {
                player.sendMessage(Component.text("✗ Nowa nazwa domu nie może być pusta!", NamedTextColor.RED));
                return;
            }

            final String cleanName = newHomeName.trim();

            // Validate home name
            if (cleanName.length() > 16) {
                player.sendMessage(Component.text("✗ Nazwa domu jest za długa! Maksymalnie 16 znaków.", NamedTextColor.RED));
                return;
            }

            if (!cleanName.matches("^[a-zA-Z0-9_]+$")) {
                player.sendMessage(Component.text("✗ Nazwa domu może zawierać tylko litery, cyfry i _", NamedTextColor.RED));
                return;
            }

            // Execute the callback
            final var homeCreationDialog = this.plugin.getHomeCreationDialog();
            final Consumer<String> callback = homeCreationDialog.getPendingRenameCallback();
            if (callback != null) {
                callback.accept(cleanName);
                homeCreationDialog.clearPendingCallbacks();
                
                // Close any open GUI and refresh the main homes GUI
                this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                    final var gui = new MainHomesGUI(this.plugin, player);
                    gui.open();
                });
            }

        } catch (final Exception exception) {
            this.plugin.getLogger().severe("Error handling home rename dialog click: " + exception.getMessage());
            player.sendMessage(Component.text("✗ Wystąpił błąd podczas przetwarzania dialogu!", NamedTextColor.RED));
        }
    }
}