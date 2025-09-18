package io.github.dmhomes.data;

import io.github.dmhomes.DMHomesPlugin;
import io.github.dmhomes.exceptions.DMHomesException;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * Manages player homes storage and operations
 */
@RequiredArgsConstructor
public final class HomeManager {

    private final DMHomesPlugin plugin;
    private final Map<UUID, Map<String, Home>> playerHomes = new ConcurrentHashMap<>();
    private Pattern namePattern;

    /**
     * Initializes the home manager
     * @throws DMHomesException if initialization fails
     */
    public void initialize() throws DMHomesException {
        this.loadNamePattern();
        this.createHomesDirectory();
        this.loadAllHomes();
    }

    /**
     * Loads the home name validation pattern from config
     */
    private void loadNamePattern() {
        final String regex = this.plugin.getConfigManager().getConfig()
                .getString("homes.name-regex", "^[a-zA-Z0-9_]{1,16}$");
        this.namePattern = Pattern.compile(regex);
    }

    /**
     * Creates the homes directory if it doesn't exist
     * @throws DMHomesException if directory creation fails
     */
    private void createHomesDirectory() throws DMHomesException {
        final File homesDir = new File(this.plugin.getDataFolder(), "homes");
        if (!homesDir.exists() && !homesDir.mkdirs()) {
            throw new DMHomesException("Failed to create homes directory: " + homesDir.getPath());
        }
    }

    /**
     * Loads all player homes from disk
     * @throws DMHomesException if loading fails
     */
    private void loadAllHomes() throws DMHomesException {
        final File homesDir = new File(this.plugin.getDataFolder(), "homes");
        final File[] playerFiles = homesDir.listFiles((dir, name) -> name.endsWith(".yml"));

        if (playerFiles == null) {
            return;
        }

        int loadedCount = 0;
        int errorCount = 0;

        for (final File playerFile : playerFiles) {
            try {
                final String fileName = playerFile.getName();
                final String uuidString = fileName.substring(0, fileName.length() - 4); // Remove .yml
                final UUID playerUuid = UUID.fromString(uuidString);

                this.loadPlayerHomes(playerUuid);
                loadedCount++;
            } catch (final Exception exception) {
                this.plugin.getLogger().log(Level.WARNING,
                        "Failed to load homes from file: " + playerFile.getName(), exception);
                errorCount++;
            }
        }

        this.plugin.getLogger().info(String.format(
                "Loaded homes: %d players loaded, %d errors", loadedCount, errorCount));
    }

    /**
     * Loads homes for a specific player
     * @param playerUuid the player's UUID
     * @throws DMHomesException if loading fails
     */
    private void loadPlayerHomes(final @NotNull UUID playerUuid) throws DMHomesException {
        final File playerFile = this.getPlayerHomesFile(playerUuid);

        if (!playerFile.exists()) {
            return; // No homes file for this player
        }

        try {
            final YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
            final Map<String, Home> homes = new HashMap<>();

            for (final String homeName : config.getKeys(false)) {
                if (config.isConfigurationSection(homeName)) {
                    final Location location = config.getLocation(homeName + ".location");
                    final long createdAt = config.getLong(homeName + ".created-at", System.currentTimeMillis());

                    if (location != null) {
                        final Home home = new Home(playerUuid, homeName, location, createdAt);
                        homes.put(homeName.toLowerCase(), home);
                    }
                } else {
                    // Handle old format without sections
                    final Location location = config.getLocation(homeName + ".location");
                    final long createdAt = config.getLong(homeName + ".created-at", System.currentTimeMillis());

                    if (location != null) {
                        final Home home = new Home(playerUuid, homeName, location, createdAt);
                        homes.put(homeName.toLowerCase(), home);
                    }
                }
            }

            if (!homes.isEmpty()) {
                this.playerHomes.put(playerUuid, homes);
            }
        } catch (final Exception exception) {
            throw new DMHomesException("Failed to load homes for player " + playerUuid, exception);
        }
    }

    /**
     * Saves homes for a specific player
     * @param playerUuid the player's UUID
     * @throws DMHomesException if saving fails
     */
    private void savePlayerHomes(final @NotNull UUID playerUuid) throws DMHomesException {
        final Map<String, Home> homes = this.playerHomes.get(playerUuid);
        final File playerFile = this.getPlayerHomesFile(playerUuid);

        try {
            final YamlConfiguration config = new YamlConfiguration();

            if (homes != null && !homes.isEmpty()) {
                for (final Home home : homes.values()) {
                    final String homeName = home.getName();
                    config.set(homeName + ".location", home.getLocation());
                    config.set(homeName + ".created-at", home.getCreatedAt());
                }
                
                // Ensure parent directories exist
                if (!playerFile.getParentFile().exists()) {
                    playerFile.getParentFile().mkdirs();
                }
            }

            config.save(playerFile);
            this.plugin.getLogger().info("Saved " + (homes != null ? homes.size() : 0) + " homes for player " + playerUuid);
        } catch (final IOException exception) {
            throw new DMHomesException("Failed to save homes for player " + playerUuid, exception);
        }
    }

    /**
     * Gets the homes file for a player
     * @param playerUuid the player's UUID
     * @return the homes file
     */
    private @NotNull File getPlayerHomesFile(final @NotNull UUID playerUuid) {
        return new File(this.plugin.getDataFolder(), "homes/" + playerUuid + ".yml");
    }

    /**
     * Creates a new home for a player
     * @param player the player
     * @param homeName the home name
     * @param location the home location
     * @return true if the home was created successfully
     * @throws DMHomesException if creation fails
     */
    public boolean createHome(final @NotNull Player player, final @NotNull String homeName,
                              final @NotNull Location location) throws DMHomesException {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(homeName, "Home name cannot be null");
        Objects.requireNonNull(location, "Location cannot be null");

        // Validate home name
        if (!this.namePattern.matcher(homeName).matches()) {
            return false;
        }

        // Check if home already exists
        if (this.hasHome(player, homeName)) {
            return false;
        }

        // Check max homes limit
        if (!this.canCreateHome(player)) {
            return false;
        }

        // Check world blacklist
        if (location.getWorld() != null && this.isWorldBlacklisted(location.getWorld().getName())) {
            this.plugin.getLogger().info("Home creation blocked in blacklisted world: " + location.getWorld().getName());
            return false;
        }

        final UUID playerUuid = player.getUniqueId();
        final Map<String, Home> homes = this.playerHomes.computeIfAbsent(playerUuid, k -> new HashMap<>());

        final Home home = new Home(playerUuid, homeName, location);
        homes.put(homeName.toLowerCase(), home);

        this.savePlayerHomes(playerUuid);
        return true;
    }

    /**
     * Deletes a home for a player
     * @param player the player
     * @param homeName the home name
     * @return true if the home was deleted
     * @throws DMHomesException if deletion fails
     */
    public boolean deleteHome(final @NotNull Player player, final @NotNull String homeName) throws DMHomesException {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(homeName, "Home name cannot be null");

        final UUID playerUuid = player.getUniqueId();
        final Map<String, Home> homes = this.playerHomes.get(playerUuid);

        if (homes == null) {
            return false;
        }

        final Home removed = homes.remove(homeName.toLowerCase());
        if (removed != null) {
            this.savePlayerHomes(playerUuid);

            // Also remove custom icon
            this.plugin.getHomeDataManager().removeHomeIcon(playerUuid, homeName);
            return true;
        }

        return false;
    }

    /**
     * Gets a specific home for a player
     * @param player the player
     * @param homeName the home name
     * @return the home or null if not found
     */
    public @Nullable Home getHome(final @NotNull Player player, final @NotNull String homeName) {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(homeName, "Home name cannot be null");

        final Map<String, Home> homes = this.playerHomes.get(player.getUniqueId());
        return homes != null ? homes.get(homeName.toLowerCase()) : null;
    }

    /**
     * Gets all homes for a player
     * @param player the player
     * @return the list of homes
     */
    public @NotNull List<Home> getHomes(final @NotNull Player player) {
        Objects.requireNonNull(player, "Player cannot be null");

        final Map<String, Home> homes = this.playerHomes.get(player.getUniqueId());
        return homes != null ? new ArrayList<>(homes.values()) : new ArrayList<>();
    }

    /**
     * Checks if a player has a specific home
     * @param player the player
     * @param homeName the home name
     * @return true if the home exists
     */
    public boolean hasHome(final @NotNull Player player, final @NotNull String homeName) {
        return this.getHome(player, homeName) != null;
    }

    /**
     * Gets the number of homes a player has
     * @param player the player
     * @return the number of homes
     */
    public int getHomeCount(final @NotNull Player player) {
        final Map<String, Home> homes = this.playerHomes.get(player.getUniqueId());
        return homes != null ? homes.size() : 0;
    }

    /**
     * Gets the maximum number of homes a player can have
     * @param player the player
     * @return the maximum number of homes
     */
    public int getMaxHomes(final @NotNull Player player) {
        // Check for unlimited homes permission
        if (player.hasPermission("dmhomes.homes.unlimited")) {
            return -1; // Unlimited
        }

        // Check for permission-based limits
        for (int i = 50; i >= 1; i--) {
            if (player.hasPermission("dmhomes.homes." + i)) {
                return i;
            }
        }

        // Return default from config
        return this.plugin.getConfigManager().getConfig().getInt("homes.max-homes-default", 3);
    }

    /**
     * Gets the maximum number of GUI slots a player can see
     * @param player the player
     * @return the maximum GUI slots
     */
    public int getMaxGuiSlots(final @NotNull Player player) {
        // Admins can see all slots
        if (player.hasPermission("dmhomes.admin")) {
            return 25; // Maximum possible slots in GUI
        }

        // Regular players see limited slots
        return this.plugin.getConfigManager().getConfig().getInt("homes.max-homes-gui-slots", 7);
    }

    /**
     * Checks if a player can create more homes
     * @param player the player
     * @return true if the player can create more homes
     */
    public boolean canCreateHome(final @NotNull Player player) {
        final int maxHomes = this.getMaxHomes(player);
        final int currentHomes = this.getHomeCount(player);
        return maxHomes == -1 || currentHomes < maxHomes;
    }

    /**
     * Checks if a world is blacklisted for home creation
     * @param worldName the world name
     * @return true if the world is blacklisted
     */
    public boolean isWorldBlacklisted(final @NotNull String worldName) {
        final List<String> blacklist = this.plugin.getConfigManager().getConfig()
                .getStringList("homes.world-blacklist");
        return blacklist.contains(worldName);
    }

    /**
     * Renames a home for a player
     * @param player the player
     * @param oldName the old home name
     * @param newName the new home name
     * @return true if the home was renamed
     * @throws DMHomesException if renaming fails
     */
    public boolean renameHome(final @NotNull Player player, final @NotNull String oldName,
                              final @NotNull String newName) throws DMHomesException {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(oldName, "Old name cannot be null");
        Objects.requireNonNull(newName, "New name cannot be null");

        // Validate new name
        if (!this.namePattern.matcher(newName).matches()) {
            return false;
        }

        final UUID playerUuid = player.getUniqueId();
        final Map<String, Home> homes = this.playerHomes.get(playerUuid);

        if (homes == null) {
            return false;
        }

        final Home oldHome = homes.remove(oldName.toLowerCase());
        if (oldHome == null) {
            return false;
        }

        // Check if new name already exists
        if (homes.containsKey(newName.toLowerCase())) {
            // Put the old home back
            homes.put(oldName.toLowerCase(), oldHome);
            return false;
        }

        // Create new home with new name
        final Home newHome = new Home(playerUuid, newName, oldHome.getLocation(), oldHome.getCreatedAt());
        homes.put(newName.toLowerCase(), newHome);

        this.savePlayerHomes(playerUuid);

        // Also rename custom icon
        this.plugin.getHomeDataManager().renameHomeIcon(playerUuid, oldName, newName);

        return true;
    }

    /**
     * Saves all player homes
     */
    public void saveAllHomes() {
        this.plugin.getLogger().info("Saving all player homes...");
        int savedCount = 0;
        int errorCount = 0;

        for (final UUID playerUuid : this.playerHomes.keySet()) {
            try {
                this.savePlayerHomes(playerUuid);
                savedCount++;
            } catch (final DMHomesException exception) {
                this.plugin.getLogger().log(Level.WARNING,
                        "Failed to save homes for player " + playerUuid, exception);
                errorCount++;
            }
        }

        this.plugin.getLogger().info(String.format(
                "Home save complete: %d saved, %d errors", savedCount, errorCount));
    }

    /**
     * Reloads all homes from disk
     * @throws DMHomesException if reloading fails
     */
    public void reloadHomes() throws DMHomesException {
        this.plugin.getLogger().info("Reloading player homes...");

        // Save current data first
        this.saveAllHomes();

        // Clear cache and reload
        this.playerHomes.clear();
        this.loadNamePattern();
        this.loadAllHomes();

        this.plugin.getLogger().info("Player homes reloaded successfully!");
    }
}