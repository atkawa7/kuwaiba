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
import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.kuwaiba.apis.persistence.application.forms.FormInstance;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteFormInstance implements Serializable {
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
    
    public RemoteFormInstance() {
    }
    
    public RemoteFormInstance(Long id, String name, String description, byte[] structure) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.structure = structure;
    }
    
    public RemoteFormInstance(FormInstance formInstance) {
        this.id = formInstance.getId();
        this.name = formInstance.getName();
        this.description = formInstance.getDescription();
        this.structure = formInstance.getStructure();
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

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RemoteFormInstance other = (RemoteFormInstance) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
}