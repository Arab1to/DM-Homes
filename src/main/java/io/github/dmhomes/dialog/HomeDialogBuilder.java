package io.github.dmhomes.dialog;

import io.github.dmhomes.DMHomesPlugin;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Builder for creating home-related dialogs using Paper Dialog API
 */
public final class HomeDialogBuilder {

    private final DMHomesPlugin plugin;

    public HomeDialogBuilder(final @NotNull DMHomesPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "Plugin cannot be null");
    }

    /**
     * Creates a home creation dialog with text input
     * @param player the player
     * @param callback the callback when input is received
     * @return the created dialog
     */
    public @NotNull Dialog createHomeCreationDialog(final @NotNull Player player, 
                                                   final @NotNull java.util.function.Consumer<String> callback) {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(callback, "Callback cannot be null");

        final UUID playerId = player.getUniqueId();
        
        // Get dialog configuration
        final var dialogConfig = this.plugin.getConfig().getConfigurationSection("dialogs.create-home");
        if (dialogConfig == null) {
            // Fallback to hardcoded values
            return createFallbackCreateHomeDialog(playerId);
        }

        final var title = dialogConfig.getString("title", "<gold><bold>Tworzenie nowego domu</bold></gold>");
        final var bodyLines = dialogConfig.getStringList("body");
        final var createButtonText = dialogConfig.getString("buttons.create.text", "<green>Utwórz dom</green>");
        final var createButtonTooltip = dialogConfig.getString("buttons.create.tooltip", "<gray>Kliknij aby utworzyć dom</gray>");
        final var cancelButtonText = dialogConfig.getString("buttons.cancel.text", "<red>Anuluj</red>");
        final var cancelButtonTooltip = dialogConfig.getString("buttons.cancel.tooltip", "<gray>Kliknij aby anulować</gray>");
        final var inputPlaceholder = dialogConfig.getString("input.placeholder", "<gray>Nazwa domu</gray>");
        final var inputMaxLength = dialogConfig.getInt("input.max-length", 16);

        // Convert body lines to DialogBody
        final List<DialogBody> body = new ArrayList<>();
        for (final String line : bodyLines) {
            body.add(DialogBody.plainMessage(this.plugin.getMessageManager().getMessage(line)));
        }

        return Dialog.create(factory -> factory.empty()
            .base(DialogBase.builder(this.plugin.getMessageManager().getMessage(title))
                .canCloseWithEscape(true)
                .body(body)
                .inputs(List.of(
                    DialogInput.text("home_name", this.plugin.getMessageManager().getMessage(inputPlaceholder))
                        .maxLength(inputMaxLength)
                        .build()
                ))
                .build()
            )
            .type(DialogType.confirmation(
                ActionButton.builder(this.plugin.getMessageManager().getMessage(createButtonText))
                    .tooltip(this.plugin.getMessageManager().getMessage(createButtonTooltip))
                    .action(DialogAction.customClick(Key.key("dmhomes:create_home/" + playerId), null))
                    .build(),
                ActionButton.builder(this.plugin.getMessageManager().getMessage(cancelButtonText))
                    .tooltip(this.plugin.getMessageManager().getMessage(cancelButtonTooltip))
                    .action(null) // null action closes the dialog
                    .build()
            ))
        );
    }

    /**
     * Creates a home deletion confirmation dialog
     * @param player the player
     * @param homeName the home name to delete
     * @param callback the callback when confirmed
     * @return the created dialog
     */
    public @NotNull Dialog createHomeDeletionDialog(final @NotNull Player player, 
                                                   final @NotNull String homeName,
                                                   final @NotNull Runnable callback) {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(homeName, "Home name cannot be null");
        Objects.requireNonNull(callback, "Callback cannot be null");

        final UUID playerId = player.getUniqueId();
        
        return Dialog.create(factory -> factory.empty()
            .base(DialogBase.builder(Component.text("Usuwanie domu", NamedTextColor.RED, TextDecoration.BOLD))
                .canCloseWithEscape(true)
                .body(List.of(
                    DialogBody.plainMessage(Component.text("Czy na pewno chcesz usunąć dom: " + homeName + "?", NamedTextColor.WHITE)),
                    DialogBody.plainMessage(Component.empty()),
                    DialogBody.plainMessage(Component.text("⚠ Ta akcja jest nieodwracalna!", NamedTextColor.RED, TextDecoration.BOLD))
                ))
                .build()
            )
            .type(DialogType.confirmation(
                ActionButton.builder(Component.text("Tak, usuń", TextColor.color(0xFF6B6B)))
                    .tooltip(Component.text("Kliknij aby potwierdzić usunięcie"))
                    .action(DialogAction.customClick(Key.key("dmhomes:delete_home/" + playerId), null))
                    .build(),
                ActionButton.builder(Component.text("Nie, anuluj", TextColor.color(0xAEFFC1)))
                    .tooltip(Component.text("Kliknij aby anulować"))
                    .action(null) // null action closes the dialog
                    .build()
            ))
        );
    }

    /**
     * Creates a home rename dialog with text input
     * @param player the player
     * @param oldHomeName the current home name
     * @param callback the callback when new name is received
     * @return the created dialog
     */
    public @NotNull Dialog createHomeRenameDialog(final @NotNull Player player, 
                                                 final @NotNull String oldHomeName,
                                                 final @NotNull java.util.function.Consumer<String> callback) {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(oldHomeName, "Old home name cannot be null");
        Objects.requireNonNull(callback, "Callback cannot be null");

        final UUID playerId = player.getUniqueId();
        
        return Dialog.create(factory -> factory.empty()
            .base(DialogBase.builder(Component.text("Zmiana nazwy domu", NamedTextColor.YELLOW, TextDecoration.BOLD))
                .canCloseWithEscape(true)
                .body(List.of(
                    DialogBody.plainMessage(Component.text("Wprowadź nową nazwę dla domu:", NamedTextColor.WHITE)),
                    DialogBody.plainMessage(Component.text("Obecna nazwa: " + oldHomeName, NamedTextColor.GRAY)),
                    DialogBody.plainMessage(Component.empty()),
                    DialogBody.plainMessage(Component.text("• Nazwa może zawierać tylko litery, cyfry i _", NamedTextColor.GRAY)),
                    DialogBody.plainMessage(Component.text("• Maksymalna długość: 16 znaków", NamedTextColor.GRAY))
                ))
                .inputs(List.of(
                    DialogInput.text("new_home_name", Component.text("Nowa nazwa", NamedTextColor.GREEN))
                        .maxLength(16)
                        .build()
                ))
                .build()
            )
            .type(DialogType.confirmation(
                ActionButton.builder(Component.text("Zmień nazwę", TextColor.color(0xFFE066)))
                    .tooltip(Component.text("Kliknij aby zmienić nazwę"))
                    .action(DialogAction.customClick(Key.key("dmhomes:rename_home/" + playerId), null))
                    .build(),
                ActionButton.builder(Component.text("Anuluj", TextColor.color(0xFFA0B1)))
                    .tooltip(Component.text("Kliknij aby anulować"))
                    .action(null) // null action closes the dialog
                    .build()
            ))
        );
    }

    /**
     * Creates a fallback create home dialog when config is not available
     * @param playerId the player ID
     * @return the fallback dialog
     */
    private @NotNull Dialog createFallbackCreateHomeDialog(final @NotNull UUID playerId) {
        return Dialog.create(factory -> factory.empty()
            .base(DialogBase.builder(Component.text("Tworzenie nowego domu", NamedTextColor.GOLD, TextDecoration.BOLD))
                .canCloseWithEscape(true)
                .body(List.of(
                    DialogBody.plainMessage(Component.text("Wprowadź nazwę dla swojego nowego domu:", NamedTextColor.WHITE)),
                    DialogBody.plainMessage(Component.empty()),
                    DialogBody.plainMessage(Component.text("• Nazwa może zawierać tylko litery, cyfry i _", NamedTextColor.GRAY)),
                    DialogBody.plainMessage(Component.text("• Maksymalna długość: 16 znaków", NamedTextColor.GRAY))
                ))
                .inputs(List.of(
                    DialogInput.text("home_name", Component.text("Nazwa domu", NamedTextColor.GREEN))
                        .maxLength(16)
                        .build()
                ))
                .build()
            )
            .type(DialogType.confirmation(
                ActionButton.builder(Component.text("Utwórz dom", TextColor.color(0xAEFFC1)))
                    .tooltip(Component.text("Kliknij aby utworzyć dom"))
                    .action(DialogAction.customClick(Key.key("dmhomes:create_home/" + playerId), null))
                    .build(),
                ActionButton.builder(Component.text("Anuluj", TextColor.color(0xFFA0B1)))
                    .tooltip(Component.text("Kliknij aby anulować"))
                    .action(null) // null action closes the dialog
                    .build()
            ))
        );
    }

}