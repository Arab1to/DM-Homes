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

package ch.andre601.dialogrules.config;

import ch.andre601.dialogrules.DialogRules;
import ch.andre601.dialogrules.RuleEntry;
import org.slf4j.Logger;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class ConfigManager{
    
    private final Path config;
    private final Logger logger;
    
    ConfigurationNode node = null;
    
    public ConfigManager(){
        this.config = DialogRules.get().getDataPath().resolve("config.yml");
        this.logger = DialogRules.get().getSLF4JLogger();
    }
    
    public boolean loadConfig(){
        File folder = DialogRules.get().getDataFolder();
        if(!folder.exists() && !folder.mkdirs()){
            logger.warn("Unable to create folder for plugin!");
            return false;
        }
        
        if(!Files.exists(config)){
            try(InputStream stream = DialogRules.get().getClass().getResourceAsStream("/config.yml")){
                if(stream == null){
                    logger.warn("Unable to create config.yml. InputStream was null!");
                    return false;
                }
                
                Files.copy(stream, config);
                logger.info("Successfully created config.yml!");
            }catch(IOException ex){
                logger.warn("Encountered IOException will creating config.yml!", ex);
                return false;
            }
        }
        
        return reloadConfig();
    }
    
    public boolean reloadConfig(){
        YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
            .path(config)
            .defaultOptions(options -> options.serializers(
                builder -> builder.register(RuleEntry.class, RuleEntryDeserializer.INSTANCE)
            ))
            .build();
        
        try{
            return (node = loader.load()) != null;
        }catch(IOException ex){
            logger.warn("Encountered IOException while loading config.yml!", ex);
            return false;
        }
    }
    
    public String string(String def, Object... path){
        return node.node(path).getString(def);
    }
    
    public boolean bool(Object... path){
        return node.node(path).getBoolean();
    }
    
    public ShowOn showOnPhase(){
        return ShowOn.resolve(node.node("showOn").getString("none"));
    }
    
    public List<RuleEntry> rules(){
        try{
            return node.node("rules").getList(RuleEntry.class);
        }catch(SerializationException ex){
            return Collections.emptyList();
        }
    }
    
    public enum ShowOn{
        CONFIGURE,
        JOIN,
        NONE;
        
        private static final ShowOn[] values = values();
        
        public static ShowOn resolve(String value){
            for(ShowOn showOn : values){
                if(showOn.name().equalsIgnoreCase(value))
                    return showOn;
            }
            
            return ShowOn.NONE;
        }
    }
}
