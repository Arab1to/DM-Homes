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
        body.add(DialogBody.plainMessage(this.miniMessage.deserialize(
            this.plugin.getConfigManager().getConfig().getString("dialogs.create-home.body.0", 
                "<white>Wprowadź nazwę dla swojego nowego domu:</white>"))));
        body.add(DialogBody.plainMessage(Component.empty()));
        body.add(DialogBody.plainMessage(this.miniMessage.deserialize(
            this.plugin.getConfigManager().getConfig().getString("dialogs.create-home.body.1", 
                "<gray>• Nazwa może zawierać tylko litery, cyfry i _</gray>"))));
        body.add(DialogBody.plainMessage(this.miniMessage.deserialize(
            this.plugin.getConfigManager().getConfig().getString("dialogs.create-home.body.2", 
                "<gray>• Maksymalna długość: 16 znaków</gray>"))));

        this.plugin.getLogger().info("Creating home creation dialog for player: " + player.getName() + " with ID: " + playerId);

        return Dialog.create(factory -> factory.empty()
            .base(DialogBase.builder(this.miniMessage.deserialize(
                this.plugin.getConfigManager().getConfig().getString("dialogs.create-home.title", 
                    "<gold><bold>Tworzenie nowego domu</bold></gold>")))
                .canCloseWithEscape(true)
                .body(body)
                .inputs(List.of(
                    DialogInput.text("home_name", this.miniMessage.deserialize(
                        this.plugin.getConfigManager().getConfig().getString("dialogs.create-home.input.placeholder", 
                            "<green>Nazwa domu</green>")))
                        .maxLength(16)
                        .build()
                ))
                .build()
            )
            .type(DialogType.confirmation(
                ActionButton.builder(this.miniMessage.deserialize(
                    this.plugin.getConfigManager().getConfig().getString("dialogs.create-home.buttons.create.text", 
                        "<green>Utwórz dom</green>")))
                    .tooltip(this.miniMessage.deserialize(
                        this.plugin.getConfigManager().getConfig().getString("dialogs.create-home.buttons.create.tooltip", 
                            "<gray>Kliknij aby utworzyć dom</gray>")))
                    .action(DialogAction.customClick(Key.key("dmhomes:create_home/" + playerId), null))
                    .build(),
                ActionButton.builder(this.miniMessage.deserialize(
                    this.plugin.getConfigManager().getConfig().getString("dialogs.create-home.buttons.cancel.text", 
                        "<red>Anuluj</red>")))
                    .tooltip(this.miniMessage.deserialize(
                        this.plugin.getConfigManager().getConfig().getString("dialogs.create-home.buttons.cancel.tooltip", 
                            "<gray>Kliknij aby anulować</gray>")))
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
            .base(DialogBase.builder(this.miniMessage.deserialize(
                this.plugin.getConfigManager().getConfig().getString("dialogs.delete-home.title", 
                    "<red><bold>Usuwanie domu</bold></red>")))
                .canCloseWithEscape(true)
                .body(List.of(
                    DialogBody.plainMessage(this.miniMessage.deserialize(
                        this.plugin.getConfigManager().getConfig().getString("dialogs.delete-home.body.0", 
                            "<white>Czy na pewno chcesz usunąć dom '{home_name}'?</white>")
                            .replace("{home_name}", homeName))),
                    DialogBody.plainMessage(Component.empty()),
                    DialogBody.plainMessage(this.miniMessage.deserialize(
                        this.plugin.getConfigManager().getConfig().getString("dialogs.delete-home.body.1", 
                            "<red><bold>⚠ Ta akcja jest nieodwracalna!</bold></red>")))
                ))
                .build()
            )
            .type(DialogType.confirmation(
                ActionButton.builder(this.miniMessage.deserialize(
                    this.plugin.getConfigManager().getConfig().getString("dialogs.delete-home.buttons.confirm.text", 
                        "<red>Tak, usuń</red>")))
                    .tooltip(this.miniMessage.deserialize(
                        this.plugin.getConfigManager().getConfig().getString("dialogs.delete-home.buttons.confirm.tooltip", 
                            "<gray>Kliknij aby potwierdzić usunięcie</gray>")))
                    .action(DialogAction.customClick(Key.key("dmhomes:delete_home/" + playerId), null))
                    .build(),
                ActionButton.builder(this.miniMessage.deserialize(
                    this.plugin.getConfigManager().getConfig().getString("dialogs.delete-home.buttons.cancel.text", 
                        "<green>Nie, anuluj</green>")))
                    .tooltip(this.miniMessage.deserialize(
                        this.plugin.getConfigManager().getConfig().getString("dialogs.delete-home.buttons.cancel.tooltip", 
                            "<gray>Kliknij aby anulować</gray>")))
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
            .base(DialogBase.builder(this.miniMessage.deserialize(
                this.plugin.getConfigManager().getConfig().getString("dialogs.rename-home.title", 
                    "<yellow><bold>Zmiana nazwy domu</bold></yellow>")))
                .canCloseWithEscape(true)
                .body(List.of(
                    DialogBody.plainMessage(this.miniMessage.deserialize(
                        this.plugin.getConfigManager().getConfig().getString("dialogs.rename-home.body.0", 
                            "<white>Wprowadź nową nazwę dla domu '{home_name}':</white>")
                            .replace("{home_name}", oldHomeName))),
                    DialogBody.plainMessage(Component.empty()),
                    DialogBody.plainMessage(this.miniMessage.deserialize(
                        this.plugin.getConfigManager().getConfig().getString("dialogs.rename-home.body.1", 
                            "<gray>• Nazwa może zawierać tylko litery, cyfry i _</gray>"))),
                    DialogBody.plainMessage(this.miniMessage.deserialize(
                        this.plugin.getConfigManager().getConfig().getString("dialogs.rename-home.body.2", 
                            "<gray>• Maksymalna długość: 16 znaków</gray>")))
                ))
                .inputs(List.of(
                    DialogInput.text("new_home_name", this.miniMessage.deserialize(
                        this.plugin.getConfigManager().getConfig().getString("dialogs.rename-home.input.placeholder", 
                            "<green>Nowa nazwa</green>")))
                        .maxLength(16)
                        .build()
                ))
                .build()
            )
            .type(DialogType.confirmation(
                ActionButton.builder(this.miniMessage.deserialize(
                    this.plugin.getConfigManager().getConfig().getString("dialogs.rename-home.buttons.confirm.text", 
                        "<yellow>Zmień nazwę</yellow>")))
                    .tooltip(this.miniMessage.deserialize(
                        this.plugin.getConfigManager().getConfig().getString("dialogs.rename-home.buttons.confirm.tooltip", 
                            "<gray>Kliknij aby zmienić nazwę</gray>")))
                    .action(DialogAction.customClick(Key.key("dmhomes:rename_home/" + playerId), null))
                    .build(),
                ActionButton.builder(this.miniMessage.deserialize(
                    this.plugin.getConfigManager().getConfig().getString("dialogs.rename-home.buttons.cancel.text", 
                        "<red>Anuluj</red>")))
                    .tooltip(this.miniMessage.deserialize(
                        this.plugin.getConfigManager().getConfig().getString("dialogs.rename-home.buttons.cancel.tooltip", 
                            "<gray>Kliknij aby anulować</gray>")))
                    .action(null) // null action closes the dialog
                    .build()
            ))
        );
    }
}