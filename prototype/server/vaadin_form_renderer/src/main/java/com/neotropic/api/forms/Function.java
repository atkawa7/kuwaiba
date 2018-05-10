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

import com.neotropic.forms.ScriptQueryManager;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class Function implements Runner {
    private String functionName;
    private List<String> parameterNames;
    private String script;
    
    public Function(String functionName, String paramNames, String script) {
        this.functionName = functionName;
        
        if (paramNames != null) {
            
            String[] parameterNamesArray = paramNames.split(" ");
            
            if (parameterNamesArray != null) {
                parameterNames = new ArrayList();
                parameterNames.addAll(Arrays.asList(parameterNamesArray));
            }
        }
        this.script = script;
    }
    
    public String getFunctionName() {
        return functionName;
    }
    
    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }
    
    public List<String> getParametersNames() {
        return parameterNames;        
    }
    
    public void setParametersNames(List<String> parametersNames) {
        this.parameterNames = parametersNames;
    }
    
    public String getScript() {
        return script;        
    }
    
    public void setScript(String script) {
        this.script = script;        
    }
    
    @Override
    public Object run(List parameters) {
        GroovyShell shell = null;
        
        Binding binding = new Binding();
        binding.setVariable("ScriptQueryManager", ScriptQueryManager.class);
                
        if (parameterNames != null && parameters != null && parameterNames.size() == parameters.size()) {
            
            for (int i = 0; i < parameters.size(); i += 1)
                binding.setVariable(parameterNames.get(i), parameters.get(i));
        }
        shell = new GroovyShell(Function.class.getClassLoader(), binding);
        
        script = script.replace("_AND_", "&&");
        return shell.evaluate(script);
    }
}
