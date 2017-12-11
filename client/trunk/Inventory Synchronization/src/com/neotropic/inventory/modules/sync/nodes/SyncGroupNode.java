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

import com.neotropic.inventory.modules.sync.nodes.actions.SyncManagerActionFactory;
import com.neotropic.inventory.modules.sync.nodes.properties.SyncGroupNativeTypeProperty;
import java.util.Collections;
import org.openide.util.ImageUtilities;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import javax.swing.Action;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalSyncDataSourceConfiguration;
import org.inventory.communications.core.LocalSyncGroup;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.nodes.Sheet.Set;
import org.openide.util.lookup.Lookups;

/**
 * Node representing a sync group
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class SyncGroupNode extends AbstractNode implements PropertyChangeListener {
    private static final Image icon = ImageUtilities.loadImage("com/neotropic/inventory/modules/sync/res/sync_group.png");
        
    public SyncGroupNode(LocalSyncGroup localSyncGroup) {
        super(new SyncGroupNodeChildren(), Lookups.singleton(localSyncGroup));
    }
    
    @Override
    public void setName(String newName) {
        if (newName != null) {
            if (CommunicationsStub.getInstance().updateFavoritesFolder(getLookup().lookup(LocalSyncGroup.class).getId(), newName)) {
                getLookup().lookup(LocalSyncGroup.class).setName(newName);
                if (getSheet() != null)
                    setSheet(createSheet());
            } else {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                        NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());                
            }
        }
    }
    
    @Override
    public String getName(){
        return getLookup().lookup(LocalSyncGroup.class).getName();
    }
    
    @Override
    public String getDisplayName() {
        return getLookup().lookup(LocalSyncGroup.class).toString();
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action[] { SyncManagerActionFactory.getNewSyncDataSourceConfigurationAction(), null, 
                                SyncManagerActionFactory.getNewRunSynchronizationProcessAction(), null, 
                                SyncManagerActionFactory.getDeleteSyncGroupAction()};
    }
        
    @Override
    public Image getIcon(int i) {
        return icon;
    }
    
    @Override
    public Image getOpenedIcon(int i) {
        return getIcon(i);
    }
    
    @Override
    protected Sheet createSheet () {
        Sheet sheet = Sheet.createDefault();
        Set generalPropertySet = Sheet.createPropertiesSet(); // General attributes category
        
        LocalSyncGroup localSyncGroup = getLookup().lookup(LocalSyncGroup.class);                
        
        PropertySupport.ReadWrite propertyName = new SyncGroupNativeTypeProperty(Constants.PROPERTY_NAME, String.class, Constants.PROPERTY_NAME, Constants.PROPERTY_NAME, this, localSyncGroup.getName());
        PropertySupport.ReadWrite propertySyncProvider = new SyncGroupNativeTypeProperty("syncProvider", String.class, I18N.gm("sync_provider"), "", this, localSyncGroup.getProvider());
                
        generalPropertySet.put(propertyName);
        generalPropertySet.put(propertySyncProvider);
        
        generalPropertySet.setName(I18N.gm("general_information"));
        generalPropertySet.setDisplayName(I18N.gm("general_attributes"));
        
        sheet.put(generalPropertySet);
        return sheet;    
    }
    
    @Override
    public boolean equals(Object obj) {
        return obj instanceof SyncGroupNode && 
                ((SyncGroupNode) obj).getLookup().lookup(LocalSyncGroup.class).equals(getLookup().lookup(LocalSyncGroup.class));
        
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(getLookup().lookup(LocalSyncGroup.class));
        return hash;
    }
    
    @Override
    public boolean canRename() {
        return true;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource().equals(getLookup().lookup(LocalSyncGroup.class))) {
            if (evt.getPropertyName().equals(Constants.PROPERTY_NAME)) {                
                setDisplayName(getDisplayName());
                fireNameChange(null, getLookup().lookup(LocalSyncGroup.class).getName());
            }
        }
    }
    
    public static class SyncGroupNodeChildren extends Children.Keys<LocalSyncDataSourceConfiguration> {
        
        @Override
        public void addNotify() {
            LocalSyncGroup selectedSyncGroup = ((SyncGroupNode) getNode()).getLookup().lookup(LocalSyncGroup.class);
            List<LocalSyncDataSourceConfiguration> dataSourceConfigurations = CommunicationsStub.getInstance().
                    getSyncDataSourceConfigurations(selectedSyncGroup.getId());
            
            if (dataSourceConfigurations == null) {
                setKeys(Collections.EMPTY_LIST);
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, 
                    CommunicationsStub.getInstance().getError());
            } else {
                Collections.sort(dataSourceConfigurations);
                setKeys(dataSourceConfigurations);
            }
        }
        
        @Override
        public void removeNotify() {
            setKeys(Collections.EMPTY_LIST);
        }
        
        @Override
        protected Node[] createNodes(LocalSyncDataSourceConfiguration key) {
            return new Node [] { new SyncConfigurationNode(key) };
        }
    }
}
