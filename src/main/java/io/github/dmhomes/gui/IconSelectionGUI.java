package io.github.dmhomes.gui;

import io.github.dmhomes.DMHomesPlugin;
import io.github.dmhomes.data.Home;
import io.github.dmhomes.utils.GUIUtils;
import io.github.dmhomes.utils.ItemBuilder;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * GUI for selecting custom icons for homes
 */
public final class IconSelectionGUI extends BaseGUI {

    private static final String GUI_NAME = "icon-menu";
    
    @Getter
    private final Home home;
    
    @Getter
    private final HomeManagementGUI parentGUI;

    /**
     * Creates a new IconSelectionGUI instance
     * @param plugin the plugin instance
     * @param player the player viewing the GUI
     * @param home the home to change icon for
     * @param parentGUI the parent GUI to return to
     */
    public IconSelectionGUI(final @NotNull DMHomesPlugin plugin, 
                          final @NotNull Player player, 
                          final @NotNull Home home,
                          final @NotNull HomeManagementGUI parentGUI) {
        super(
            plugin, 
            player, 
            plugin.getConfigManager().getGuiTitle(GUI_NAME),
            plugin.getConfigManager().getGuiSize(GUI_NAME)
        );
        
        this.home = Objects.requireNonNull(home, "Home cannot be null");
        this.parentGUI = Objects.requireNonNull(parentGUI, "Parent GUI cannot be null");
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
            this.populateIcons();
            this.addBackButton();
        } catch (final Exception exception) {
            this.plugin.getLogger().severe("Error refreshing IconSelectionGUI for player " 
                + this.player.getName() + ": " + exception.getMessage());
        }
    }

    /**
     * Populates the GUI with available icons
     */
    private void populateIcons() {
        final List<String> availableIcons = this.plugin.getConfigManager().getAvailableIcons();
        
        if (availableIcons.isEmpty()) {
            this.plugin.getLogger().warning("No available icons configured for icon-menu GUI");
            return;
        }
        
        // Define the specific slots where icons should be placed
        final int[] iconSlots = {1, 2, 3, 4, 5, 6, 7, 10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
        int iconIndex = 0;
        
        for (final String iconMaterial : availableIcons) {
            if (iconIndex >= iconSlots.length) {
                // TODO: Implement pagination if needed
                this.plugin.getLogger().info("More icons available than can fit in GUI, consider implementing pagination");
                break;
            }
            
            final ItemStack iconItem = this.createIconItem(iconMaterial);
            if (iconItem != null) {
                this.inventory.setItem(iconSlots[iconIndex++], iconItem);
            }
        }
    }

    /**
     * Creates an icon item from a material string
     * @param materialString the material string
     * @return the created item or null if invalid
     */
    private @Nullable ItemStack createIconItem(final @NotNull String materialString) {
        Objects.requireNonNull(materialString, "Material string cannot be null");
        
        final Material material = this.parseMaterial(materialString);
        if (material == null) {
            this.plugin.getLogger().warning("Invalid icon material: " + materialString);
            return null;
        }
        
        return ItemBuilder.create(material, 
            "<!italic><yellow>" + this.formatMaterialName(material.name()) + "</yellow>",
            "<!italic><gray>Click to select this icon</gray>");
    }

    /**
     * Parses a material string, supporting both vanilla and ItemsAdder formats
     * @param materialString the material string
     * @return the parsed Material or null if invalid
     */
    private @Nullable Material parseMaterial(final @NotNull String materialString) {
        try {
            // Handle ItemsAdder items
            if (materialString.contains(":")) {
                if (materialString.startsWith("itemsadder:")) {
                    // TODO: Implement ItemsAdder support
                    return Material.BARRIER; // Fallback for now
                } else if (materialString.startsWith("minecraft:")) {
                    return Material.matchMaterial(materialString.substring(10));
                }
            }
            
            // Try to parse as vanilla material
            return Material.matchMaterial(materialString);
        } catch (final Exception exception) {
            return null;
        }
    }

    /**
     * Formats a material name for display
     * @param materialName the material name
     * @return the formatted name
     */
    private @NotNull String formatMaterialName(final @NotNull String materialName) {
        return materialName.toLowerCase().replace("_", " ");
    }

    /**
     * Adds the back button to the GUI
     */
    private void addBackButton() {
        final ConfigurationSection backConfig = this.plugin.getConfigManager()
            .getItemSection(GUI_NAME, "back-button");
        
        if (backConfig == null) {
            this.plugin.getLogger().warning("Missing back-button configuration for icon-menu GUI");
            return;
        }
        
        final int slot = backConfig.getInt("slot", this.inventory.getSize() - 1);
        final ItemStack backButton = ItemBuilder.createFromConfig(backConfig);
        
        if (backButton != null) {
            GUIUtils.setItem(this.inventory, slot, backButton);
        }
    }

    /**
     * Checks if a slot represents the back button
     * @param slot the slot number
     * @return true if the slot is the back button
     */
    public boolean isBackButton(final int slot) {
        final ConfigurationSection backConfig = this.plugin.getConfigManager()
            .getItemSection(GUI_NAME, "back-button");
        
        if (backConfig == null) {
            return false;
        }
        
        final int backButtonSlot = backConfig.getInt("slot", 31);
        return slot == backButtonSlot;
    }

    /**
     * Gets the material string for a clicked icon slot
     * @param slot the clicked slot
     * @return the material string or null if not an icon slot
     */
    public @Nullable String getIconMaterial(final int slot) {
        if (!this.isValidSlot(slot) || this.isBackButton(slot)) {
            return null;
        }
        
        final ItemStack item = this.inventory.getItem(slot);
        if (item == null) {
            return null;
        }
        
        // Define the specific slots where icons are placed
        final int[] iconSlots = {1, 2, 3, 4, 5, 6, 7, 10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
        final List<String> availableIcons = this.plugin.getConfigManager().getAvailableIcons();
        
        // Find which icon slot this is
        for (int i = 0; i < iconSlots.length; i++) {
            if (iconSlots[i] == slot && i < availableIcons.size()) {
                return availableIcons.get(i);
            }
        }
        
        return null;
    }
}