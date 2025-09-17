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

package ch.andre601.dialogrules.commands;

import ch.andre601.dialogrules.DialogRules;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

@Command("dialogrules|dr")
public class CmdDialogRules{
    
    @Command("help")
    @Permission({"dialogrules.admin", "dialogrules.command.help"})
    public void help(CommandSourceStack source){
        CommandSender sender = source.getSender();
        
        sender.sendMessage(DialogRules.get().miniMessage().deserialize(""));
        sender.sendMessage(DialogRules.get().miniMessage().deserialize("Dialog<aqua>Rules <grey>Commands"));
        sender.sendMessage(DialogRules.get().miniMessage().deserialize(""));
        sender.sendMessage(DialogRules.get().miniMessage().deserialize("<aqua>/dr help"));
        sender.sendMessage(DialogRules.get().miniMessage().deserialize("Shows this help page."));
        sender.sendMessage(DialogRules.get().miniMessage().deserialize(""));
        sender.sendMessage(DialogRules.get().miniMessage().deserialize("<aqua>/dr reload"));
        sender.sendMessage(DialogRules.get().miniMessage().deserialize("Reloads the config.yml"));
        sender.sendMessage(DialogRules.get().miniMessage().deserialize(""));
    }
    
    @Command("reload")
    @Permission({"dialogrules.admin", "dialogrules.command.reload"})
    public void reload(CommandSourceStack source){
        String prefix = "<grey>[<white>Dialog<aqua>Rules</aqua></white>] ";
        CommandSender sender = source.getSender();
        
        sender.sendMessage(DialogRules.get().miniMessage().deserialize(prefix + "Reloading config.yml..."));
        
        if(DialogRules.get().configManager().reloadConfig()){
            sender.sendMessage(DialogRules.get().miniMessage().deserialize(prefix + "<green>Config.yml reloaded successfully!"));
        }else{
            sender.sendMessage(DialogRules.get().miniMessage().deserialize(prefix + "<red>Failed to reload config.yml! Check console for details."));
        }
    }
}
