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

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import java.util.HashMap;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ScriptRunner1 {
    private static final String VARIABLE_NAME_ELEMENTS = "elements";
        
    private final HashMap<String, String> functions;
    private final Binding binding = new Binding();
    
    public ScriptRunner1(HashMap<String, AbstractElement> elements, HashMap<String, String> functions) {
        this.functions = functions;
        addVariable(VARIABLE_NAME_ELEMENTS, elements);
//        addVariable(ChangeDescriptor.class.getSimpleName(), ChangeDescriptor.class);
    }
    
    public void addVariable(String name, Object value) {
        binding.setVariable(name, value);
    }
    
    public void run(String function) {
        if (function != null) {
            if (functions.containsKey(function)) {
                String script = functions.get(function);
                
                binding.getVariable(VARIABLE_NAME_ELEMENTS);
                                                                
                GroovyShell shell = new GroovyShell(ElementUtil.class.getClassLoader(), binding);
                Object result = shell.evaluate(script);
                                
                
                int i = 0;
            }
        }
    }
}
