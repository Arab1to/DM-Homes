package io.github.dmhomes.commands;

import io.github.dmhomes.DMHomesPlugin;
import io.github.dmhomes.data.Home;
import io.github.dmhomes.exceptions.DMHomesException;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Command executor for the main DM-Homes command
 */
@RequiredArgsConstructor
public final class DMHomesCommandExecutor implements CommandExecutor, TabCompleter {

    private final DMHomesPlugin plugin;

    @Override
    public boolean onCommand(final @NotNull CommandSender sender, 
                           final @NotNull Command command, 
                           final @NotNull String label, 
                           final @NotNull String[] args) {
        Objects.requireNonNull(sender, "Sender cannot be null");
        Objects.requireNonNull(command, "Command cannot be null");

        if (args.length == 0) {
            this.sendHelp(sender);
            return true;
        }

        final String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
                return this.handleReload(sender);
            case "list":
                return this.handleList(sender);
            case "info":
                return this.handleInfo(sender, args);
            case "help":
            default:
                this.sendHelp(sender);
                return true;
        }
    }

    /**
     * Handles the reload subcommand
     * @param sender the command sender
     * @return true if handled
     */
    private boolean handleReload(final @NotNull CommandSender sender) {
        if (!sender.hasPermission("dmhomes.admin.reload")) {
            sender.sendMessage(this.plugin.getMessageManager().getMessage("error-no-permission"));
            return true;
        }

        try {
            this.plugin.reloadPlugin();
            sender.sendMessage(this.plugin.getMessageManager().getMessage("plugin-reloaded"));
        } catch (final DMHomesException exception) {
            sender.sendMessage(this.plugin.getMessageManager()
                .getMessage("error-generic", "error", exception.getMessage()));
        }

        return true;
    }

    /**
     * Handles the list subcommand
     * @param sender the command sender
     * @return true if handled
     */
    private boolean handleList(final @NotNull CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("This command can only be used by players."));
            return true;
        }

        final Player player = (Player) sender;
        final List<Home> homes = this.plugin.getHomeManager().getHomes(player);
        final int maxHomes = this.plugin.getHomeManager().getMaxHomes(player);

        // Send header
        sender.sendMessage(this.plugin.getMessageManager()
            .getMessage("home-list-header", 
                "count", String.valueOf(homes.size()),
                "max", String.valueOf(maxHomes)));

        if (homes.isEmpty()) {
            sender.sendMessage(this.plugin.getMessageManager().getMessage("home-list-empty"));
        } else {
            for (final Home home : homes) {
                sender.sendMessage(this.plugin.getMessageManager()
                    .getMessage("home-list-entry",
                        "home_name", home.getName(),
                        "world", home.getWorldName(),
                        "x", String.valueOf(home.getBlockX()),
                        "y", String.valueOf(home.getBlockY()),
                        "z", String.valueOf(home.getBlockZ())));
            }
        }

        return true;
    }

    /**
     * Handles the info subcommand
     * @param sender the command sender
     * @param args the command arguments
     * @return true if handled
     */
    private boolean handleInfo(final @NotNull CommandSender sender, final @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("This command can only be used by players."));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /dmhomes info <home_name>"));
            return true;
        }

        final Player player = (Player) sender;
        final String homeName = args[1];
        final Home home = this.plugin.getHomeManager().getHome(player, homeName);

        if (home == null) {
            sender.sendMessage(this.plugin.getMessageManager()
                .getMessage("error-home-not-found", "home_name", homeName));
            return true;
        }

        // Send home information
        sender.sendMessage(Component.text("§6Home Information: §e" + home.getName()));
        sender.sendMessage(Component.text("§7World: §f" + home.getWorldName()));
        sender.sendMessage(Component.text("§7Location: §f" + home.getBlockX() + ", " + home.getBlockY() + ", " + home.getBlockZ()));
        sender.sendMessage(Component.text("§7Created: §f" + new java.util.Date(home.getCreatedAt())));

        return true;
    }

    /**
     * Sends help information to the sender
     * @param sender the command sender
     */
    private void sendHelp(final @NotNull CommandSender sender) {
        sender.sendMessage(Component.text("§6DM-Homes Commands:"));
        sender.sendMessage(Component.text("§e/dmhomes list §7- List all your homes"));
        sender.sendMessage(Component.text("§e/dmhomes info <home> §7- Show home information"));
        sender.sendMessage(Component.text("§e/dmhomes help §7- Show this help"));
        
        if (sender.hasPermission("dmhomes.admin.reload")) {
            sender.sendMessage(Component.text("§e/dmhomes reload §7- Reload plugin configuration"));
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(final @NotNull CommandSender sender, 
                                               final @NotNull Command command, 
                                               final @NotNull String alias, 
                                               final @NotNull String[] args) {
        if (args.length == 1) {
            final List<String> subCommands = new ArrayList<>(Arrays.asList("list", "info", "help"));
            if (sender.hasPermission("dmhomes.admin.reload")) {
                subCommands.add("reload");
            }
            
            return subCommands.stream()
                .filter(sub -> sub.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("info") && sender instanceof Player) {
            final Player player = (Player) sender;
            return this.plugin.getHomeManager().getHomes(player).stream()
                .map(Home::getName)
                .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}