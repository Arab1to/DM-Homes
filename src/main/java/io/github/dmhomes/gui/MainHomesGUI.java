package io.github.dmhomes.gui;

import io.github.dmhomes.DMHomesPlugin;
import io.github.dmhomes.data.PlayerHomeData;
import io.github.dmhomes.data.Home;
import io.github.dmhomes.utils.GUIUtils;
import io.github.dmhomes.utils.ItemBuilder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * Main homes GUI that displays all player homes and available slots
 */
public final class MainHomesGUI extends BaseGUI {

    private static final String GUI_NAME = "main-menu";

    /**
     * Creates a new MainHomesGUI instance
     * @param plugin the plugin instance
     * @param player the player viewing the GUI
     */
    public MainHomesGUI(final @NotNull DMHomesPlugin plugin, final @NotNull Player player) {
        super(
            plugin, 
            player, 
            plugin.getConfigManager().getGuiTitle(GUI_NAME),
            plugin.getConfigManager().getGuiSize(GUI_NAME)
        );
    }

    @Override
    protected void setupGUI() {
        this.refresh();
    }

    @Override
    public void refresh() {
        // Clear the inventory first
        this.inventory.clear();
        
        try {
            this.populateHomes();
            this.addCloseButton();
        } catch (final Exception exception) {
            this.plugin.getLogger().severe("Error refreshing MainHomesGUI for player " 
                + this.player.getName() + ": " + exception.getMessage());
        }
    }

    /**
     * Populates the GUI with home slots (both occupied and available)
     * Only uses slots 1-7 for homes
     */
    private void populateHomes() {
        final List<Home> playerHomes = this.plugin.getHomeManager().getHomes(this.player);
        final int maxHomes = this.plugin.getHomeManager().getMaxHomes(this.player);
        final int maxGuiSlots = 7; // Fixed to 7 slots for homes
        final PlayerHomeData playerData = this.plugin.getHomeDataManager().getPlayerData(this.player);
        
        // Add occupied home slots (slots 1-7)
        if (playerHomes != null) {
            for (int i = 0; i < Math.min(playerHomes.size(), 7); i++) {
                final Home home = playerHomes.get(i);
                final ItemStack homeItem = this.createOccupiedSlotItem(home, playerData);
                if (homeItem != null) {
                    this.inventory.setItem(i + 1, homeItem); // Slots 1-7
                }
            }
        }
        
        // Add available slots (slots 1-7)
        final int currentHomes = playerHomes != null ? playerHomes.size() : 0;
        final int playerMaxSlots = Math.min(maxGuiSlots, maxHomes == -1 ? maxGuiSlots : maxHomes);
        final int availableSlots = Math.max(0, playerMaxSlots - currentHomes);
        
        for (int i = 0; i < availableSlots && (currentHomes + i) < 7; i++) {
            final ItemStack availableSlotItem = this.createAvailableSlotItem();
            if (availableSlotItem != null) {
                this.inventory.setItem(currentHomes + i + 1, availableSlotItem); // Slots 1-7
            }
        }
        
        // Add unavailable slots (slots 1-7)
        if (!this.player.hasPermission("dmhomes.admin")) {
            final int totalUsedSlots = currentHomes + availableSlots;
            final int unavailableSlots = Math.min(7, 7) - totalUsedSlots; // Show up to 7 total slots
            
            for (int i = 0; i < unavailableSlots && (totalUsedSlots + i) < 7; i++) {
                final ItemStack unavailableSlotItem = this.createUnavailableSlotItem();
                if (unavailableSlotItem != null) {
                    this.inventory.setItem(totalUsedSlots + i + 1, unavailableSlotItem); // Slots 1-7
                }
            }
        }
    }

    /**
     * Creates an item for an occupied home slot
     * @param home the home
     * @param playerData the player's data
     * @return the item stack
     */
    private ItemStack createOccupiedSlotItem(final @NotNull Home home, final @NotNull PlayerHomeData playerData) {
        Objects.requireNonNull(home, "Home cannot be null");
        Objects.requireNonNull(playerData, "Player data cannot be null");
        
        final ConfigurationSection itemConfig = this.plugin.getConfigManager()
            .getItemSection(GUI_NAME, "occupied-slot");
        
        if (itemConfig == null) {
            this.plugin.getLogger().warning("Missing occupied-slot configuration for main-menu GUI");
            return null;
        }
        
        // Check if player has a custom icon for this home
        final String customIcon = playerData.getHomeIcon(home.getName());
        if (customIcon != null) {
            // Create a modified configuration with custom material
            final ConfigurationSection customConfig = this.plugin.getConfig().createSection("temp");
            customConfig.set("material", customIcon);
            customConfig.set("name", itemConfig.getString("name"));
            customConfig.set("lore", itemConfig.getStringList("lore"));
            
            return this.createHomeItemWithPlaceholders(customConfig, home);
        }
        
        return this.createHomeItemWithPlaceholders(itemConfig, home);
    }

    /**
     * Creates an item with home placeholders replaced
     * @param config the item configuration
     * @param home the home
     * @return the item stack
     */
    private ItemStack createHomeItemWithPlaceholders(final @NotNull ConfigurationSection config, final @NotNull Home home) {
        final String homeName = home.getName();
        final String homeWorld = home.getLocation().getWorld() != null ? 
            home.getLocation().getWorld().getName() : "unknown";
        final String homeX = String.valueOf(home.getLocation().getBlockX());
        final String homeY = String.valueOf(home.getLocation().getBlockY());
        final String homeZ = String.valueOf(home.getLocation().getBlockZ());
        
        return ItemBuilder.createFromConfigWithPlaceholders(config,
            "home_name", homeName,
            "home_world", homeWorld,
            "home_x", homeX,
            "home_y", homeY,
            "home_z", homeZ
        );
    }

    /**
     * Creates an item for an available home slot
     * @return the item stack
     */
    private ItemStack createAvailableSlotItem() {
        final ConfigurationSection itemConfig = this.plugin.getConfigManager()
            .getItemSection(GUI_NAME, "available-slot");
        
        if (itemConfig == null) {
            this.plugin.getLogger().warning("Missing available-slot configuration for main-menu GUI");
            return null;
        }
        
        return ItemBuilder.createFromConfig(itemConfig);
    }

    /**
     * Creates an item for an unavailable home slot
     * @return the item stack
     */
    private ItemStack createUnavailableSlotItem() {
        final ConfigurationSection itemConfig = this.plugin.getConfigManager()
            .getItemSection(GUI_NAME, "unavailable-slot");
        
        if (itemConfig == null) {
            this.plugin.getLogger().warning("Missing unavailable-slot configuration for main-menu GUI");
            return null;
        }
        
        return ItemBuilder.createFromConfig(itemConfig);
    }

    /**
     * Adds the close button to the GUI
     */
    private void addCloseButton() {
        final ConfigurationSection closeButtonConfig = this.plugin.getConfigManager()
            .getItemSection(GUI_NAME, "close-button");
        
        if (closeButtonConfig == null) {
            this.plugin.getLogger().warning("Missing close-button configuration for main-menu GUI");
            return;
        }
        
        final int slot = closeButtonConfig.getInt("slot", this.inventory.getSize() - 1);
        final ItemStack closeButton = ItemBuilder.createFromConfig(closeButtonConfig);
        
        if (closeButton != null) {
            GUIUtils.setItem(this.inventory, slot, closeButton);
        }
    }

    /**
     * Gets a home by slot number
     * @param slot the slot number
     * @return the home or null if not found
     */
    public Home getHomeBySlot(final int slot) {
        final List<Home> playerHomes = this.plugin.getHomeManager().getHomes(this.player);
        
        // Convert GUI slot (1-7) to home index (0-6)
        final int homeIndex = slot - 1;
        
        if (homeIndex < 0 || homeIndex >= playerHomes.size()) {
            return null;
        }
        
        return playerHomes.get(homeIndex);
    }

    /**
     * Checks if a slot represents an available home slot
     * @param slot the slot number
     * @return true if the slot is available
     */
    public boolean isAvailableSlot(final int slot) {
        // Only slots 1-7 can be home slots
        if (slot < 1 || slot > 7) {
            return false;
        }
        
        final ItemStack item = this.inventory.getItem(slot);
        if (item == null) {
            return false;
        }
        
        final ConfigurationSection availableConfig = this.plugin.getConfigManager()
            .getItemSection(GUI_NAME, "available-slot");
        
        if (availableConfig == null) {
            return false;
        }
        
        final String configMaterial = availableConfig.getString("material");
        if (configMaterial == null) {
            return false;
        }
        
        // Check if it matches available slot material
        final boolean materialMatches = this.matchesMaterial(item, configMaterial);
        
        if (!materialMatches) {
            return false;
        }
        
        // Additional check: make sure it's not an unavailable slot by checking display name
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            final String displayName = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                .serialize(item.getItemMeta().displayName());
            return displayName.contains("Available Slot");
        }
        
        return true;
    }

    /**
     * Checks if a slot represents an unavailable home slot
     * @param slot the slot number
     * @return true if the slot is unavailable
     */
    public boolean isUnavailableSlot(final int slot) {
        // Only slots 1-7 can be home slots
        if (slot < 1 || slot > 7) {
            return false;
        }
        
        final ItemStack item = this.inventory.getItem(slot);
        if (item == null) {
            return false;
        }
        
        // Check if it has the "Upgrade Required" display name
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            final String displayName = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                .serialize(item.getItemMeta().displayName());
            return displayName.contains("Upgrade Required");
        }
        
        return false;
    }

    /**
     * Checks if a slot represents an occupied home slot
     * @param slot the slot number
     * @return true if the slot is occupied
     */
    public boolean isOccupiedSlot(final int slot) {
        // Only slots 1-7 can be home slots
        if (slot < 1 || slot > 7) {
            return false;
        }
        
        final ItemStack item = this.inventory.getItem(slot);
        if (item == null) {
            return false;
        }
        
        // Check if it's not an available or unavailable slot
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            final String displayName = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                .serialize(item.getItemMeta().displayName());
            return !displayName.contains("Upgrade Required") && !displayName.contains("Available Slot") && !displayName.contains("Close");
        }
        
        return true;
    }

    /**
     * Checks if a slot represents the close button
     * @param slot the slot number
     * @return true if the slot is the close button
     */
    public boolean isCloseButton(final int slot) {
        final ConfigurationSection closeButtonConfig = this.plugin.getConfigManager()
            .getItemSection(GUI_NAME, "close-button");
        
        if (closeButtonConfig == null) {
            return false;
        }
        
        final int closeButtonSlot = closeButtonConfig.getInt("slot", 13);
        return slot == closeButtonSlot;
    }
    
    /**
     * Helper method to check if an item matches a material configuration
     * @param item the item to check
     * @param configMaterial the material from config
     * @return true if they match
     */
    private boolean matchesMaterial(final @NotNull ItemStack item, final @NotNull String configMaterial) {
        final String itemKey = item.getType().getKey().toString();
        final String itemName = item.getType().name();
        
        return configMaterial.equals(itemKey) ||
               configMaterial.equals("minecraft:" + itemName.toLowerCase()) ||
               configMaterial.equalsIgnoreCase(itemName);
    }
}