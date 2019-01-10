/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.apis.persistence.application.process;

/**
 * The representation of a a running process
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ProcessInstance {
    /**
     * The id of the process
     */
    private long id;
    /**
     * The name of the process
     */
    private String name;
    /**
     * The description of the process
     */
    private String description;
    /**
     * The pointer to the current activity (that is, the las activity that was committed successfully). 
     * When a process was just created, the pointer will point to a dummy TYPE_START activity. When it's finished, will point to a dummy TYPE_END activity
     */
    private long currentActivity;
    /**
     * A reference to the process definition the current process instance was spawned from
     */
    private long processDefinition;
    /**
     * A XML Structure that storage the content of the artifacts to this process instance
     */
    private byte[] artifactsContent;
        
    public ProcessInstance(long id, String name, String description, long currentActivity, long processDefinition) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.currentActivity = currentActivity;
        this.processDefinition = processDefinition;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public long getCurrentActivity() {
        return currentActivity;
    }

    public void setCurrentActivity(long currentActivity) {
        this.currentActivity = currentActivity;
    }

    public long getProcessDefinition() {
        return processDefinition;
    }

    public void setProcessDefinition(long processDefinition) {
        this.processDefinition = processDefinition;
    }
    
    public void setArtifactsContent(byte[] artifactsContent) {
        this.artifactsContent = artifactsContent;
    }
    
    public byte[] getArtifactsContent() {
        return artifactsContent;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + (int) (this.id ^ (this.id >>> 32));
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
        final ProcessInstance other = (ProcessInstance) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }
        
}
