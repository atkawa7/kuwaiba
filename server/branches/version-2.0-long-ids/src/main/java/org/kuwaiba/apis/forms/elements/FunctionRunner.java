/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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
 * The function runner executes the scripts defined in the process engine inside the tags scripts and functions
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class FunctionRunner implements Runner {
    /**
     * Function identity
     */
    private String functionName;
    /**
     * List of parameters names to run in the script
     */
    private List<String> parameterNames = new ArrayList();
    /**
     * Groovy script to run
     */
    private String script;
    /**
     * A container of all the existing functions in the current form artifact and the sources defined in the tag script
     */
    private final ElementScript elementScript;
    /**
     * A runner to execute functions in the level of web service bean
     */
    private ScriptQueryExecutor scriptQueryExecutor;
    
    public FunctionRunner(String functionName, String paramNames, String script, ElementScript elementScript) {
        this.functionName = functionName;
        
        if (paramNames != null) {
            
            String[] parameterNamesArray = paramNames.split(" ");
            
            if (parameterNamesArray != null) {
                parameterNames = new ArrayList();
                parameterNames.addAll(Arrays.asList(parameterNamesArray));
            }
        }
        this.script = script;
        this.elementScript = elementScript;
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
        binding.setVariable("elementScript", elementScript);
                
        if (parameterNames != null && parameters != null && parameterNames.size() == parameters.size()) {
            
            for (int i = 0; i < parameters.size(); i += 1)
                binding.setVariable(parameterNames.get(i), parameters.get(i));
        }
        GroovyShell shell = new GroovyShell(FunctionRunner.class.getClassLoader(), binding);
        System.out.println(">>><<<" + getFunctionName());//Only for test purposes print the function name in production time this line must be commented
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
