/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.neotropic.api.forms;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class Evaluator {
    private final ElementI18N i18n;
        
    public Evaluator(ElementI18N i18n) {
        this.i18n = i18n;
    }
       
    public String getValue(String input) {        
        if (input != null && input.contains("${") && input.contains("}")) {
            
            String cpyInput = input;

            cpyInput = cpyInput.replace("${", "");
            cpyInput = cpyInput.replace("}", "");
            
            String[] messages = cpyInput.split("\\.");
            
            switch (messages[0]) {
                case "I18N":
                    return i18n.getMessage(messages[1], i18n.getLang());
                case "Query":
                    if (messages.length >= 2 && "orderNumber".equals(messages[1]))
                        return "0001";
                    break;
////                case "Query":
////                    return "TODO Query form engine";
            }            
        } else
            return input;
        
        return input;        
    }
}
