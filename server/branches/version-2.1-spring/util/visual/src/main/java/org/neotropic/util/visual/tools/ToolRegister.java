/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.neotropic.util.visual.tools;

import java.util.HashMap;
import java.util.List;

/**
 * Registries tools
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public interface ToolRegister {
    /**
     * Adds a tool
     * @param tool tool to add
     */
    default void addTool(Tool tool) {
        if (getTools() != null)
            getTools().add(tool);
    }
    /**
     * Removes a tool
     * @param tool tool to remove
     */
    default void removeTool(Tool tool) {
        if (getTools() != null)
            getTools().remove(tool);
    }
    /**
     * Remove all tools
     */
    default void removeAllTools() {
        if (getTools() != null)
            getTools().clear();
    }
    /**
     * Gets the tools registered
     * @return list of tools
     */
    List<Tool> getTools();
    /**
     * Sets the active tool.
     * @param tool the active tool
     */
    void setTool(Tool tool);
    
    default void fireEvent(ToolRegisterEvent event) {
        if (getListeners() != null)
            getListeners().forEach(listener -> listener.onToolRegisterEvent(event));
    }
    
    default void addListener(ToolRegisterListener listener) {
        if (getListeners() != null)
            getListeners().add(listener);
    }
    
    default void removeListener(ToolRegisterListener listener) {
        if (getListeners() != null)
            getListeners().remove(listener);
    }
    
    default void removeAllListeners() {
        if (getListeners() != null)
            getListeners().clear();
    }
    
    List<ToolRegisterListener> getListeners();
    
    public interface ToolRegisterListener {
        void onToolRegisterEvent(ToolRegisterEvent event);
    }
    
    public class ToolRegisterEvent {
        private String id;
        private HashMap<String, Object> properties;
        
        public ToolRegisterEvent() {
        }
        
        public ToolRegisterEvent(String id, HashMap<String, Object> properties) {
            this.id = id;
            this.properties = properties;
        }
        public String getId() {
            return id;
        }
        public HashMap<String, Object> getProperties() {
            return properties;
        }
    }
}
