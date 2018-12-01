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
package org.kuwaiba.interfaces.ws.toserialize.application;

import java.io.Serializable;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.kuwaiba.apis.persistence.application.ScriptQuery;
import org.kuwaiba.apis.persistence.util.StringPair;

/**
 * Remote representation of script query
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteScriptQuery implements Serializable {
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
     * Used to know if the Script Query return a collection
     */
    private String countable;
    /**
     * Script Query Parameters name/value pair
     */
    private List<StringPair> parameters;
    
    public RemoteScriptQuery() {
    }
    
    public RemoteScriptQuery(ScriptQuery scriptQuery) {
        this.id = scriptQuery.getId();
        this.name = scriptQuery.getName();
        this.description = scriptQuery.getDescription();
        this.script = scriptQuery.getScript();
        this.countable = scriptQuery.getCountable();
        this.parameters = scriptQuery.getParameters();
    }
    
    public RemoteScriptQuery(Long id, String name, String description, String script, String countable, List<StringPair> parameters) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.script = script;
        this.countable = countable;
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
    
    public String getCountable() {
        return countable;
    }
    
    public void setCountable(String countable) {
        this.countable = countable;        
    }
    
    public List<StringPair> getParameters() {
        return parameters;
    }
    
    public void setParameters(List<StringPair> parameters) {
        this.parameters = parameters;        
    }        
}
