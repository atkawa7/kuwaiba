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

import java.util.List;

/**
 * Wraps the definition of a process. The activities are represented as a linked list
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ProcessDefinition {
    /**
     * Process id
     */
    private long id;
    /**
     * Process name
     */
    private String name;
    /**
     * Process description
     */
    private String description;
    /**
     * Process creation date
     */
    private long creationDate;
    /**
     * Process version, expressed as a three numeric sections separated by a dot (e.g. 1.3.1)
     */
    private String version;
    /**
     * If instances of the current process can be created or not
     */
    private boolean enabled;
    /**
     * Reference to the start activity (typically a TYPE_START type of activity). The rest will be linked from this one
     */
    private ActivityDefinition startActivity;
    /**
     * List of Key Performance Indicators to Process Definition
     */
    private List<Kpi> kpis;
    /**
     * List of Key Performance Indicator Actions to Process Definition
     */
    private List<KpiAction> kpiActions;
        
    public ProcessDefinition(long id, String name, String description, long creationDate, 
            String version, boolean enabled, ActivityDefinition startActivity, List<Kpi> kpis, List<KpiAction> kpiActions) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.creationDate = creationDate;
        this.version = version;
        this.enabled = enabled;
        this.startActivity = startActivity;
        this.kpis = kpis;
        this.kpiActions = kpiActions;
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

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public ActivityDefinition getStartActivity() {
        return startActivity;
    }

    public void setStartActivity(ActivityDefinition startActivity) {
        this.startActivity = startActivity;
    }
    
    public List<Kpi> getKpis() {
        return kpis;
    }
    
    public void setKpis(List<Kpi> kpis) {
        this.kpis = kpis;
    }
    
    public List<KpiAction> getKpiActions() {
        return kpiActions;
    }
    
    public void setKpiActions(List<KpiAction> kpiActions) {
        this.kpiActions = kpiActions;
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + (int) (this.id ^ (this.id >>> 32));
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
        final ProcessDefinition other = (ProcessDefinition) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }
        
}
