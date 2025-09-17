package io.github.dmhomes.data;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents player-specific home data including custom icons
 */
@Data
public final class PlayerHomeData {

    private final UUID playerUuid;
    private final Map<String, String> homeIcons = new HashMap<>();
    private boolean modified = false;

    /**
     * Creates a new PlayerHomeData instance
     * @param playerUuid the player's UUID
     */
    public PlayerHomeData(final @NotNull UUID playerUuid) {
        this.playerUuid = Objects.requireNonNull(playerUuid, "Player UUID cannot be null");
    }

    /**
     * Sets the custom icon for a home
     * @param homeName the home name
     * @param iconMaterial the icon material (e.g., "minecraft:diamond_sword" or "itemsadder:custom_item")
     */
    public void setHomeIcon(final @NotNull String homeName, final @NotNull String iconMaterial) {
        Objects.requireNonNull(homeName, "Home name cannot be null");
        Objects.requireNonNull(iconMaterial, "Icon material cannot be null");
        
        this.homeIcons.put(homeName.toLowerCase(), iconMaterial);
        this.modified = true;
    }

    /**
     * Gets the custom icon for a home
     * @param homeName the home name
     * @return the icon material or null if no custom icon is set
     */
    public @Nullable String getHomeIcon(final @NotNull String homeName) {
        Objects.requireNonNull(homeName, "Home name cannot be null");
        return this.homeIcons.get(homeName.toLowerCase());
    }

    /**
     * Removes the custom icon for a home
     * @param homeName the home name
     * @return true if an icon was removed, false if no icon was set
     */
    public boolean removeHomeIcon(final @NotNull String homeName) {
        Objects.requireNonNull(homeName, "Home name cannot be null");
        
        final boolean removed = this.homeIcons.remove(homeName.toLowerCase()) != null;
        if (removed) {
            this.modified = true;
        }
        return removed;
    }

    /**
     * Renames a home's icon mapping
     * @param oldName the old home name
     * @param newName the new home name
     * @return true if the home had a custom icon that was renamed, false otherwise
     */
    public boolean renameHome(final @NotNull String oldName, final @NotNull String newName) {
        Objects.requireNonNull(oldName, "Old home name cannot be null");
        Objects.requireNonNull(newName, "New home name cannot be null");
        
        final String icon = this.homeIcons.remove(oldName.toLowerCase());
        if (icon != null) {
            this.homeIcons.put(newName.toLowerCase(), icon);
            this.modified = true;
            return true;
        }
        return false;
    }

    /**
     * Checks if the player has any custom home icons
     * @return true if the player has custom icons, false otherwise
     */
    public boolean hasCustomIcons() {
        return !this.homeIcons.isEmpty();
    }

    /**
     * Gets the number of homes with custom icons
     * @return the number of homes with custom icons
     */
    public int getCustomIconCount() {
        return this.homeIcons.size();
    }

    /**
     * Clears all data and marks as modified
     */
    public void clearData() {
        this.homeIcons.clear();
        this.modified = true;
    }

    /**
     * Marks the data as saved (not modified)
     */
    public void markAsSaved() {
        this.modified = false;
    }
}