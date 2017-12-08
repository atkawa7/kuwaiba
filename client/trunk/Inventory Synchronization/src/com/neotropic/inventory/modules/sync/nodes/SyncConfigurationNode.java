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
package com.neotropic.inventory.modules.sync.nodes;

import java.awt.Image;
import org.inventory.communications.core.LocalSyncDataSourceConfiguration;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.Lookups;

/**
 * Represents a sync data source configuration object
 * Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class SyncConfigurationNode extends AbstractNode {
    private static final Image icon = ImageUtilities.loadImage("com/neotropic/inventory/modules/sync/res/sync_config.png");

    public SyncConfigurationNode(LocalSyncDataSourceConfiguration syncConfig) {
        super(Children.LEAF, Lookups.singleton(syncConfig));
    }
    
    @Override
    public Image getOpenedIcon(int type) {
        return icon;
    }

    @Override
    public Image getIcon(int type) {
        return icon;
    }
    
    @Override
    public String getName(){
        return getLookup().lookup(LocalSyncDataSourceConfiguration.class).getName();
    }
    
    @Override
    public String getDisplayName() {
        return getLookup().lookup(LocalSyncDataSourceConfiguration.class).toString();
    }
}
