package io.github.dmhomes.config;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Manages plugin messages and provides MiniMessage formatting
 */
@RequiredArgsConstructor
public final class MessageManager {

    private final ConfigManager configManager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    /**
     * Reloads all messages from configuration
     */
    public void reloadMessages() {
        // Messages are loaded directly from config, no caching needed
    }

    /**
     * Gets a message component with placeholders replaced
     * @param messageKey the message key
     * @param placeholders the placeholders to replace (key-value pairs)
     * @return the formatted message component
     */
    public @NotNull Component getMessage(final @NotNull String messageKey, final @NotNull String... placeholders) {
        Objects.requireNonNull(messageKey, "Message key cannot be null");
        
        String message = this.configManager.getMessage(messageKey);
        if (message == null) {
            // Return a more user-friendly fallback message
            switch (messageKey) {
                case "warmup-title":
                    message = "<yellow>Teleporting...</yellow>";
                    break;
                case "warmup-subtitle":
                    message = "<gray>Don't move! {time}s</gray>";
                    break;
                case "blackscreen-title":
                    message = ":blackscreen:";
                    break;
                default:
                    message = "<red>Missing message: " + messageKey + "</red>";
                    break;
            }
        }
        
        // Replace placeholders
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                final String placeholder = placeholders[i];
                final String value = placeholders[i + 1];
                message = message.replace("{" + placeholder + "}", value != null ? value : "null");
            }
        }
        
        return this.miniMessage.deserialize(message);
    }

    /**
     * Gets a message component without placeholder replacement
     * @param messageKey the message key
     * @return the formatted message component
     */
    public @NotNull Component getMessage(final @NotNull String messageKey) {
        return this.getMessage(messageKey, new String[0]);
    }

    /**
     * Gets a raw message string with placeholders replaced
     * @param messageKey the message key
     * @param placeholders the placeholders to replace (key-value pairs)
     * @return the raw message string
     */
    public @NotNull String getRawMessage(final @NotNull String messageKey, final @NotNull String... placeholders) {
        Objects.requireNonNull(messageKey, "Message key cannot be null");
        
        String message = this.configManager.getMessage(messageKey);
        if (message == null) {
            message = "Missing message: " + messageKey;
        }
        
        // Replace placeholders
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                final String placeholder = placeholders[i];
                final String value = placeholders[i + 1];
                message = message.replace("{" + placeholder + "}", value != null ? value : "null");
            }
        }
        
        return message;
    }
}