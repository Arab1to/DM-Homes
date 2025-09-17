/*
 * MIT License
 *
 * Copyright (c) 2025 Andre_601
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ch.andre601.dialogrules;

import ch.andre601.dialogrules.commands.CmdDialogRules;
import ch.andre601.dialogrules.commands.CmdRules;
import ch.andre601.dialogrules.config.ConfigManager;
import ch.andre601.dialogrules.listeners.ClickListener;
import ch.andre601.dialogrules.listeners.PlayerConfigPhaseListener;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.connection.PlayerCommonConnection;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.PaperCommandManager;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

@SuppressWarnings("UnstableApiUsage")
public final class DialogRules extends JavaPlugin{
    
    public static NamespacedKey KEY;
    private static DialogRules instance;
    
    private final Map<PlayerCommonConnection, CompletableFuture<ActionResolver.ActionType>> awaitingAction = new HashMap<>();
    private final List<UUID> acceptsToProcess = new CopyOnWriteArrayList<>();
    private final MiniMessage mm = MiniMessage.miniMessage();
    
    private ConfigManager configManager;
    
    @Override
    public void onEnable(){
        try{
            Class.forName("io.papermc.paper.dialog.Dialog");
        }catch(ClassNotFoundException ignored){
            getSLF4JLogger().warn("This version of Paper does not have the Dialog Feature for MC 1.21.7+!");
            getSLF4JLogger().warn("Please use at least 1.21.7 of Paper to use this plugin.");
            
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        
        DialogRules.instance = this;
        DialogRules.KEY = NamespacedKey.fromString("accepted_rules", this);
        
        this.configManager = new ConfigManager();
        
        getSLF4JLogger().info("Loading config.yml...");
        configManager.loadConfig();
        
        getSLF4JLogger().info("Registering Events...");
        PlayerConfigPhaseListener.register();
        ClickListener.register();
        
        getSLF4JLogger().info("Register commands...");
        PaperCommandManager<CommandSourceStack> manager = PaperCommandManager.builder()
            .executionCoordinator(ExecutionCoordinator.simpleCoordinator())
            .buildOnEnable(this);
        
        new AnnotationParser<>(manager, CommandSourceStack.class)
            .parse(
                new CmdDialogRules(),
                new CmdRules()
            );
        
        processAccepts();
    }
    
    @Override
    public void onDisable(){
        // Plugin shutdown logic
    }
    
    public static DialogRules get(){
        return instance;
    }
    
    public ConfigManager configManager(){
        return configManager;
    }
    
    public MiniMessage miniMessage(){
        return mm;
    }
    
    public void queueAction(PlayerCommonConnection connection, CompletableFuture<ActionResolver.ActionType> future){
        awaitingAction.put(connection, future);
    }
    
    public void removeAction(PlayerCommonConnection connection){
        awaitingAction.remove(connection);
    }
    
    public void updateActionResult(PlayerCommonConnection connection){
        CompletableFuture<ActionResolver.ActionType> future = awaitingAction.get(connection);
        if(future != null)
            future.complete(null);
    }
    
    public void queueAccepted(UUID uuid){
        acceptsToProcess.add(uuid);
    }
    
    // We cannot set a PersistentDataContainer for a player during Config Phase.
    // So instead we "queue" the UUID to be processed as soon as the Player is made available on the Server.
    private void processAccepts(){
        getServer().getScheduler().runTaskTimer(this, () -> {
            for(UUID uuid : acceptsToProcess){
                Player player = getServer().getPlayer(uuid);
                if(player == null)
                    continue;
                
                PersistentDataContainer pdc = player.getPersistentDataContainer();
                pdc.set(KEY, PersistentDataType.BOOLEAN, true);
                acceptsToProcess.remove(uuid);
            }
        }, 1L, 20L);
    }
}
