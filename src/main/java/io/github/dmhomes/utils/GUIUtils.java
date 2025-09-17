package io.github.dmhomes.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Utility class for GUI-related operations
 */
@UtilityClass
public class GUIUtils {

    /**
     * Safely sets an item in an inventory at the specified slot
     * @param inventory the inventory
     * @param slot the slot number
     * @param item the item to set
     */
    public void setItem(final @NotNull Inventory inventory, final int slot, final @Nullable ItemStack item) {
        Objects.requireNonNull(inventory, "Inventory cannot be null");
        
        if (slot >= 0 && slot < inventory.getSize()) {
            inventory.setItem(slot, item);
        }
    }

    /**
     * Checks if a slot number is valid for the given inventory
     * @param inventory the inventory
     * @param slot the slot number
     * @return true if the slot is valid
     */
    public boolean isValidSlot(final @NotNull Inventory inventory, final int slot) {
        Objects.requireNonNull(inventory, "Inventory cannot be null");
        return slot >= 0 && slot < inventory.getSize();
    }

    /**
     * Gets the first empty slot in an inventory
     * @param inventory the inventory
     * @return the first empty slot number or -1 if no empty slots
     */
    public int getFirstEmptySlot(final @NotNull Inventory inventory) {
        Objects.requireNonNull(inventory, "Inventory cannot be null");
        
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                return i;
            }
        }
        
        return -1;
    }

    /**
     * Counts the number of empty slots in an inventory
     * @param inventory the inventory
     * @return the number of empty slots
     */
    public int countEmptySlots(final @NotNull Inventory inventory) {
        Objects.requireNonNull(inventory, "Inventory cannot be null");
        
        int emptySlots = 0;
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                emptySlots++;
            }
        }
        
        return emptySlots;
    }

    /**
     * Validates that a GUI size is appropriate for Minecraft inventories
     * @param size the GUI size
     * @return true if the size is valid
     */
    public boolean isValidGuiSize(final int size) {
        return size > 0 && size % 9 == 0 && size <= 54;
    }

    /**
     * Converts a GUI size to the number of rows
     * @param size the GUI size
     * @return the number of rows
     */
    public int getRowCount(final int size) {
        return Math.max(1, size / 9);
    }

    /**
     * Converts row and column to slot number
     * @param row the row (0-based)
     * @param col the column (0-based)
     * @return the slot number
     */
    public int getSlot(final int row, final int col) {
        return row * 9 + col;
    }

    /**
     * Gets the row number for a slot
     * @param slot the slot number
     * @return the row number (0-based)
     */
    public int getRow(final int slot) {
        return slot / 9;
    }

    /**
     * Gets the column number for a slot
     * @param slot the slot number
     * @return the column number (0-based)
     */
    public int getColumn(final int slot) {
        return slot % 9;
    }
}