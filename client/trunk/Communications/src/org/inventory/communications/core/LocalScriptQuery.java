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
package org.inventory.communications.core;

import java.util.HashMap;
import org.inventory.communications.wsclient.RemoteScriptQuery;
import org.inventory.communications.wsclient.StringPair;

/**
 * A local representation of a Script Query
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class LocalScriptQuery {
    /**
     * Script Query Id
     */
    private Long id;
    /**
     * Script Query Name
     */
    private String name;
    /**
     * Script Query Description
     */
    private String description;
    /**
     * Script Query code block (script)
     */
    private String script;
    /**
     * Script Query Parameters name/value pair
     */
    private HashMap<String, String> parameters;
    
    public LocalScriptQuery() {
    }
    
    public LocalScriptQuery(RemoteScriptQuery remoteScriptQuery) {
        if (remoteScriptQuery == null) {
            this.id = remoteScriptQuery.getId();
            this.name = remoteScriptQuery.getName();
            this.description = remoteScriptQuery.getDescription();
            this.script = remoteScriptQuery.getScript();

            if (remoteScriptQuery.getParameters() != null) {
                for (StringPair parameter : remoteScriptQuery.getParameters())
                    parameters.put(parameter.getKey(), parameter.getValue());
            }
        }
    }
        
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;        
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getScript() {
        return script;        
    }
    
    public void setScript(String script) {
        this.script = script;        
    }
    
    public HashMap<String, String> getParameters() {
        return parameters;
    }
    
    public void setParameters(HashMap<String, String> parameters) {
        this.parameters = parameters;        
    }
}
