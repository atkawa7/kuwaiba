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
package org.kuwaiba.apis.persistence.application;

import java.io.Serializable;
import java.util.List;
import org.kuwaiba.interfaces.ws.todeserialize.StringPair;

/**
 * Class used to define a special type of query which execute an script and return a result
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ScriptQuery implements Serializable {
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
    private List<StringPair> parameters;
    
    public ScriptQuery(Long id, String name, String description, String script, List<StringPair> parameters) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.script = script;
        this.parameters = parameters;
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
    
    public List<StringPair> getParameters() {
        return parameters;
    }
    
    public void setParameters(List<StringPair> parameters) {
        this.parameters = parameters;        
    }
}
