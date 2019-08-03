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
package com.neotropic.inventory.modules.sync.nodes;

import com.neotropic.inventory.modules.sync.nodes.actions.DeleteSyncAction;
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
import com.neotropic.inventory.modules.sync.LocalSyncDataSourceConfiguration;
import com.neotropic.inventory.modules.sync.nodes.actions.ReleaseSyncDataSourceConfigurationAction;
import com.neotropic.inventory.modules.sync.nodes.actions.RunSynchronizationProcessAction;
import com.neotropic.inventory.modules.sync.nodes.properties.DevicePropertyReadEditor;
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
 * Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class SyncDataSourceConfigurationNode extends AbstractNode implements PropertyChangeListener {
    private static final Image NODE_ICON = ImageUtilities.loadImage("com/neotropic/inventory/modules/sync/res/sync_config.png");

    public SyncDataSourceConfigurationNode(LocalSyncDataSourceConfiguration syncConfig) {
        super(Children.LEAF, Lookups.singleton(syncConfig));
    }

    @Override
    public Image getOpenedIcon(int type) {
        return NODE_ICON;
    }

    @Override
    public Image getIcon(int type) {
        return NODE_ICON;
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
        Set snmpVersion3GeneralPropertySet = Sheet.createPropertiesSet(); // SNMP Version 3 attributes category
        Set sshGeneralPropertySet = Sheet.createPropertiesSet(); // Set of properties used in SSH connections
        
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
        
        /*SSH Properties*/
        PropertySupport.ReadWrite propertySshPort = new SyncConfigurationNativeTypeProperty("sshPort", String.class, "ssh Port", "ssh Port", this, 
            !parameters.containsKey("sshPort") ? null : parameters.get("sshPort"));
        PropertySupport.ReadWrite propertySshUser = new SyncConfigurationNativeTypeProperty("sshUser", String.class, "ssh User", "ssh User", this, 
            !parameters.containsKey("sshUser") ? null : parameters.get("sshUser"));
        PropertySupport.ReadWrite propertySshPassword = new SyncConfigurationNativeTypeProperty("sshPassword", String.class, "ssh Password", "ssh Password", this, 
            !parameters.containsKey("sshPassword") ? null : parameters.get("sshPassword"));
        /*End SSH Properties*/
        
        String deviceId = parameters.containsKey("deviceId") ? parameters.get("deviceId") : null;
        String deviceClass = parameters.containsKey("deviceClass") ? parameters.get("deviceClass") : null;
        LocalObjectLight deviceObj = CommunicationsStub.getInstance().getObjectInfoLight(deviceClass, deviceId);
        
        PropertySupport.ReadOnly propertyDeviceId = new DevicePropertyReadEditor("deviceId", String.class, "deviceId", "deviceId", deviceId);
        PropertySupport.ReadOnly propertyDevice = new DevicePropertyReadEditor("device", String.class, "device", "device", deviceObj.toString());

        
        //new DeviceTypeProperty(this, NoneObject.getInstance(), propertyDeviceId);
        
        generalPropertySet.put(propertyName);
//        if (deviceClass != null && deviceId != null) {
//            LocalObjectLight deviceObj = CommunicationsStub.getInstance().getObjectInfoLight(deviceClass, deviceId);
//            if (deviceObj != null) {
//                try {
//                    propertyDevice.setValue(deviceObj);
//                    propertyDeviceId.setValue(parameters.get("deviceId"));
//                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
//                }
//            }
//        }
        generalPropertySet.put(propertyDevice);
        generalPropertySet.put(propertyDeviceId);
        generalPropertySet.put(propertyIpAddress);
        generalPropertySet.put(propertyPort);
        generalPropertySet.setName(I18N.gm("general_properties"));
        generalPropertySet.setDisplayName(I18N.gm("general_properties"));
        
        snmpVersion2cPropertySet.put(propertyVersion);
        snmpVersion2cPropertySet.put(propertyCommunity);
        snmpVersion2cPropertySet.setName(I18N.gm("snmp_version_2c_properties"));
        snmpVersion2cPropertySet.setDisplayName(I18N.gm("snmp_version_2c_properties"));
        
        snmpVersion3GeneralPropertySet.put(propertyAuthenticationProtocol);
        snmpVersion3GeneralPropertySet.put(propertyAuthenticationProtocolPassPhrase);
        snmpVersion3GeneralPropertySet.put(propertySecurityLevel);
        snmpVersion3GeneralPropertySet.put(propertyContextName);
        snmpVersion3GeneralPropertySet.put(propertySecurityName);
        snmpVersion3GeneralPropertySet.put(propertyPrivacyProtocol);
        snmpVersion3GeneralPropertySet.put(propertyPrivacyProtocolPassPhrase);
        snmpVersion3GeneralPropertySet.setName(I18N.gm("snmp_version_3_properties"));
        snmpVersion3GeneralPropertySet.setDisplayName(I18N.gm("snmp_version_3_properties"));
        
        sshGeneralPropertySet.put(propertySshPort);
        sshGeneralPropertySet.put(propertySshUser);
        sshGeneralPropertySet.put(propertySshPassword);
        sshGeneralPropertySet.setName(I18N.gm("ssh_properties"));
        sshGeneralPropertySet.setDisplayName(I18N.gm("ssh_properties"));
        
        sheet.put(generalPropertySet);
        sheet.put(snmpVersion2cPropertySet);
        sheet.put(snmpVersion3GeneralPropertySet);
        sheet.put(sshGeneralPropertySet);
        
        return sheet;        
    }
    
    @Override
    public Action[] getActions(boolean context) {
        Action copyAction = SystemAction.get(CopyAction.class);
        copyAction.putValue(Action.NAME, I18N.gm("lbl_copy_action"));

        Action cutAction = SystemAction.get(CutAction.class);
        cutAction.putValue(Action.NAME, I18N.gm("lbl_cut_action"));

        return new Action[] {
            RunSynchronizationProcessAction.getInstance(),
            null, 
            copyAction, 
            cutAction,
            null, 
            ReleaseSyncDataSourceConfigurationAction.getInstance(),
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
