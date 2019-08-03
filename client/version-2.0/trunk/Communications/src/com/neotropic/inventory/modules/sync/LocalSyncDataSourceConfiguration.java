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
import java.util.HashMap;

/**
 * This class represent Sync data source configuration
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class LocalSyncDataSourceConfiguration implements Transferable, Comparable<LocalSyncDataSourceConfiguration> {
    public static DataFlavor DATA_FLAVOR = new DataFlavor(LocalSyncDataSourceConfiguration.class, "Object/LocalSyncDataSourceConfiguration");
    /**
     * Configuration id
     */
    private long id;
    /**
     * Configuration name
     */
    private String name;
    /**
     * The parameters stored in this configuration entry
     */
    private HashMap<String, String> parameters;

    public LocalSyncDataSourceConfiguration(long id, String name, HashMap<String, String> parameters) {
        this.id = id;
        this.name = name;
        this.parameters = parameters;
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

    public HashMap<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(HashMap<String, String> parameters) {
        this.parameters = parameters;
    }

    @Override
    public int compareTo(LocalSyncDataSourceConfiguration o) {
        return getName().compareTo(o.getName());
    }
    
    @Override
    public String toString() {
        return this.name;
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
