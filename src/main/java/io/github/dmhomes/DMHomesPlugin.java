package io.github.dmhomes;

import io.github.dmhomes.commands.HomeCommandExecutor;
import io.github.dmhomes.commands.DMHomesCommandExecutor;
import io.github.dmhomes.config.ConfigManager;
import io.github.dmhomes.config.MessageManager;
import io.github.dmhomes.data.HomeDataManager;
import io.github.dmhomes.data.HomeManager;
import io.github.dmhomes.dialog.HomeCreationDialog;
import io.github.dmhomes.teleportation.TeleportationManager;
import io.github.dmhomes.exceptions.DMHomesException;
import io.github.dmhomes.listeners.GUIListener;
import io.github.dmhomes.listeners.PlayerListener;
import io.github.dmhomes.listeners.DialogClickListener;
import lombok.Getter;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

/**
 * Main plugin class for DM-Homes
 * A standalone home teleportation system with advanced GUI
 */
public final class DMHomesPlugin extends JavaPlugin {

    @Getter
    private static DMHomesPlugin instance;
    
    @Getter
    private ConfigManager configManager;
    
    @Getter
    private MessageManager messageManager;
    
    @Getter
    private HomeDataManager homeDataManager;
    
    @Getter
    private HomeManager homeManager;
    
    @Getter
    private TeleportationManager teleportationManager;
    
    @Getter
    private HomeCreationDialog homeCreationDialog;
    
    private final List<String> homeCommands = Arrays.asList(
        "home", "sethome", "delhome", "dom", "domy", "ustawdom", "dmhomes", "dmh"
    );

    @Override
    public void onEnable() {
        instance = this;
        
        try {
            this.initializePlugin();
            this.getLogger().info("DM-Homes has been successfully enabled!");
        } catch (final DMHomesException exception) {
            this.getLogger().severe("Failed to initialize DM-Homes: " + exception.getMessage());
            this.getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        try {
            // Save all data before shutdown
            if (this.homeDataManager != null) {
                this.homeDataManager.saveAllData();
            }
            if (this.homeManager != null) {
                this.homeManager.saveAllHomes();
            }
            this.getLogger().info("DM-Homes has been successfully disabled!");
        } catch (final Exception exception) {
            this.getLogger().log(Level.SEVERE, "Error during plugin shutdown", exception);
        } finally {
            instance = null;
        }
    }

    /**
     * Initializes all plugin components
     * @throws DMHomesException if initialization fails
     */
    private void initializePlugin() throws DMHomesException {
        this.checkDependencies();
        this.initializeManagers();
        this.registerCommands();
        this.registerListeners();
        this.startTasks();
    }

    /**
     * Checks if optional dependencies are present
     */
    private void checkDependencies() {
        final Plugin itemsAdder = this.getServer().getPluginManager().getPlugin("ItemsAdder");
        if (itemsAdder != null && itemsAdder.isEnabled()) {
            this.getLogger().info("ItemsAdder detected - custom items will be supported");
        } else {
            this.getLogger().info("ItemsAdder not found - using vanilla items only");
        }
    }

    /**
     * Initializes all manager instances
     * @throws DMHomesException if manager initialization fails
     */
    private void initializeManagers() throws DMHomesException {
        try {
            this.configManager = new ConfigManager(this);
            this.messageManager = new MessageManager(this.configManager);
            this.homeDataManager = new HomeDataManager(this);
            this.homeManager = new HomeManager(this);
            this.homeManager.initialize();
            this.teleportationManager = new TeleportationManager(this);
            this.homeCreationDialog = new HomeCreationDialog(this);
        } catch (final Exception exception) {
            throw new DMHomesException("Failed to initialize managers", exception);
        }
    }

    /**
     * Registers all plugin commands
     */
    private void registerCommands() {
        final HomeCommandExecutor commandExecutor = new HomeCommandExecutor(this);
        final DMHomesCommandExecutor dmhCommandExecutor = new DMHomesCommandExecutor(this);
        
        for (final String commandName : this.homeCommands) {
            final PluginCommand command = this.getCommand(commandName);
            if (command != null) {
                if (commandName.equals("dmhomes") || commandName.equals("dmh")) {
                    command.setExecutor(dmhCommandExecutor);
                    command.setTabCompleter(dmhCommandExecutor);
                } else {
                    command.setExecutor(commandExecutor);
                    command.setTabCompleter(commandExecutor);
                }
            } else {
                this.getLogger().warning("Could not register command: " + commandName);
            }
        }
    }

    /**
     * Registers all event listeners
     */
    private void registerListeners() {
        this.getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        this.getServer().getPluginManager().registerEvents(new DialogClickListener(this), this);
    }

    /**
     * Starts recurring tasks
     */
    private void startTasks() {
        final int autoSaveInterval = this.configManager.getAutoSaveInterval();
        if (autoSaveInterval > 0) {
            this.getLogger().info("Starting auto-save task with interval: " + autoSaveInterval + " minutes");
            this.getServer().getScheduler().runTaskTimerAsynchronously(
                this,
                () -> {
                    this.homeDataManager.saveAllData();
                    this.homeManager.saveAllHomes();
                },
                20L * 60L * autoSaveInterval, // Convert minutes to ticks
                20L * 60L * autoSaveInterval
            );
        } else {
            this.getLogger().info("Auto-save disabled (interval: " + autoSaveInterval + ")");
        }
    }

    /**
     * Reloads all plugin configuration and data
     * @throws DMHomesException if reload fails
     */
    public void reloadPlugin() throws DMHomesException {
        try {
            this.configManager.reloadConfig();
            this.messageManager.reloadMessages();
            this.homeDataManager.reloadData();
            this.getLogger().info("Plugin configuration reloaded successfully!");
        } catch (final Exception exception) {
            throw new DMHomesException("Failed to reload plugin", exception);
        }
    }

    /**
     * Gets the plugin instance safely
     * @return the plugin instance
     * @throws IllegalStateException if plugin is not initialized
     */
    public static @NotNull DMHomesPlugin getInstance() {
        if (instance == null) {
            throw new IllegalStateException("DM-Homes plugin is not initialized!");
        }
        return instance;
    }
}