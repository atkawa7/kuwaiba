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

import java.util.HashMap;
import java.util.List;

/**
 * Executes an script
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class Script123Runner {
    private final HashMap<String, String> sampleCacheOfFunctions;
            
    public Script123Runner() {
        sampleCacheOfFunctions = new HashMap();
        sampleCacheOfFunctions.put("random", "");
    }
    
    /**
     * Runs the script to the given function
     * @param functionName The function name, can no be null if is empty the function is taken like global
     * @param parameters   A set of parameters, can be empty or null
     * @return the function evaluation result
     */
    public Object run(String functionName, List<String> parameters) throws Exception {
        if (functionName == null)
            throw new Exception("Function Name can not be Null");
        
        if (sampleCacheOfFunctions.containsKey("random"))
            return String.valueOf(Math.random());
        return null;
    }
}
