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
import com.neotropic.inventory.modules.sync.nodes.properties.SyncConfigurationNativeTypeProperty;
import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import javax.swing.Action;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalSyncDataSourceConfiguration;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.nodes.Sheet.Set;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 * Represents a sync data source configuration object
 * Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class SyncConfigurationNode extends AbstractNode implements PropertyChangeListener {
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
    public void setName(String newName){
        if (newName != null) {
            HashMap<String, String> attributes = new HashMap<>();
            attributes.put("name", newName);
            if (CommunicationsStub.getInstance().updateSyncDataSourceConfiguration(getLookup().lookup(LocalSyncDataSourceConfiguration.class).getId(), attributes)) {
                getLookup().lookup(LocalSyncDataSourceConfiguration.class).setName(newName);
                propertyChange(new PropertyChangeEvent(getLookup().lookup(LocalSyncDataSourceConfiguration.class), Constants.PROPERTY_NAME, "", newName));
                if (getSheet() != null)
                   setSheet(createSheet());
            } else {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                        NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());                
            }
        }
    }
            
    @Override
    public boolean canRename() {
        return true;
    }
    @Override
    public String getDisplayName() {
        return getLookup().lookup(LocalSyncDataSourceConfiguration.class).toString();
    }
    
    @Override
    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        Set generalPropertySet = Sheet.createPropertiesSet(); // General attributes category
        
        LocalSyncDataSourceConfiguration localsyncDataSrcConfig = getLookup().lookup(LocalSyncDataSourceConfiguration.class);
                
        HashMap<String, String> parameters = localsyncDataSrcConfig.getParameters(); 
        
        PropertySupport.ReadWrite propertyName = new SyncConfigurationNativeTypeProperty(Constants.PROPERTY_NAME, String.class, Constants.PROPERTY_NAME, Constants.PROPERTY_NAME, this, localsyncDataSrcConfig.getName());

        PropertySupport.ReadWrite propertyDeviceId = new SyncConfigurationNativeTypeProperty("deviceId", String.class, "deviceId", "deviceId", this, 
            !parameters.containsKey("deviceId") ? null : parameters.get("deviceId"));
        
        PropertySupport.ReadWrite propertyDeviceClass = new SyncConfigurationNativeTypeProperty("deviceClass", String.class, "deviceClass", "deviceClass", this, 
            !parameters.containsKey("deviceClass") ? null : parameters.get("deviceClass"));
                        
        PropertySupport.ReadWrite propertyIpAddress = new SyncConfigurationNativeTypeProperty("ipAddress", String.class, "ipAddress", "ipAddress", this, 
            !parameters.containsKey("ipAddress") ? null : parameters.get("ipAddress"));
        
        PropertySupport.ReadWrite propertyPort = new SyncConfigurationNativeTypeProperty("port", String.class, "port", "port", this, 
            !parameters.containsKey("port") ? null : parameters.get("port"));
        
        PropertySupport.ReadWrite propertyCommunity = new SyncConfigurationNativeTypeProperty("community", String.class, "community", "community", this, 
            !parameters.containsKey("community") ? null : parameters.get("community"));
        
        Long deviceId = parameters.containsKey("deviceId") ? Long.valueOf(parameters.get("deviceId")) : null;
        String deviceClass = parameters.containsKey("deviceClass") ? parameters.get("deviceClass") : null;
        
        generalPropertySet.put(propertyName);
        if (deviceClass != null && deviceId != null) {
            LocalObjectLight deviceObj = CommunicationsStub.getInstance().getObjectInfoLight(deviceClass, deviceId);
            if (deviceObj == null) {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                    NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                return null;
            }
            PropertySupport.ReadWrite propertyDeviceName = new SyncConfigurationNativeTypeProperty("deviceName", String.class, "deviceName", "deviceName", this, deviceObj.getName());
            generalPropertySet.put(propertyDeviceName);
        }
        generalPropertySet.put(propertyDeviceId);
        generalPropertySet.put(propertyDeviceClass);
        generalPropertySet.put(propertyIpAddress);
        generalPropertySet.put(propertyPort);
        generalPropertySet.put(propertyCommunity);
        
        generalPropertySet.setName(I18N.gm("general_information"));
        generalPropertySet.setDisplayName(I18N.gm("general_attributes"));
        
        sheet.put(generalPropertySet);
        return sheet;        
    }
    
    @Override
    public Action[] getActions(boolean context) {
        Action copyAction = SystemAction.get(CopyAction.class);
        copyAction.putValue(Action.NAME, I18N.gm("lbl_copy_action"));

        Action cutAction = SystemAction.get(CutAction.class);
        cutAction.putValue(Action.NAME, I18N.gm("lbl_cut_action"));
        
        return new Action[] {
            copyAction, 
            cutAction, 
            null, 
            SyncManagerActionFactory.getDeleteSyncDataSourceConfigurationAction()};
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource().equals(getLookup().lookup(LocalSyncDataSourceConfiguration.class))) {
            if (evt.getPropertyName().equals(Constants.PROPERTY_NAME)) {                
                setDisplayName(getDisplayName());
                fireNameChange(null, getLookup().lookup(LocalSyncDataSourceConfiguration.class).getName());
            }
        }
    }
    
    @Override
    public Transferable drag() throws IOException {        
        return getLookup().lookup(LocalSyncDataSourceConfiguration.class);
    }
    
    @Override
    public boolean canCut() {
        return true;
    }

    @Override
    public boolean canCopy() {
        return true;
    }

    @Override
    public boolean canDestroy() {
        return true;
    }
}
