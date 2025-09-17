package io.github.dmhomes.listeners;

import io.github.dmhomes.DMHomesPlugin;
import io.github.dmhomes.gui.BaseGUI;
import io.github.dmhomes.gui.HomeManagementGUI;
import io.github.dmhomes.gui.IconSelectionGUI;
import io.github.dmhomes.gui.MainHomesGUI;
import io.github.dmhomes.data.Home;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Event listener for all GUI interactions
 */
@RequiredArgsConstructor
public final class GUIListener implements Listener {

    private final DMHomesPlugin plugin;

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(final @NotNull InventoryClickEvent event) {
        Objects.requireNonNull(event, "Event cannot be null");
        
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        final Player player = (Player) event.getWhoClicked();
        final Inventory clickedInventory = event.getClickedInventory();
        
        if (clickedInventory == null) {
            return;
        }
        
        final InventoryHolder holder = clickedInventory.getHolder();
        
        if (!(holder instanceof BaseGUI)) {
            return;
        }
        
        // Cancel the event to prevent item movement
        event.setCancelled(true);
        
        final BaseGUI gui = (BaseGUI) holder;
        final int slot = event.getSlot();
        
        try {
            if (gui instanceof MainHomesGUI) {
                this.handleMainHomesGUIClick((MainHomesGUI) gui, player, slot, event.isLeftClick());
            } else if (gui instanceof HomeManagementGUI) {
                this.handleHomeManagementGUIClick((HomeManagementGUI) gui, player, slot);
            } else if (gui instanceof IconSelectionGUI) {
                this.handleIconSelectionGUIClick((IconSelectionGUI) gui, player, slot);
            }
        } catch (final Exception exception) {
            this.plugin.getLogger().severe("Error handling GUI click for player " + player.getName() + ": " + exception.getMessage());
            player.sendMessage(this.plugin.getMessageManager().getMessage("error-generic", "error", "An error occurred"));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(final @NotNull InventoryCloseEvent event) {
        Objects.requireNonNull(event, "Event cannot be null");
        
        final InventoryHolder holder = event.getInventory().getHolder();
        
        if (holder instanceof BaseGUI) {
            final BaseGUI gui = (BaseGUI) holder;
            gui.onClose();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryDrag(final @NotNull InventoryDragEvent event) {
        Objects.requireNonNull(event, "Event cannot be null");
        
        final InventoryHolder holder = event.getInventory().getHolder();
        
        if (holder instanceof BaseGUI) {
            // Prevent item dragging in custom GUIs
            event.setCancelled(true);
        }
    }

    /**
     * Handles clicks in the MainHomesGUI
     * @param gui the GUI instance
     * @param player the clicking player
     * @param slot the clicked slot
     * @param isLeftClick whether it was a left click
     */
    private void handleMainHomesGUIClick(final @NotNull MainHomesGUI gui, 
                                       final @NotNull Player player, 
                                       final int slot, 
                                       final boolean isLeftClick) {
        
        // Debug logging
        this.plugin.getLogger().info("Player " + player.getName() + " clicked slot " + slot + 
            " (left=" + isLeftClick + ")");
        
        // Add more detailed debugging
        final var item = gui.getInventory().getItem(slot);
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            final String displayName = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                .serialize(item.getItemMeta().displayName());
            this.plugin.getLogger().info("Item display name: '" + displayName + "'");
            this.plugin.getLogger().info("Item material: " + item.getType());
        } else {
            this.plugin.getLogger().info("Item is null or has no display name");
        }
        
        this.plugin.getLogger().info("isCloseButton: " + gui.isCloseButton(slot));
        this.plugin.getLogger().info("isAvailableSlot: " + gui.isAvailableSlot(slot));
        this.plugin.getLogger().info("isUnavailableSlot: " + gui.isUnavailableSlot(slot));
        this.plugin.getLogger().info("isOccupiedSlot: " + gui.isOccupiedSlot(slot));
        
        if (gui.isCloseButton(slot)) {
            gui.close();
            return;
        }
        
        if (gui.isAvailableSlot(slot)) {
            if (isLeftClick) {
                this.handleCreateHome(gui, player);
            }
            return;
        }
        
        if (gui.isUnavailableSlot(slot)) {
            // Show upgrade message
            player.sendMessage(this.plugin.getMessageManager()
                .getMessage("error-upgrade-required"));
            return;
        }
        
        if (gui.isOccupiedSlot(slot)) {
            final Home home = gui.getHomeBySlot(slot);
            if (home != null) {
                if (isLeftClick) {
                    this.handleTeleportToHome(gui, player, home);
                } else {
                    this.handleOpenHomeManagement(gui, player, home);
                }
            }
        }
    }

    /**
     * Handles clicks in the HomeManagementGUI
     * @param gui the GUI instance
     * @param player the clicking player
     * @param slot the clicked slot
     */
    private void handleHomeManagementGUIClick(final @NotNull HomeManagementGUI gui, 
                                            final @NotNull Player player, 
                                            final int slot) {
        
        if (gui.isBackButton(slot)) {
            gui.getParentGUI().open();
            return;
        }
        
        if (gui.isRenameButton(slot)) {
            this.handleRenameHome(gui, player);
            return;
        }
        
        if (gui.isDeleteButton(slot)) {
            this.handleDeleteHome(gui, player);
            return;
        }
        
        if (gui.isChangeIconButton(slot)) {
            this.handleOpenIconSelection(gui, player);
        }
    }

    /**
     * Handles clicks in the IconSelectionGUI
     * @param gui the GUI instance
     * @param player the clicking player
     * @param slot the clicked slot
     */
    private void handleIconSelectionGUIClick(final @NotNull IconSelectionGUI gui, 
                                           final @NotNull Player player, 
                                           final int slot) {
        
        if (gui.isBackButton(slot)) {
            gui.getParentGUI().open();
            return;
        }
        
        final String iconMaterial = gui.getIconMaterial(slot);
        if (iconMaterial != null) {
            this.handleSelectIcon(gui, player, iconMaterial);
        }
    }

    /**
     * Handles creating a new home
     * @param gui the GUI instance
     * @param player the player
     */
    private void handleCreateHome(final @NotNull MainHomesGUI gui, final @NotNull Player player) {
        // Check if player can create more homes
        if (!this.plugin.getHomeManager().canCreateHome(player)) {
            player.sendMessage(this.plugin.getMessageManager().getMessage("error-max-homes"));
            return;
        }
        
        gui.close();
        
        // Show dialog for home name input
        this.showCreateHomeDialog(player);
    }

    /**
     * Shows the create home dialog
     * @param player the player
     */
    private void showCreateHomeDialog(final @NotNull Player player) {
        // Use the new Paper Dialog API
        this.plugin.getHomeCreationDialog().openDialog(player);
    }

    /**
     * Handles teleporting to a home
     * @param gui the GUI instance
     * @param player the player
     * @param home the home to teleport to
     */
    private void handleTeleportToHome(final @NotNull MainHomesGUI gui, final @NotNull Player player, final @NotNull Home home) {
        gui.close();
        
        // Don't send message here - TeleportationManager will handle it
        
        // Use our teleportation manager
        this.plugin.getTeleportationManager().teleportToHome(player, home);
    }

    /**
     * Handles opening the home management GUI
     * @param gui the GUI instance
     * @param player the player
     * @param home the home to manage
     */
    private void handleOpenHomeManagement(final @NotNull MainHomesGUI gui, final @NotNull Player player, final @NotNull Home home) {
        final HomeManagementGUI managementGUI = new HomeManagementGUI(this.plugin, player, home, gui);
        managementGUI.open();
    }

    /**
     * Handles renaming a home
     * @param gui the GUI instance
     * @param player the player
     */
    private void handleRenameHome(final @NotNull HomeManagementGUI gui, final @NotNull Player player) {
        // Use the new Paper Dialog API for renaming
        this.plugin.getHomeCreationDialog().openRenameDialog(player, gui.getHome().getName());
    }

    /**
     * Handles deleting a home
     * @param gui the GUI instance
     * @param player the player
     */
    private void handleDeleteHome(final @NotNull HomeManagementGUI gui, final @NotNull Player player) {
        // Use the new Paper Dialog API for deletion confirmation
        this.plugin.getHomeCreationDialog().openDeletionDialog(player, gui.getHome().getName());
    }

    /**
     * Handles opening the icon selection GUI
     * @param gui the GUI instance
     * @param player the player
     */
    private void handleOpenIconSelection(final @NotNull HomeManagementGUI gui, final @NotNull Player player) {
        final IconSelectionGUI iconGUI = new IconSelectionGUI(this.plugin, player, gui.getHome(), gui);
        iconGUI.open();
    }

    /**
     * Handles selecting an icon
     * @param gui the GUI instance
     * @param player the player
     * @param iconMaterial the selected icon material
     */
    private void handleSelectIcon(final @NotNull IconSelectionGUI gui, final @NotNull Player player, final @NotNull String iconMaterial) {
        final Home home = gui.getHome();
        
        // Set the custom icon
        this.plugin.getHomeDataManager().setHomeIcon(player.getUniqueId(), home.getName(), iconMaterial);
        
        player.sendMessage(this.plugin.getMessageManager()
            .getMessage("home-icon-changed", "home_name", home.getName()));
        
        // Close current GUI and refresh the main homes GUI
        gui.close();
        
        // Schedule GUI refresh for next tick to ensure proper closing
        this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
            // Get the main homes GUI and refresh it
            final var mainGUI = new MainHomesGUI(this.plugin, player);
            mainGUI.open();
        });
    }
}