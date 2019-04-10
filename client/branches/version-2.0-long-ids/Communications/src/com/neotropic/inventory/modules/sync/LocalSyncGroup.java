/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.inventory.modules.sync;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;

/**
 * This class represent a Sync  Group
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class LocalSyncGroup implements Transferable, Comparable<LocalSyncGroup> {
    public static DataFlavor DATA_FLAVOR = new DataFlavor(LocalSyncGroup.class, "Object/LocalSyncGroup");
    
    private long id;
    private String name;
    private LocalSyncProvider provider;
    private List<LocalSyncDataSourceConfiguration> dataSourceConfig;

    public LocalSyncGroup(long id, String name) {
        this.id = id;
        this.name = name;
    }
    
    public LocalSyncGroup(long id, String name, LocalSyncProvider provider) {
        this.id = id;
        this.name = name;
        this.provider = provider;
    }

    public LocalSyncGroup(long id, String name, List<LocalSyncDataSourceConfiguration> dataSourceConfig) {
        this.id = id;
        this.name = name;
        this.dataSourceConfig = dataSourceConfig;
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

    public LocalSyncProvider getProvider() {
        return provider;
    }

    public void setProvider(LocalSyncProvider provider) {
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
        return getName();
    }
        
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        
        return obj instanceof LocalSyncGroup && this.getId() == ((LocalSyncGroup) obj).getId();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{DATA_FLAVOR};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor == DATA_FLAVOR;
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if(flavor == DATA_FLAVOR)
            return this;
        else
            throw new UnsupportedFlavorException(flavor);
    }    
}
