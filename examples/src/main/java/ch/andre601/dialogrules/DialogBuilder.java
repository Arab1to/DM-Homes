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

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.key.Key;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public class DialogBuilder{
    
    public static Dialog create(MenuType type, Audience audience){
        UUID uuid = audience.get(Identity.UUID).orElse(null);
        if(uuid == null)
            return null;
        
        DialogBuilder builder = new DialogBuilder(type);
        
        builder.title(DialogRules.get().configManager().string("Server Rules", "title"));
        
        String description = DialogRules.get().configManager().string("", "description");
        if(description != null && !description.isEmpty())
            builder.description(description);
        
        List<RuleEntry> rules = DialogRules.get().configManager().rules();
        if(rules == null || rules.isEmpty())
            return null;
        
        for(int i = 0; i < rules.size(); i++){
            RuleEntry entry = rules.get(i);
            
            builder.rule(entry, i);
        }
        
        return builder.build(uuid);
    }
    
    private final List<DialogBody> bodies = new ArrayList<>();
    
    private final MenuType type;
    
    private String title = "Server Rules";
    
    public DialogBuilder(MenuType type){
        this.type = type;
    }
    
    public DialogBuilder title(String title){
        this.title = title;
        return this;
    }
    
    public DialogBuilder description(String description){
        bodies.add(DialogBody.plainMessage(DialogRules.get().miniMessage().deserialize(description)));
        return this;
    }
    
    public DialogBuilder rule(RuleEntry entry, int index){
        if(entry.text().isEmpty())
            return this;
        
        if(entry.item() != null){
            bodies.add(
                DialogBody.item(entry.item())
                    .description(DialogBody.plainMessage(DialogRules.get().miniMessage().deserialize(
                        DialogRules.get().configManager().string("{num}) {text}", "ruleFormat")
                            .replace("{num}", String.valueOf(index + 1))
                            .replace("{text}", entry.text())
                    )))
                    .showTooltip(false)
                    .build()
            );
        }else{
            bodies.add(DialogBody.plainMessage(DialogRules.get().miniMessage().deserialize(entry.text())));
        }
        return this;
    }
    
    public Dialog build(UUID uuid){
        if(uuid == null)
            throw new IllegalStateException("Received null UUID for Dialog!");
        
        return Dialog.create(
            factory -> {
                if(type == MenuType.CONFIRMATION){
                    factory.empty()
                        .type(DialogType.confirmation(
                            ActionButton.builder(DialogRules.get().miniMessage().deserialize(
                                DialogRules.get().configManager().string("Accept", "buttons", "accept", "text")
                            )).action(DialogAction.customClick(Key.key("dialogrules:accept/" + uuid), null)).build(),
                            ActionButton.builder(DialogRules.get().miniMessage().deserialize(
                                DialogRules.get().configManager().string("Decline", "buttons", "decline", "text")
                            )).action(DialogAction.customClick(Key.key("dialogrules:decline/" + uuid), null)).build()
                        ))
                        .base(
                            DialogBase.builder(DialogRules.get().miniMessage().deserialize(title))
                                .canCloseWithEscape(false)
                                .body(bodies)
                                .build()
                        );
                }else{
                    factory.empty()
                        .type(DialogType.notice())
                        .base(
                            DialogBase.builder(DialogRules.get().miniMessage().deserialize(title))
                                .body(bodies)
                                .build()
                        );
                }
            }
        );
    }
    
    public enum MenuType{
        CONFIRMATION,
        NOTICE
    }
}
