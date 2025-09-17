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

import java.util.Locale;

public class ActionResolver{
    
    private static final ResolvedAction DUMMY_ACTION = new ResolvedAction(ActionType.NONE, "");
    
    public static ResolvedAction resolve(String value){
        if(value == null || value.isEmpty())
            return DUMMY_ACTION;
        
        ActionType type = ActionType.resolve(value);
        // Check if value is just '<type>' or '<type>:' or ActionType.NONE
        if(type.length() + 1 >= value.length() || type == ActionType.NONE)
            return DUMMY_ACTION;
        
        return new ResolvedAction(type, value.substring(type.length() + 1));
    }
    
    public record ResolvedAction(ActionType type, String value){}
    
    public enum ActionType{
        COMMAND,
        DISCONNECT,
        NONE;
        
        private static final ActionType[] values = values();
        
        public static ActionType resolve(String text){
            for(ActionType value : values){
                if(text.toLowerCase(Locale.ROOT).startsWith(value.name().toLowerCase(Locale.ROOT)))
                    return value;
            }
            
            return ActionType.NONE;
        }
        
        public int length(){
            return this.name().length();
        }
    }
}
