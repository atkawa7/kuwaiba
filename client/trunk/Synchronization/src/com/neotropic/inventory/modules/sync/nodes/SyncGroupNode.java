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

import com.neotropic.inventory.modules.sync.actions.NewSyncDataSourceConfigurationAction;
import java.util.Collections;
import org.openide.util.ImageUtilities;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Objects;
import javax.swing.Action;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalSyncGroup;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.lookup.Lookups;

/**
 * Represents a Sync Group
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class SyncGroupNode extends AbstractNode implements PropertyChangeListener {
    public static final String ICON_PATH = "com/neotropic/inventory/modules/sync/res/icon.png";
    private static final Image icon = ImageUtilities.loadImage(ICON_PATH);
    
    private LocalSyncGroup localSyncGroup;
    protected Sheet sheet;
    
    public SyncGroupNode(LocalSyncGroup localSyncGroup) {
        super(new SyncGroupChildren(), Lookups.singleton(localSyncGroup));
        this.localSyncGroup = localSyncGroup;
//TODO HERE!               
//        if (localSyncGroup.getName() != null)
//            localSyncGroup.addPropertyChangeListener(WeakListeners.propertyChange(this, localSyncGroup));
    }
    
    @Override
    public void setName(String newName) {
        if (newName != null) {
            if (CommunicationsStub.getInstance().updateFavoritesFolder(localSyncGroup.getId(), newName)) {
                localSyncGroup.setName(newName);
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
        return localSyncGroup.getName();
    }
    
    @Override
    public String getDisplayName() {
        return localSyncGroup.toString();
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {NewSyncDataSourceConfigurationAction.getInstance()};
    }
    
    public LocalSyncGroup getLocalSyncGroup() {
        return localSyncGroup;
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
        sheet = Sheet.createDefault();
//        Set generalPropertySet = Sheet.createPropertiesSet(); // General attributes category
//        LocalFavoritesFolder lb = CommunicationsStub.getInstance().getFavoritesFolder(localFavoritesFolder.getId());
//        if (lb == null) {
//            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
//            return sheet;
//        }
//        localFavoritesFolder.setName(lb.getName());
//                
//        PropertySupport.ReadWrite propertyName = new FavoritesFolderNativeTypeProperty(
//                Constants.PROPERTY_NAME, String.class, Constants.PROPERTY_NAME, 
//                Constants.PROPERTY_NAME, this, lb.getName());
//        generalPropertySet.put(propertyName);
//        
//        generalPropertySet.setName(I18N.gm("general_information"));
//        generalPropertySet.setDisplayName(I18N.gm("general_attributes"));
//        sheet.put(generalPropertySet);
        return sheet;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SyncGroupNode) {
            return ((SyncGroupNode) obj).getLocalSyncGroup().equals(getLocalSyncGroup());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.localSyncGroup);
        return hash;
    }
    
    @Override
    public boolean canRename() {
        return true;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource().equals(localSyncGroup)) {
            localSyncGroup = (LocalSyncGroup) evt.getSource();
            if (evt.getPropertyName().equals(Constants.PROPERTY_NAME)) {                
                setDisplayName(getDisplayName());
                fireNameChange(null, localSyncGroup.getName());
            }
        }
    }
    
    public static class SyncGroupChildren extends Children.Keys<LocalObjectLight> {
        
        @Override
        public void addNotify() {
            SyncGroupNode selectedNode = (SyncGroupNode) getNode();
            
            List<LocalObjectLight> commDevices = CommunicationsStub.getInstance().getObjectsInFavoritesFolder(selectedNode.getLocalSyncGroup().getId(), -1);
            
            if (commDevices == null) {
                setKeys(Collections.EMPTY_LIST);
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, 
                    CommunicationsStub.getInstance().getError());
            } else {
                Collections.sort(commDevices);
                setKeys(commDevices);
            }
        }
        
        @Override
        public void removeNotify() {
            setKeys(Collections.EMPTY_LIST);
        }
        
        @Override
        protected Node[] createNodes(LocalObjectLight key) {
            return new Node [] { new ObjectNode(key) };
        }
    }
}
