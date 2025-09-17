package io.github.dmhomes.commands;

import io.github.dmhomes.DMHomesPlugin;
import io.github.dmhomes.gui.MainHomesGUI;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Command executor for all home-related commands
 * Intercepts commands and opens GUI instead of executing default behavior
 */
@RequiredArgsConstructor
public final class HomeCommandExecutor implements CommandExecutor, TabCompleter {

    private final DMHomesPlugin plugin;

    @Override
    public boolean onCommand(final @NotNull CommandSender sender, 
                           final @NotNull Command command, 
                           final @NotNull String label, 
                           final @NotNull String[] args) {
        Objects.requireNonNull(sender, "Sender cannot be null");
        Objects.requireNonNull(command, "Command cannot be null");
        
        // Only players can use GUI commands
        if (!(sender instanceof Player)) {
            sender.sendMessage(this.plugin.getMessageManager()
                .getMessage("error-player-only", "command", label));
            return true;
        }
        
        final Player player = (Player) sender;
        
        // Check permissions
        if (!player.hasPermission("dmhomes.use") || !player.hasPermission("dmhomes.gui")) {
            player.sendMessage(this.plugin.getMessageManager().getMessage("error-no-permission"));
            return true;
        }
        
        // Open the main homes GUI for all home-related commands
        try {
            final MainHomesGUI gui = new MainHomesGUI(this.plugin, player);
            gui.open();
        } catch (final Exception exception) {
            this.plugin.getLogger().severe("Failed to open homes GUI for player " + player.getName() + ": " + exception.getMessage());
            player.sendMessage(this.plugin.getMessageManager()
                .getMessage("error-generic", "error", "Failed to open homes GUI"));
        }
        
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(final @NotNull CommandSender sender, 
                                               final @NotNull Command command, 
                                               final @NotNull String alias, 
                                               final @NotNull String[] args) {
        // Since we're intercepting commands to open GUI, we don't need tab completion
        // Return empty list to disable tab completion for these commands
        return new ArrayList<>();
    }
}