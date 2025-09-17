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
import ch.andre601.dialogrules.DialogRules;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Locale;
import java.util.UUID;

public class ClickListener implements Listener{
    
    public static void register(){
        new ClickListener();
    }
    
    private ClickListener(){
        Bukkit.getPluginManager().registerEvents(this, DialogRules.get());
    }
    
    @EventHandler
    @SuppressWarnings("UnstableApiUsage")
    public void onCustomClick(PlayerCustomClickEvent event){
        String key = event.getIdentifier().key().asString().toLowerCase(Locale.ROOT);
        if(!key.startsWith("dialogrules:accept/") && !key.startsWith("dialogrules:decline/"))
            return;
        
        String type = key.startsWith("dialogrules:accept/") ? "accept" : "decline";
        UUID uuid = UUID.fromString(key.startsWith("dialogrules:accept/") ?
            key.substring("dialogrules:accept/".length()) :
            key.substring("dialogrules:decline/".length()));
        
        ActionResolver.ResolvedAction action = ActionResolver.resolve(
            DialogRules.get().configManager().string("NONE", "buttons", type, "action")
        );
        
        switch(action.type()){
            case COMMAND -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), action.value().replace("{uuid}", uuid.toString()));
            case DISCONNECT -> event.getCommonConnection().disconnect(DialogRules.get().miniMessage().deserialize(action.value()));
            case NONE -> {}
        }
        
        DialogRules.get().updateActionResult(event.getCommonConnection());
        
        if(type.equals("accept"))
            DialogRules.get().queueAccepted(uuid);
    }
}
