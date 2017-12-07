/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
 * 
 */
package org.inventory.communications.core;

import java.util.List;
import org.inventory.communications.util.Constants;

/**
 * This class represent a Sync  Group
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class LocalSyncGroup implements Comparable<LocalSyncGroup> {
    private long id;
    private String name;
    private String provider;
    private List<LocalSyncDataSourceConfiguration> dataSourceConfig;

    public LocalSyncGroup(long id, String name, String provider) {
        this.id = id;
        this.name = name;
        this.provider = provider;
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

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public List<LocalSyncDataSourceConfiguration> getDataSourceConfig() {
        return dataSourceConfig;
    }

    public void setDataSourceConfig(List<LocalSyncDataSourceConfiguration> dataSourceConfig) {
        this.dataSourceConfig = dataSourceConfig;
    }

    @Override
    public int compareTo(LocalSyncGroup o) {
        return getName().compareTo(o.getName());
    }

    @Override
    public String toString() {
        return getName() == null ? Constants.LABEL_NONAME : getName();
    }
        
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        
        if (!(obj instanceof LocalSyncGroup))
            return false;
        
        return this.getId() == ((LocalSyncGroup) obj).getId();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }
}
