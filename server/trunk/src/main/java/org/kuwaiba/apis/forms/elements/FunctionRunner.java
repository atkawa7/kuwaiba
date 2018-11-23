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
package org.kuwaiba.apis.forms.elements;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class FunctionRunner implements Runner {
    private String functionName;
    private List<String> parameterNames = new ArrayList();;
    private String script;
        
    private ScriptQueryExecutor scriptQueryExecutor;
    
    public FunctionRunner(String functionName, String paramNames, String script) {
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
        
        Binding binding = new Binding();
        
        binding.setVariable("scriptQueryExecutor", scriptQueryExecutor);
                
        if (parameterNames != null && parameters != null && parameterNames.size() == parameters.size()) {
            
            for (int i = 0; i < parameters.size(); i += 1)
                binding.setVariable(parameterNames.get(i), parameters.get(i));
        }
        GroovyShell shell = new GroovyShell(FunctionRunner.class.getClassLoader(), binding);
        
        return shell.evaluate(script);
    }

    @Override
    public ScriptQueryExecutor getScriptQueryExecutor() {
        return scriptQueryExecutor;
    }
    
    @Override
    public void setScriptQueryExecutor(ScriptQueryExecutor scriptQueryExecutor) {
        this.scriptQueryExecutor = scriptQueryExecutor;
    }
}
