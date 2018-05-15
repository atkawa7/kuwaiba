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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.inventory.communications.util.Constants;
import org.inventory.communications.wsclient.RemoteScriptQuery;
import org.inventory.communications.wsclient.StringPair;

/**
 * A local representation of a Script Query
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class LocalScriptQuery implements Comparable<LocalScriptQuery> {
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
    /**
     * List of listeners of this ScriptQuery's properties
     */
    private List<VetoableChangeListener> changeListeners;
    
    public LocalScriptQuery() {
    }
    
    public LocalScriptQuery(RemoteScriptQuery remoteScriptQuery) {
        if (remoteScriptQuery != null) {
            this.id = remoteScriptQuery.getId();
            this.name = remoteScriptQuery.getName();
            this.description = remoteScriptQuery.getDescription();
            this.script = remoteScriptQuery.getScript();

            if (remoteScriptQuery.getParameters() != null) {
                parameters = new HashMap();
                
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
        String oldName = this.name;
        try {
            firePropertyChange(Constants.PROPERTY_NAME, oldName, name);
            this.name = name;
        } catch (PropertyVetoException ex) {}
    }
    
    public String getDescription() {
        return description;        
    }
    
    public void setDescription(String description) {
        String oldDescription = this.description;
        try {
            firePropertyChange(Constants.PROPERTY_DESCRIPTION, oldDescription, description);
            this.description = description;
        } catch (PropertyVetoException ex) {}
    }
    
    public String getScript() {
        return script;        
    }
    
    public void setScript(String script) {
        String oldScript = this.script;        
        try {
            firePropertyChange(Constants.PROPERTY_SCRIPT, oldScript, script);
            this.script = script;
        } catch (PropertyVetoException ex) {}       
    }
    
    public HashMap<String, String> getParameters() {
        return parameters;
    }
    
    public void setParameter(String parameterName, String parameterValue) {
        String oldParameterValue = this.parameters.get(parameterName);
        try {
            firePropertyChange(parameterName, oldParameterValue, parameterValue);
            this.parameters.put(parameterName, parameterValue);
        } catch (PropertyVetoException ex) {}
    }    
    
    public void setParameters(HashMap<String, String> parameters) {
        this.parameters = parameters;        
    }

    @Override
    public int compareTo(LocalScriptQuery o) {
        
        if (name == null)
            return -1;
        
        return name.compareTo(o.getName());
    }
    
    public void firePropertyChange(String propertyName, Object oldValue, Object newValue) throws PropertyVetoException {
        for (VetoableChangeListener changeListener : changeListeners)
            changeListener.vetoableChange(new PropertyChangeEvent(this, propertyName, oldValue, newValue));
    }
        
    public void addChangeListener(VetoableChangeListener listener) {
        if (changeListeners == null)
            changeListeners = new ArrayList();
        
        changeListeners.add(listener);
    }
    
    public void removeChangeListener(VetoableChangeListener listener) {
        if (changeListeners != null)
            changeListeners.remove(listener);
    }
    
    public void resetChangeListeners() {
        if (changeListeners != null)
            changeListeners.clear();
    }
}
