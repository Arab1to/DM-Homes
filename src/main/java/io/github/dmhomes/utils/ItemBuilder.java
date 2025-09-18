package io.github.dmhomes.utils;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Utility class for building ItemStacks from configuration
 */
@UtilityClass
public class ItemBuilder {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    /**
     * Creates an ItemStack from a configuration section
     * @param config the configuration section
     * @return the created ItemStack or null if invalid
     */
    public @Nullable ItemStack createFromConfig(final @Nullable ConfigurationSection config) {
        if (config == null) return null;
        
        final String materialName = config.getString("material");
        if (materialName == null) return null;
        
        // Handle ItemsAdder items directly (only if ItemsAdder is available)
        if (materialName.contains(":") && !materialName.startsWith("minecraft:")) {
            return createItemsAdderItem(config, materialName);
        }
        
        final Material material = parseMaterial(materialName);
        if (material == null) return null;
        
        final ItemStack item = new ItemStack(material);
        final ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            // Set display name
            final String name = config.getString("name");
            if (name != null) {
                // Add <!italic> if not already present
                final String processedName = name.startsWith("<!italic>") ? name : "<!italic>" + name;
                meta.displayName(miniMessage.deserialize(processedName));
            }
            
            // Set lore
            final List<String> loreStrings = config.getStringList("lore");
            if (!loreStrings.isEmpty()) {
                final List<Component> lore = new ArrayList<>();
                for (final String loreLine : loreStrings) {
                    // Add <!italic> if not already present
                    final String processedLore = loreLine.startsWith("<!italic>") ? loreLine : "<!italic>" + loreLine;
                    lore.add(miniMessage.deserialize(processedLore));
                }
                meta.lore(lore);
            }
            
            item.setItemMeta(meta);
        }
        
        return item;
    }

    /**
     * Creates an ItemStack with placeholders replaced
     * @param config the configuration section
     * @param placeholders the placeholders to replace (key-value pairs)
     * @return the created ItemStack or null if invalid
     */
    public @Nullable ItemStack createFromConfigWithPlaceholders(final @Nullable ConfigurationSection config, 
                                                               final @NotNull String... placeholders) {
        if (config == null) return null;
        
        final String materialName = config.getString("material");
        if (materialName == null) return null;
        
        final Material material = parseMaterial(materialName);
        if (material == null) return null;
        
        final ItemStack item = new ItemStack(material);
        final ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            // Set display name with placeholders
            String name = config.getString("name");
            if (name != null) {
                name = replacePlaceholders(name, placeholders);
                // Add <!italic> if not already present
                final String processedName = name.startsWith("<!italic>") ? name : "<!italic>" + name;
                meta.displayName(miniMessage.deserialize(processedName));
            }
            
            // Set lore with placeholders
            final List<String> loreStrings = config.getStringList("lore");
            if (!loreStrings.isEmpty()) {
                final List<Component> lore = new ArrayList<>();
                for (String loreLine : loreStrings) {
                    loreLine = replacePlaceholders(loreLine, placeholders);
                    // Add <!italic> if not already present
                    final String processedLore = loreLine.startsWith("<!italic>") ? loreLine : "<!italic>" + loreLine;
                    lore.add(miniMessage.deserialize(processedLore));
                }
                meta.lore(lore);
            }
            
            item.setItemMeta(meta);
        }
        
        return item;
    }

    /**
     * Creates a simple ItemStack with name and lore
     * @param material the material
     * @param name the display name (MiniMessage format)
     * @param lore the lore lines (MiniMessage format)
     * @return the created ItemStack
     */
    public @NotNull ItemStack create(final @NotNull Material material, 
                                    final @Nullable String name, 
                                    final @NotNull String... lore) {
        Objects.requireNonNull(material, "Material cannot be null");
        
        final ItemStack item = new ItemStack(material);
        final ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            if (name != null) {
                meta.displayName(miniMessage.deserialize("<!italic>" + name));
            }
            
            if (lore.length > 0) {
                final List<Component> loreComponents = new ArrayList<>();
                for (final String loreLine : lore) {
                    loreComponents.add(miniMessage.deserialize("<!italic>" + loreLine));
                }
                meta.lore(loreComponents);
            }
            
            item.setItemMeta(meta);
        }
        
        return item;
    }

    /**
     * Parses a material string, supporting both vanilla and ItemsAdder formats
     * @param materialString the material string
     * @return the parsed Material or null if invalid
     */
    private @Nullable Material parseMaterial(final @NotNull String materialString) {
        Objects.requireNonNull(materialString, "Material string cannot be null");
        
        // Handle minecraft: prefix
        if (materialString.startsWith("minecraft:")) {
            final String materialName = materialString.substring(10);
            final Material material = Material.matchMaterial(materialName);
            if (material == null) {
                // Try with uppercase
                return Material.matchMaterial(materialName.toUpperCase());
            }
            return material;
        }
        
        // Handle ItemsAdder items (only if ItemsAdder is available)
        if (materialString.contains(":") && !materialString.startsWith("minecraft:")) {
            return handleItemsAdder(materialString);
        }
        
        // Try to parse as vanilla material
        return Material.matchMaterial(materialString);
    }

    /**
     * Handles ItemsAdder item parsing
     * @param itemsAdderString the ItemsAdder item string
     * @return the Material or null if ItemsAdder is not available
     */
    private @Nullable Material handleItemsAdder(final @NotNull String itemsAdderString) {
        // Check if ItemsAdder is available
        if (Bukkit.getPluginManager().getPlugin("ItemsAdder") == null) {
            // Return a reasonable fallback material
            return Material.STONE; // Fallback to stone for all ItemsAdder items
        }
        
        try {
            // Try to get ItemsAdder item using reflection
            final Class<?> itemsAdderClass = Class.forName("dev.lone.itemsadder.api.ItemsAdder");
            final java.lang.reflect.Method getCustomItemMethod = itemsAdderClass.getMethod("getCustomItem", String.class);
            final Object itemStack = getCustomItemMethod.invoke(null, itemsAdderString);
            
            if (itemStack instanceof ItemStack) {
                return ((ItemStack) itemStack).getType();
            }
            
            return Material.STONE; // Fallback material
        } catch (final Exception exception) {
            return Material.STONE; // Fallback material
        }
    }

    /**
     * Replaces placeholders in a string
     * @param text the text with placeholders
     * @param placeholders the placeholders (key-value pairs)
     * @return the text with placeholders replaced
     */
    private @NotNull String replacePlaceholders(final @NotNull String text, final @NotNull String... placeholders) {
        String result = text;
        
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
     * Creates an ItemsAdder item from configuration
     * @param config the configuration section
     * @param itemsAdderId the ItemsAdder item ID
     * @return the created ItemStack or fallback if ItemsAdder is not available
     */
    private @Nullable ItemStack createItemsAdderItem(final @NotNull ConfigurationSection config, final @NotNull String itemsAdderId) {
        try {
            // Check if ItemsAdder is available
            if (Bukkit.getPluginManager().getPlugin("ItemsAdder") == null) {
                return createFallbackItem(config, itemsAdderId);
            }
            
            // Try to get ItemsAdder item using reflection
            final Class<?> itemsAdderClass = Class.forName("dev.lone.itemsadder.api.ItemsAdder");
            final java.lang.reflect.Method getCustomItemMethod = itemsAdderClass.getMethod("getCustomItem", String.class);
            final Object itemStack = getCustomItemMethod.invoke(null, itemsAdderId);
            
            if (itemStack instanceof ItemStack) {
                final ItemStack item = (ItemStack) itemStack;
                final ItemMeta meta = item.getItemMeta();
                
                if (meta != null) {
                    // Set display name
                    final String name = config.getString("name");
                    if (name != null) {
                        // Add <!italic> if not already present
                        final String processedName = name.startsWith("<!italic>") ? name : "<!italic>" + name;
                        meta.displayName(miniMessage.deserialize("<!italic><red>ItemsAdder: " + itemsAdderId + "</red>"));
                    }
                    
                    // Set lore
                    final List<String> loreStrings = config.getStringList("lore");
                    if (!loreStrings.isEmpty()) {
                        final List<Component> lore = new ArrayList<>();
                        for (final String loreLine : loreStrings) {
                            // Add <!italic> if not already present
                            final String processedLore = loreLine.startsWith("<!italic>") ? loreLine : "<!italic>" + loreLine;
                            lore.add(miniMessage.deserialize(processedLore));
                        }
                        meta.lore(lore);
                    }
                    
                        lore.add(miniMessage.deserialize("<!italic><gray>ItemsAdder item: " + itemsAdderId + "</gray>"));
                        lore.add(miniMessage.deserialize("<!italic><red>ItemsAdder plugin not available</red>"));
                
                return item;
            }
            
            return createFallbackItem(config, itemsAdderId);
        } catch (final Exception exception) {
            return createFallbackItem(config, itemsAdderId);
        }
    }

    /**
     * Creates a fallback item when ItemsAdder is not available
     * @param config the configuration section
     * @param itemsAdderId the original ItemsAdder ID
     * @return the fallback ItemStack
     */
    private @NotNull ItemStack createFallbackItem(final @NotNull ConfigurationSection config, final @NotNull String itemsAdderId) {
        final ItemStack item = new ItemStack(Material.BARRIER);
        final ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            // Set display name
            final String name = config.getString("name");
            if (name != null) {
                // Add <!italic> if not already present
                final String processedName = name.startsWith("<!italic>") ? name : "<!italic>" + name;
                meta.displayName(miniMessage.deserialize(processedName));
            } else {
                meta.displayName(miniMessage.deserialize("<!italic><red>ItemsAdder: " + itemsAdderId + "</red>"));
            }
            
            // Set lore
            final List<String> loreStrings = config.getStringList("lore");
            if (!loreStrings.isEmpty()) {
                final List<Component> lore = new ArrayList<>();
                for (final String loreLine : loreStrings) {
                    // Add <!italic> if not already present
                    final String processedLore = loreLine.startsWith("<!italic>") ? loreLine : "<!italic>" + loreLine;
                    lore.add(miniMessage.deserialize(processedLore));
                }
                meta.lore(lore);
            } else {
                final List<Component> lore = new ArrayList<>();
                lore.add(miniMessage.deserialize("<!italic><gray>ItemsAdder item: " + itemsAdderId + "</gray>"));
                lore.add(miniMessage.deserialize("<!italic><red>ItemsAdder plugin not available</red>"));
                meta.lore(lore);
            }
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
}