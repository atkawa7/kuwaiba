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

import com.neotropic.inventory.modules.sync.nodes.actions.DeleteSyncAction;
import com.neotropic.inventory.modules.sync.nodes.properties.DeviceTypeProperty;
import com.neotropic.inventory.modules.sync.nodes.properties.NoneObject;
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
        final String none = "None";
        Sheet sheet = Sheet.createDefault();
        Set generalPropertySet = Sheet.createPropertiesSet(); // General attributes category
        Set snmpVersion2cPropertySet = Sheet.createPropertiesSet(); // SNMP Version 2c attributes category
        Set snmpVersion3generalPropertySet = Sheet.createPropertiesSet(); // SNMP Version 3 attributes category
        
        LocalSyncDataSourceConfiguration localsyncDataSrcConfig = getLookup().lookup(LocalSyncDataSourceConfiguration.class);
                
        HashMap<String, String> parameters = localsyncDataSrcConfig.getParameters(); 
        
        PropertySupport.ReadWrite propertyName = new SyncConfigurationNativeTypeProperty(Constants.PROPERTY_NAME, String.class, Constants.PROPERTY_NAME, Constants.PROPERTY_NAME, this, localsyncDataSrcConfig.getName());
                                
        PropertySupport.ReadWrite propertyIpAddress = new SyncConfigurationNativeTypeProperty("ipAddress", String.class, "ipAddress", "ipAddress", this, 
            !parameters.containsKey("ipAddress") ? null : parameters.get("ipAddress"));
        
        PropertySupport.ReadWrite propertyPort = new SyncConfigurationNativeTypeProperty("port", String.class, "port", "port", this, 
            !parameters.containsKey("port") ? null : parameters.get("port"));
        
        PropertySupport.ReadWrite propertyVersion = new SyncConfigurationNativeTypeProperty(Constants.PROPERTY_VERSION, String.class, Constants.PROPERTY_VERSION, I18N.gm("snmp_version"), this, 
            !parameters.containsKey(Constants.PROPERTY_VERSION) ? none : parameters.get(Constants.PROPERTY_VERSION));
                        
        PropertySupport.ReadWrite propertyCommunity = new SyncConfigurationNativeTypeProperty(Constants.PROPERTY_COMMUNITY, String.class, Constants.PROPERTY_COMMUNITY, I18N.gm("community"), this, 
            !parameters.containsKey(Constants.PROPERTY_COMMUNITY) ? null : parameters.get(Constants.PROPERTY_COMMUNITY));
                
        PropertySupport.ReadWrite propertyAuthenticationProtocol = new SyncConfigurationNativeTypeProperty(Constants.PROPERTY_AUTH_PROTOCOL, String.class, Constants.PROPERTY_AUTH_PROTOCOL, I18N.gm("snmp_version_3_auth_protocol"), this, 
            !parameters.containsKey(Constants.PROPERTY_AUTH_PROTOCOL) ? none : parameters.get(Constants.PROPERTY_AUTH_PROTOCOL));
        
        PropertySupport.ReadWrite propertyAuthenticationProtocolPassPhrase = new SyncConfigurationNativeTypeProperty(Constants.PROPERTY_AUTH_PASS, String.class, Constants.PROPERTY_AUTH_PASS, I18N.gm("snmp_version_3_auth_pass"), this, 
            !parameters.containsKey(Constants.PROPERTY_AUTH_PASS) ? null : parameters.get(Constants.PROPERTY_AUTH_PASS));
        
        PropertySupport.ReadWrite propertySecurityLevel = new SyncConfigurationNativeTypeProperty(Constants.PROPERTY_SECURITY_LEVEL, String.class, Constants.PROPERTY_SECURITY_LEVEL, I18N.gm("snmp_version_3_security_level"), this, 
            !parameters.containsKey(Constants.PROPERTY_SECURITY_LEVEL) ? none : parameters.get(Constants.PROPERTY_SECURITY_LEVEL));
        
        PropertySupport.ReadWrite propertyContextName = new SyncConfigurationNativeTypeProperty(Constants.PROPERTY_CONTEXT_NAME, String.class, Constants.PROPERTY_CONTEXT_NAME, I18N.gm("snmp_version_3_context_name"), this, 
            !parameters.containsKey(Constants.PROPERTY_CONTEXT_NAME) ? null : parameters.get(Constants.PROPERTY_CONTEXT_NAME));
        
        PropertySupport.ReadWrite propertySecurityName = new SyncConfigurationNativeTypeProperty(Constants.PROPERTY_SECURITY_NAME, String.class, Constants.PROPERTY_SECURITY_NAME, I18N.gm("snmp_version_3_security_name"), this, 
            !parameters.containsKey(Constants.PROPERTY_SECURITY_NAME) ? null : parameters.get(Constants.PROPERTY_SECURITY_NAME));
        
        PropertySupport.ReadWrite propertyPrivacyProtocol = new SyncConfigurationNativeTypeProperty(Constants.PROPERTY_PRIVACY_PROTOCOL, String.class, Constants.PROPERTY_PRIVACY_PROTOCOL, I18N.gm("snmp_version_3_privacy_protocol"), this, 
            !parameters.containsKey(Constants.PROPERTY_PRIVACY_PROTOCOL) ? none : parameters.get(Constants.PROPERTY_PRIVACY_PROTOCOL));
        
        PropertySupport.ReadWrite propertyPrivacyProtocolPassPhrase = new SyncConfigurationNativeTypeProperty(Constants.PROPERTY_PRIVACY_PASS, String.class, Constants.PROPERTY_PRIVACY_PASS, I18N.gm("snmp_version_3_privacy_pass"), this, 
            !parameters.containsKey(Constants.PROPERTY_PRIVACY_PASS) ? null : parameters.get(Constants.PROPERTY_PRIVACY_PASS));
                
        Long deviceId = parameters.containsKey("deviceId") ? Long.valueOf(parameters.get("deviceId")) : null;
        String deviceClass = parameters.containsKey("deviceClass") ? parameters.get("deviceClass") : null;
        
        PropertySupport.ReadWrite propertyDeviceId = new SyncConfigurationNativeTypeProperty("deviceId", String.class, "deviceId", "deviceId", this, null);
        PropertySupport.ReadWrite propertyDevice = new DeviceTypeProperty(this, NoneObject.getInstance(), propertyDeviceId);
        
        generalPropertySet.put(propertyName);
        if (deviceClass != null && deviceId != null) {
            LocalObjectLight deviceObj = CommunicationsStub.getInstance().getObjectInfoLight(deviceClass, deviceId);
            if (deviceObj != null) {
                try {
                    propertyDevice.setValue(deviceObj);
                    propertyDeviceId.setValue(parameters.get("deviceId"));
                } catch (Exception ex) {
                }
            }
        }
        generalPropertySet.put(propertyDevice);
        generalPropertySet.put(propertyDeviceId);
        
        generalPropertySet.put(propertyIpAddress);
        generalPropertySet.put(propertyPort);
        generalPropertySet.put(propertyVersion);
        
        snmpVersion2cPropertySet.put(propertyCommunity);
        
        snmpVersion3generalPropertySet.put(propertyAuthenticationProtocol);
        snmpVersion3generalPropertySet.put(propertyAuthenticationProtocolPassPhrase);
        snmpVersion3generalPropertySet.put(propertySecurityLevel);
        snmpVersion3generalPropertySet.put(propertyContextName);
        snmpVersion3generalPropertySet.put(propertySecurityName);
        snmpVersion3generalPropertySet.put(propertyPrivacyProtocol);
        snmpVersion3generalPropertySet.put(propertyPrivacyProtocolPassPhrase);
        
        generalPropertySet.setName(I18N.gm("general_information"));
        generalPropertySet.setDisplayName(I18N.gm("general_attributes"));
        
        snmpVersion2cPropertySet.setName(I18N.gm("snmp_version_2c_info"));
        snmpVersion2cPropertySet.setDisplayName(I18N.gm("snmp_version_2c_attr"));
        
        snmpVersion3generalPropertySet.setName(I18N.gm("snmp_version_3_info"));
        snmpVersion3generalPropertySet.setDisplayName(I18N.gm("snmp_version_3_attr"));
        
        sheet.put(generalPropertySet);
        sheet.put(snmpVersion2cPropertySet);
        sheet.put(snmpVersion3generalPropertySet);
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
            DeleteSyncAction.getInstance()};
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
