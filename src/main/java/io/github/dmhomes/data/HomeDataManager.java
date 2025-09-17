package io.github.dmhomes.data;

import io.github.dmhomes.DMHomesPlugin;
import io.github.dmhomes.exceptions.DMHomesException;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Manages player home data storage and retrieval
 */
public final class HomeDataManager {

    private final DMHomesPlugin plugin;
    
    @Getter
    private final File dataFolder;
    
    private final Map<UUID, PlayerHomeData> playerDataCache = new ConcurrentHashMap<>();

    /**
     * Creates a new HomeDataManager instance
     * @param plugin the plugin instance
     * @throws DMHomesException if initialization fails
     */
    public HomeDataManager(final @NotNull DMHomesPlugin plugin) throws DMHomesException {
        this.plugin = Objects.requireNonNull(plugin, "Plugin cannot be null");
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        
        if (!this.dataFolder.exists() && !this.dataFolder.mkdirs()) {
            throw new DMHomesException("Failed to create player data directory: " + this.dataFolder.getPath());
        }
    }

    /**
     * Gets player home data, loading it if not already cached
     * @param playerUuid the player's UUID
     * @return the player's home data
     */
    public @NotNull PlayerHomeData getPlayerData(final @NotNull UUID playerUuid) {
        Objects.requireNonNull(playerUuid, "Player UUID cannot be null");
        
        return this.playerDataCache.computeIfAbsent(playerUuid, uuid -> {
            try {
                return this.loadPlayerData(uuid);
            } catch (final DMHomesException exception) {
                this.plugin.getLogger().log(Level.WARNING, 
                    "Failed to load data for player " + uuid + ", creating new data", exception);
                return new PlayerHomeData(uuid);
            }
        });
    }

    /**
     * Gets player home data by player object
     * @param player the player
     * @return the player's home data
     */
    public @NotNull PlayerHomeData getPlayerData(final @NotNull Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        return this.getPlayerData(player.getUniqueId());
    }

    /**
     * Saves player data to disk
     * @param playerUuid the player's UUID
     * @throws DMHomesException if saving fails
     */
    public void savePlayerData(final @NotNull UUID playerUuid) throws DMHomesException {
        Objects.requireNonNull(playerUuid, "Player UUID cannot be null");
        
        final PlayerHomeData playerData = this.playerDataCache.get(playerUuid);
        if (playerData == null || !playerData.isModified()) {
            return; // No data to save or data hasn't changed
        }
        
        try {
            final File playerFile = this.getPlayerDataFile(playerUuid);
            final YamlConfiguration config = new YamlConfiguration();
            
            // Save home icons
            if (playerData.hasCustomIcons()) {
                config.createSection("home-icons", playerData.getHomeIcons());
            }
            
            config.save(playerFile);
            playerData.markAsSaved();
            
        } catch (final IOException exception) {
            throw new DMHomesException("Failed to save player data for " + playerUuid, exception);
        }
    }

    /**
     * Loads player data from disk
     * @param playerUuid the player's UUID
     * @return the loaded player data
     * @throws DMHomesException if loading fails
     */
    private @NotNull PlayerHomeData loadPlayerData(final @NotNull UUID playerUuid) throws DMHomesException {
        final File playerFile = this.getPlayerDataFile(playerUuid);
        final PlayerHomeData playerData = new PlayerHomeData(playerUuid);
        
        if (!playerFile.exists()) {
            return playerData; // Return empty data for new players
        }
        
        try {
            final YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
            
            // Load home icons
            if (config.contains("home-icons")) {
                final Map<String, Object> icons = config.getConfigurationSection("home-icons").getValues(false);
                for (final Map.Entry<String, Object> entry : icons.entrySet()) {
                    if (entry.getValue() instanceof String) {
                        playerData.setHomeIcon(entry.getKey(), (String) entry.getValue());
                    }
                }
            }
            
            playerData.markAsSaved(); // Data loaded from disk is not modified
            return playerData;
            
        } catch (final Exception exception) {
            throw new DMHomesException("Failed to load player data for " + playerUuid, exception);
        }
    }

    /**
     * Gets the data file for a specific player
     * @param playerUuid the player's UUID
     * @return the player's data file
     */
    private @NotNull File getPlayerDataFile(final @NotNull UUID playerUuid) {
        return new File(this.dataFolder, playerUuid.toString() + ".yml");
    }

    /**
     * Saves all cached player data
     */
    public void saveAllData() {
        this.plugin.getLogger().info("Saving all player data...");
        int savedCount = 0;
        int errorCount = 0;
        
        for (final UUID playerUuid : this.playerDataCache.keySet()) {
            try {
                this.savePlayerData(playerUuid);
                savedCount++;
            } catch (final DMHomesException exception) {
                this.plugin.getLogger().log(Level.WARNING, 
                    "Failed to save data for player " + playerUuid, exception);
                errorCount++;
            }
        }
        
        this.plugin.getLogger().info(String.format(
            "Player data save complete: %d saved, %d errors", savedCount, errorCount));
    }

    /**
     * Removes a player from the cache (call when player leaves)
     * @param playerUuid the player's UUID
     */
    public void unloadPlayer(final @NotNull UUID playerUuid) {
        Objects.requireNonNull(playerUuid, "Player UUID cannot be null");
        
        try {
            this.savePlayerData(playerUuid);
        } catch (final DMHomesException exception) {
            this.plugin.getLogger().log(Level.WARNING, 
                "Failed to save data for player " + playerUuid + " on unload", exception);
        } finally {
            this.playerDataCache.remove(playerUuid);
        }
    }

    /**
     * Reloads all player data from disk
     * @throws DMHomesException if reloading fails
     */
    public void reloadData() throws DMHomesException {
        this.plugin.getLogger().info("Reloading player data...");
        
        // Save current data first
        this.saveAllData();
        
        // Clear cache to force reload
        this.playerDataCache.clear();
        
        this.plugin.getLogger().info("Player data reloaded successfully!");
    }

    /**
     * Gets a player's custom icon for a specific home
     * @param playerUuid the player's UUID
     * @param homeName the home name
     * @return the custom icon material or null if not set
     */
    public @Nullable String getHomeIcon(final @NotNull UUID playerUuid, final @NotNull String homeName) {
        return this.getPlayerData(playerUuid).getHomeIcon(homeName);
    }

    /**
     * Sets a player's custom icon for a specific home
     * @param playerUuid the player's UUID
     * @param homeName the home name
     * @param iconMaterial the icon material
     */
    public void setHomeIcon(final @NotNull UUID playerUuid, final @NotNull String homeName, 
                           final @NotNull String iconMaterial) {
        this.getPlayerData(playerUuid).setHomeIcon(homeName, iconMaterial);
    }

    /**
     * Removes a player's custom icon for a specific home
     * @param playerUuid the player's UUID
     * @param homeName the home name
     * @return true if an icon was removed
     */
    public boolean removeHomeIcon(final @NotNull UUID playerUuid, final @NotNull String homeName) {
        return this.getPlayerData(playerUuid).removeHomeIcon(homeName);
    }

    /**
     * Renames a home's icon mapping
     * @param playerUuid the player's UUID
     * @param oldName the old home name
     * @param newName the new home name
     */
    public void renameHomeIcon(final @NotNull UUID playerUuid, final @NotNull String oldName, 
                              final @NotNull String newName) {
        this.getPlayerData(playerUuid).renameHome(oldName, newName);
    }
}