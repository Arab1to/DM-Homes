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

package ch.andre601.dialogrules.listeners;

import ch.andre601.dialogrules.ActionResolver;
import ch.andre601.dialogrules.DialogBuilder;
import ch.andre601.dialogrules.DialogRules;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.event.connection.configuration.AsyncPlayerConnectionConfigureEvent;
import io.papermc.paper.persistence.PersistentDataContainerView;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerConfigPhaseListener implements Listener{
    
    public static void register(){
        new PlayerConfigPhaseListener();
    }
    
    private PlayerConfigPhaseListener(){
        Bukkit.getPluginManager().registerEvents(this, DialogRules.get());
    }
    
    @EventHandler
    @SuppressWarnings("UnstableApiUsage")
    public void onPlayerConfigure(AsyncPlayerConnectionConfigureEvent event){
        if(!DialogRules.get().configManager().bool("showOnJoin"))
            return;
        
        UUID uuid = event.getConnection().getProfile().getId();
        if(uuid == null){
            DialogRules.get().getSLF4JLogger().warn(
                "Unable to fetch UUID for a Player Configure phase!"
            );
            return;
        }
        
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        PersistentDataContainerView pdc = player.getPersistentDataContainer();
        //if(pdc.has(DialogRules.KEY, PersistentDataType.BOOLEAN) && pdc.get(DialogRules.KEY, PersistentDataType.BOOLEAN))
        //    return;
        
        Dialog dialog = DialogBuilder.create(DialogBuilder.MenuType.CONFIRMATION, event.getConnection().getAudience());
        if(dialog == null){
            DialogRules.get().getSLF4JLogger().warn("Unable to display dialog to Player on Config Phase! Dialog was null.");
            return;
        }
        
        CompletableFuture<ActionResolver.ActionType> action = new CompletableFuture<>();
        
        DialogRules.get().queueAction(event.getConnection(), action);
        
        event.getConnection().getAudience().showDialog(dialog);
        
        // Block the thread until the CompletableFuture has been Completed.
        action.join();
        
        DialogRules.get().removeAction(event.getConnection());
    }
}
