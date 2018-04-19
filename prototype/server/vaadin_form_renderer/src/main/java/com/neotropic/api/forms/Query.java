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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class Query implements Runner {
    private String name;
    private String queryName;
    private List<String> parameterNames;
    
    public Query(String name, String queryName, String paramNames) {
        this.name = name;
        this.queryName = queryName;
        if (paramNames != null) {
            
            String[] parameterNamesArray = paramNames.split(" ");
            
            if (parameterNamesArray != null) {
                parameterNames = new ArrayList();
                parameterNames.addAll(Arrays.asList(parameterNamesArray));
            }
        }
    }
    
    public String getName() {
        return name;
    }
        
    public void setName(String name) {
        this.name = name;
    }
    
    public String getQueryName() {
        return queryName;
    }
    
    public void setQueryName(String queryName) {
        this.queryName = queryName;        
    }
    
    public List<String> getParameterNames() {
        return parameterNames;
    }
    
    public void setParameterNames(List<String> parameterNames) {
        this.parameterNames = parameterNames;        
    } 

    @Override
    public Object run(List parameters) {
        return ElementQuery.getInstance().executeQuery(queryName);
    }
}
