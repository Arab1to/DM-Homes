package io.github.dmhomes.gui;

import io.github.dmhomes.DMHomesPlugin;
import io.github.dmhomes.data.Home;
import io.github.dmhomes.utils.GUIUtils;
import io.github.dmhomes.utils.ItemBuilder;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * GUI for managing a specific home (rename, delete, change icon)
 */
public final class HomeManagementGUI extends BaseGUI {

    private static final String GUI_NAME = "management-menu";
    
    @Getter
    private final Home home;
    
    @Getter
    private final MainHomesGUI parentGUI;

    /**
     * Creates a new HomeManagementGUI instance
     * @param plugin the plugin instance
     * @param player the player viewing the GUI
     * @param home the home being managed
     * @param parentGUI the parent GUI to return to
     */
    public HomeManagementGUI(final @NotNull DMHomesPlugin plugin, 
                           final @NotNull Player player, 
                           final @NotNull Home home,
                           final @NotNull MainHomesGUI parentGUI) {
        super(
            plugin, 
            player, 
            plugin.getConfigManager().getGuiTitle(GUI_NAME).replace("{home_name}", home.getName()),
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
            this.addRenameButton();
            this.addDeleteButton();
            this.addChangeIconButton();
            this.addBackButton();
        } catch (final Exception exception) {
            this.plugin.getLogger().severe("Error refreshing HomeManagementGUI for player " 
                + this.player.getName() + ": " + exception.getMessage());
        }
    }

    /**
     * Adds the rename button to the GUI
     */
    private void addRenameButton() {
        final ConfigurationSection renameConfig = this.plugin.getConfigManager()
            .getItemSection(GUI_NAME, "rename-button");
        
        if (renameConfig == null) {
            this.plugin.getLogger().warning("Missing rename-button configuration for management-menu GUI");
            return;
        }
        
        final int slot = renameConfig.getInt("slot", 2);
        final ItemStack renameButton = ItemBuilder.createFromConfig(renameConfig);
        
        if (renameButton != null) {
            GUIUtils.setItem(this.inventory, slot, renameButton);
        }
    }

    /**
     * Adds the delete button to the GUI
     */
    private void addDeleteButton() {
        final ConfigurationSection deleteConfig = this.plugin.getConfigManager()
            .getItemSection(GUI_NAME, "delete-button");
        
        if (deleteConfig == null) {
            this.plugin.getLogger().warning("Missing delete-button configuration for management-menu GUI");
            return;
        }
        
        final int slot = deleteConfig.getInt("slot", 4);
        final ItemStack deleteButton = ItemBuilder.createFromConfig(deleteConfig);
        
        if (deleteButton != null) {
            GUIUtils.setItem(this.inventory, slot, deleteButton);
        }
    }

    /**
     * Adds the change icon button to the GUI
     */
    private void addChangeIconButton() {
        final ConfigurationSection iconConfig = this.plugin.getConfigManager()
            .getItemSection(GUI_NAME, "change-icon-button");
        
        if (iconConfig == null) {
            this.plugin.getLogger().warning("Missing change-icon-button configuration for management-menu GUI");
            return;
        }
        
        final int slot = iconConfig.getInt("slot", 6);
        final ItemStack iconButton = ItemBuilder.createFromConfig(iconConfig);
        
        if (iconButton != null) {
            GUIUtils.setItem(this.inventory, slot, iconButton);
        }
    }

    /**
     * Adds the back button to the GUI
     */
    private void addBackButton() {
        final ConfigurationSection backConfig = this.plugin.getConfigManager()
            .getItemSection(GUI_NAME, "back-button");
        
        if (backConfig == null) {
            this.plugin.getLogger().warning("Missing back-button configuration for management-menu GUI");
            return;
        }
        
        final int slot = backConfig.getInt("slot", 8);
        final ItemStack backButton = ItemBuilder.createFromConfig(backConfig);
        
        if (backButton != null) {
            GUIUtils.setItem(this.inventory, slot, backButton);
        }
    }

    /**
     * Checks if a slot represents the rename button
     * @param slot the slot number
     * @return true if the slot is the rename button
     */
    public boolean isRenameButton(final int slot) {
        final ConfigurationSection renameConfig = this.plugin.getConfigManager()
            .getItemSection(GUI_NAME, "rename-button");
        
        if (renameConfig == null) {
            return false;
        }
        
        return slot == renameConfig.getInt("slot", 2);
    }

    /**
     * Checks if a slot represents the delete button
     * @param slot the slot number
     * @return true if the slot is the delete button
     */
    public boolean isDeleteButton(final int slot) {
        final ConfigurationSection deleteConfig = this.plugin.getConfigManager()
            .getItemSection(GUI_NAME, "delete-button");
        
        if (deleteConfig == null) {
            return false;
        }
        
        return slot == deleteConfig.getInt("slot", 4);
    }

    /**
     * Checks if a slot represents the change icon button
     * @param slot the slot number
     * @return true if the slot is the change icon button
     */
    public boolean isChangeIconButton(final int slot) {
        final ConfigurationSection iconConfig = this.plugin.getConfigManager()
            .getItemSection(GUI_NAME, "change-icon-button");
        
        if (iconConfig == null) {
            return false;
        }
        
        return slot == iconConfig.getInt("slot", 6);
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
        
        return slot == backConfig.getInt("slot", 8);
    }
}