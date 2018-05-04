/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.kuwaiba.interfaces.ws.toserialize.application;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * Wrapper of ProcessDefinition. A process definition is the metadata of a process. It contains the set of 
 * activities, conditionals and the flow that connects everything
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteProcessDefinition implements Serializable {
    /**
    * The name of this process definition (e.g. Provisioning, Service Audit, etc)
    */
    private String name;
    /**
    * Process definition description
    */
    private String description;
    /**
    * Should instances of this process be permitted?
    */
    private boolean enabled;
    /**
    * Version of this process definition. Versions are important because if a user changes a definition, the instances 
    */
    private String version;
    /**
    * An XML file containing the actual structure of the process. See the Wiki for details about the format
    */
    private byte[] structure;

    public RemoteProcessDefinition() { }

    public RemoteProcessDefinition(String name, String description, boolean enabled, String version, byte[] structure) {
        this.name = name;
        this.description = description;
        this.enabled = enabled;
        this.version = version;
        this.structure = structure;
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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public byte[] getStructure() {
        return structure;
    }

    public void setStructure(byte[] structure) {
        this.structure = structure;
    }
}
