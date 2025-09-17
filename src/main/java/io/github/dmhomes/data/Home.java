package io.github.dmhomes.data;

import lombok.Data;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a player's home location
 */
@Data
public final class Home {

    private final UUID playerUuid;
    private final String name;
    private final Location location;
    private final long createdAt;

    /**
     * Creates a new Home instance
     * @param playerUuid the player's UUID
     * @param name the home name
     * @param location the home location
     */
    public Home(final @NotNull UUID playerUuid, final @NotNull String name, final @NotNull Location location) {
        this.playerUuid = Objects.requireNonNull(playerUuid, "Player UUID cannot be null");
        this.name = Objects.requireNonNull(name, "Home name cannot be null");
        this.location = Objects.requireNonNull(location, "Location cannot be null").clone();
        this.createdAt = System.currentTimeMillis();
    }

    /**
     * Creates a Home instance with a specific creation time
     * @param playerUuid the player's UUID
     * @param name the home name
     * @param location the home location
     * @param createdAt the creation timestamp
     */
    public Home(final @NotNull UUID playerUuid, final @NotNull String name, 
               final @NotNull Location location, final long createdAt) {
        this.playerUuid = Objects.requireNonNull(playerUuid, "Player UUID cannot be null");
        this.name = Objects.requireNonNull(name, "Home name cannot be null");
        this.location = Objects.requireNonNull(location, "Location cannot be null").clone();
        this.createdAt = createdAt;
    }


    /**
     * Gets a cloned copy of the location to prevent external modification
     * @return a cloned location
     */
    public @NotNull Location getLocation() {
        return this.location.clone();
    }

    /**
     * Gets the world name of this home
     * @return the world name or "unknown" if world is null
     */
    public @NotNull String getWorldName() {
        return this.location.getWorld() != null ? this.location.getWorld().getName() : "unknown";
    }

    /**
     * Gets the block X coordinate
     * @return the block X coordinate
     */
    public int getBlockX() {
        return this.location.getBlockX();
    }

    /**
     * Gets the block Y coordinate
     * @return the block Y coordinate
     */
    public int getBlockY() {
        return this.location.getBlockY();
    }

    /**
     * Gets the block Z coordinate
     * @return the block Z coordinate
     */
    public int getBlockZ() {
        return this.location.getBlockZ();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        final Home home = (Home) obj;
        return Objects.equals(playerUuid, home.playerUuid) && 
               Objects.equals(name.toLowerCase(), home.name.toLowerCase());
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerUuid, name.toLowerCase());
    }
}