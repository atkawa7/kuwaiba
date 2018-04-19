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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class Validator implements Runner {
    private String validatorName;
    private List<String> parameterNames;
    private String script;
    private String message;
    
    public Validator(String validatorName, String paramNames, String script, String message) {
        this.validatorName = validatorName;
        if (paramNames != null) {
            
            String[] parameterNamesArray = paramNames.split(" ");
            
            if (parameterNamesArray != null) {
                parameterNames = new ArrayList();
                parameterNames.addAll(Arrays.asList(parameterNamesArray));
            }
        }
        this.script = script;
        this.message = message; 
    }
    
    public String getValidatorName() {
        return validatorName;
    }
    
    public void setValidatorName(String validatorName) {
        this.validatorName = validatorName;
    }
    
    public List<String> getParameterNames() {
        return parameterNames;                
    }
    
    public String getScript() {
        return script;
    }
    
    public void setScript(String script) {
        this.script = script;
    }
    
    public void setParameterNames(List<String> parameterNames) {
        this.parameterNames = parameterNames;        
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
            
    @Override
    public Object run(List parameters) {
        GroovyShell shell = null;
                
        if (parameterNames != null && parameters != null && parameterNames.size() == parameters.size()) {
            
            Binding binding = new Binding();
            
            for (int i = 0; i < parameters.size(); i += 1)
                binding.setVariable(parameterNames.get(i), parameters.get(i));
            
            shell = new GroovyShell(Function.class.getClassLoader(), binding);
        } else
            shell = new GroovyShell(Function.class.getClassLoader());
                
        return shell.evaluate(script);
    }
    
}
