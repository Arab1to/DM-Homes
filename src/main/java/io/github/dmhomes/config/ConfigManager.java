package io.github.dmhomes.config;

import io.github.dmhomes.DMHomesPlugin;
import io.github.dmhomes.exceptions.DMHomesException;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * Manages plugin configuration and provides safe access to config values
 */
public final class ConfigManager {

    private final DMHomesPlugin plugin;
    
    @Getter
    private FileConfiguration config;

    /**
     * Creates a new ConfigManager instance
     * @param plugin the plugin instance
     * @throws DMHomesException if config loading fails
     */
    public ConfigManager(final @NotNull DMHomesPlugin plugin) throws DMHomesException {
        this.plugin = Objects.requireNonNull(plugin, "Plugin cannot be null");
        this.reloadConfig();
    }

    /**
     * Reloads the plugin configuration
     * @throws DMHomesException if config reloading fails
     */
    public void reloadConfig() throws DMHomesException {
        try {
            this.plugin.saveDefaultConfig();
            this.plugin.reloadConfig();
            this.config = this.plugin.getConfig();
            this.validateConfig();
        } catch (final Exception exception) {
            throw new DMHomesException("Failed to reload configuration", exception);
        }
    }

    /**
     * Validates the configuration for required values
     * @throws DMHomesException if validation fails
     */
    private void validateConfig() throws DMHomesException {
        if (this.config.getConfigurationSection("guis") == null) {
            throw new DMHomesException("Missing 'guis' section in config.yml");
        }
        
        if (this.config.getConfigurationSection("messages") == null) {
            throw new DMHomesException("Missing 'messages' section in config.yml");
        }
        
        // Validate GUI configurations
        this.validateGuiConfig("main-menu");
        this.validateGuiConfig("management-menu");
        this.validateGuiConfig("icon-menu");
    }

    /**
     * Validates a specific GUI configuration section
     * @param guiName the GUI name to validate
     * @throws DMHomesException if validation fails
     */
    private void validateGuiConfig(final @NotNull String guiName) throws DMHomesException {
        final ConfigurationSection guiSection = this.getGuiSection(guiName);
        if (guiSection == null) {
            throw new DMHomesException("Missing GUI configuration: " + guiName);
        }
        
        if (!guiSection.contains("title")) {
            throw new DMHomesException("Missing title for GUI: " + guiName);
        }
        
        if (!guiSection.contains("size")) {
            throw new DMHomesException("Missing size for GUI: " + guiName);
        }
        
        final int size = guiSection.getInt("size");
        if (size <= 0 || size % 9 != 0 || size > 54) {
            throw new DMHomesException("Invalid GUI size for " + guiName + ": " + size);
        }
    }

    /**
     * Gets a GUI configuration section
     * @param guiName the GUI name
     * @return the configuration section or null if not found
     */
    public @Nullable ConfigurationSection getGuiSection(final @NotNull String guiName) {
        return this.config.getConfigurationSection("guis." + guiName);
    }

    /**
     * Gets a message from the configuration
     * @param messageKey the message key
     * @return the message or null if not found
     */
    public @Nullable String getMessage(final @NotNull String messageKey) {
        return this.config.getString("messages." + messageKey);
    }

    /**
     * Gets the GUI title for a specific GUI
     * @param guiName the GUI name
     * @return the title or a default value
     */
    public @NotNull String getGuiTitle(final @NotNull String guiName) {
        final ConfigurationSection section = this.getGuiSection(guiName);
        return section != null ? section.getString("title", guiName) : guiName;
    }

    /**
     * Gets the GUI size for a specific GUI
     * @param guiName the GUI name
     * @return the size or 27 as default
     */
    public int getGuiSize(final @NotNull String guiName) {
        final ConfigurationSection section = this.getGuiSection(guiName);
        return section != null ? section.getInt("size", 27) : 27;
    }

    /**
     * Gets an item configuration section for a specific GUI and item
     * @param guiName the GUI name
     * @param itemName the item name
     * @return the item configuration section or null if not found
     */
    public @Nullable ConfigurationSection getItemSection(final @NotNull String guiName, final @NotNull String itemName) {
        final ConfigurationSection guiSection = this.getGuiSection(guiName);
        if (guiSection == null) return null;
        
        return guiSection.getConfigurationSection("items." + itemName);
    }

    /**
     * Gets the list of available icons from the configuration
     * @return the list of available icon materials
     */
    public @NotNull List<String> getAvailableIcons() {
        return this.config.getStringList("guis.icon-menu.items.available-icons");
    }

    /**
     * Gets the data format from the configuration
     * @return the data format (YAML or JSON)
     */
    public @NotNull String getDataFormat() {
        return this.config.getString("data.format", "YAML");
    }

    /**
     * Gets the auto-save interval from the configuration
     * @return the auto-save interval in minutes
     */
    public int getAutoSaveInterval() {
        return this.config.getInt("data.auto-save-interval", 5);
    }
}