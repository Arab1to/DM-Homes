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
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Builder for creating home-related dialogs using Paper Dialog API
 */
@SuppressWarnings("UnstableApiUsage")
public final class HomeDialogBuilder {

    private final DMHomesPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

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
        
        // Create dialog body
        final List<DialogBody> body = new ArrayList<>();
        body.add(DialogBody.plainMessage(Component.text("Wprowadź nazwę dla swojego nowego domu:", NamedTextColor.WHITE)));
        body.add(DialogBody.plainMessage(Component.empty()));
        body.add(DialogBody.plainMessage(Component.text("• Nazwa może zawierać tylko litery, cyfry i _", NamedTextColor.GRAY)));
        body.add(DialogBody.plainMessage(Component.text("• Maksymalna długość: 16 znaków", NamedTextColor.GRAY)));

        this.plugin.getLogger().info("Creating home creation dialog for player: " + player.getName() + " with ID: " + playerId);

        return Dialog.create(factory -> factory.empty()
            .base(DialogBase.builder(Component.text("Tworzenie nowego domu", NamedTextColor.GOLD, TextDecoration.BOLD))
                .canCloseWithEscape(true)
                .body(body)
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
}