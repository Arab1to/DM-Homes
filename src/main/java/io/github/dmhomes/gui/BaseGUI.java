package io.github.dmhomes.gui;

import io.github.dmhomes.DMHomesPlugin;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Base class for all GUI implementations
 */
@Getter
public abstract class BaseGUI implements InventoryHolder {

    protected final DMHomesPlugin plugin;
    protected final Player player;
    protected final Inventory inventory;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    /**
     * Creates a new BaseGUI instance
     * @param plugin the plugin instance
     * @param player the player viewing the GUI
     * @param title the GUI title
     * @param size the GUI size (must be multiple of 9, max 54)
     */
    protected BaseGUI(final @NotNull DMHomesPlugin plugin, 
                     final @NotNull Player player, 
                     final @NotNull String title, 
                     final int size) {
        this.plugin = Objects.requireNonNull(plugin, "Plugin cannot be null");
        this.player = Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(title, "Title cannot be null");
        
        if (size <= 0 || size % 9 != 0 || size > 54) {
            throw new IllegalArgumentException("Invalid GUI size: " + size);
        }
        
        final Component titleComponent = this.miniMessage.deserialize(title);
        this.inventory = Bukkit.createInventory(this, size, titleComponent);
        
        this.setupGUI();
    }

    /**
     * Sets up the GUI with initial items
     * This method should be overridden by subclasses to populate the GUI
     */
    protected abstract void setupGUI();

    /**
     * Refreshes the GUI content
     * This method should be overridden by subclasses to update the GUI
     */
    public abstract void refresh();

    /**
     * Opens the GUI for the player
     */
    public final void open() {
        this.player.openInventory(this.inventory);
    }

    /**
     * Closes the GUI for the player
     */
    public final void close() {
        this.player.closeInventory();
    }

    /**
     * Checks if the given inventory belongs to this GUI
     * @param inventory the inventory to check
     * @return true if the inventory belongs to this GUI
     */
    public final boolean isThisInventory(final @NotNull Inventory inventory) {
        return this.inventory.equals(inventory);
    }

    /**
     * Gets the GUI title with placeholders replaced
     * @param title the title template
     * @param placeholders the placeholders (key-value pairs)
     * @return the processed title
     */
    protected final @NotNull String processTitle(final @NotNull String title, final @NotNull String... placeholders) {
        String result = title;
        
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                final String placeholder = placeholders[i];
                final String value = placeholders[i + 1];
                result = result.replace("{" + placeholder + "}", value != null ? value : "null");
            }
        }
        
        return result;
    }

    /**
     * Checks if a slot number is valid for this GUI
     * @param slot the slot number
     * @return true if valid
     */
    protected final boolean isValidSlot(final int slot) {
        return slot >= 0 && slot < this.inventory.getSize();
    }

    /**
     * Called when the GUI is closed
     * Override this method to perform cleanup or additional actions
     */
    public void onClose() {
        // Default implementation does nothing
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}