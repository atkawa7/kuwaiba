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
package org.kuwaiba.apis.persistence.application.forms;

import java.io.Serializable;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class FormInstance implements Serializable {
    /**
     * Form Id
     */
    private Long id;
    /**
     * Form Name
     */
    private String name;
    /**
     * Form Description
     */
    private String description;
    /**
     * Form Structure
     */
    private byte [] structure;
    
    public FormInstance(Long id, String name, String description, byte[] structure) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.structure = structure;
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
    
    public byte[] getStructure() {
        return structure;
    }
    
    public void setStructure(byte[] structure) {
        this.structure = structure;        
    }
    
}
