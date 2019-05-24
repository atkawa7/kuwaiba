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

package org.kuwaiba.web.modules.sync.miniapps;

import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ItemCaptionGenerator;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import org.kuwaiba.apis.persistence.util.StringPair;
import org.kuwaiba.apis.web.gui.miniapps.AbstractMiniApplication;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSynchronizationConfiguration;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSynchronizationGroup;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;

/**
 * This mini application allows to configure a synchronization group, and add data source configurations to it. It's also possible that instead of creating a sync group from scratch, the user chooses an existing one
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ConfigureSyncGroupMiniApp extends AbstractMiniApplication<Window, Panel>{

    /**
     * Default constructor
     * @param inputParameters The class name and the id of the object that should be added to the sync group
     */
    public ConfigureSyncGroupMiniApp(Properties inputParameters) {
        super(inputParameters);
    }

    @Override
    public String getDescription() {
        return "This mini application allows to configure a synchronization group, "
             + "and add data source configurations to it. "
             + "It's also possible that instead of creating a sync group from scratch, "
             + "the user chooses an existing one";
    }

    @Override
    public Window launchDetached() {
        if (inputParameters == null)
            return null;
        final RemoteSession remoteSession = (RemoteSession) UI.getCurrent().getSession().getAttribute("session"); //NOI18N
        if (remoteSession == null)
            return null;
        final String DEVICE_ID = "deviceId"; //NOI18N
        final String DEVICE_CLASS = "deviceClass"; //NOI18N
        
        if (!inputParameters.containsKey(DEVICE_ID)) {
            Notifications.showError("Missing input parameter deviceId");
            return null;
        }
        if (!inputParameters.containsKey(DEVICE_CLASS)) {
            Notifications.showError("Missing input parameter deviceClass");
            return null;
        }
        String deviceId = inputParameters.getProperty(DEVICE_ID);
        String deviceClass = inputParameters.getProperty(DEVICE_CLASS);
                
        try {
            final RemoteObjectLight device = wsBean.getObjectLight(deviceClass, deviceId, 
                remoteSession.getIpAddress(), 
                remoteSession.getSessionId());
            if (device == null)            
                return null;
            
            List<RemoteSynchronizationGroup> syncGroups = wsBean.getSynchronizationGroups(
                remoteSession.getIpAddress(), 
                remoteSession.getSessionId());
            if (syncGroups == null)
                return null;
            
            Properties parameters = new Properties();
            final String NAME = "name"; //NOI18N
            final String IP_ADDRESS = "ipAddress"; //NOI18N
            final String PORT = "port"; //NOI18N
            final String VERSION = "version"; //NOI18N
            final String COMMUNITY = "community"; //NOI18N
            final String AUTH_PROTOCOL = "authProtocol"; //NOI18N
            final String AUTH_PASS = "authPass"; //NOI18N
            final String SECURITY_LEVEL = "securityLevel"; //NOI18N
            final String CONTEXT_NAME = "contextName"; //NOI18N
            final String SECURITY_NAME = "securityName"; //NOI18N
            final String PRIVACY_PROTOCOL = "privacyProtocol"; //NOI18N
            final String PRIVACY_PASS = "privacyPass"; //NOI18N
            final String SSH_PORT = "sshPort"; //NOI18N
            final String SSH_USER = "sshUser"; //NOI18N
            final String SSH_PASSWORD = "sshPassword"; //NOI18N
            
            RemoteSynchronizationConfiguration syncConfig = null;
            
            try {
                syncConfig = wsBean.getSyncDataSourceConfiguration(
                    device.getId(), 
                    remoteSession.getIpAddress(), 
                    remoteSession.getSessionId());
                
                if (syncConfig != null && syncConfig.getParameters() != null) {
                    if (syncConfig.getName() != null)
                        parameters.setProperty(NAME, syncConfig.getName());
                                        
                    for (StringPair parameter : syncConfig.getParameters()) {
                        if (parameter.getKey() != null && parameter.getValue() != null)
                            parameters.setProperty(parameter.getKey(), parameter.getValue());
                    }                    
                }
            } catch (ServerSideException ex) {
            }
            List<RemoteSynchronizationGroup> itemSyncGroups;
            
            if (syncConfig != null) {
                
                itemSyncGroups = new ArrayList();
                
                for (RemoteSynchronizationGroup syncGroup : syncGroups) {

                    List<RemoteSynchronizationConfiguration> dataSourceConfigs = wsBean.getSyncDataSourceConfigurations(
                       syncGroup.getId(), 
                       remoteSession.getIpAddress(), 
                       remoteSession.getSessionId());

                    if (dataSourceConfigs == null)
                        return null;
                    
                    boolean hasDataSourceConfig = false;

                    for (RemoteSynchronizationConfiguration dataSourceConfig : dataSourceConfigs) {
                        if (syncConfig.getId() == dataSourceConfig.getId()) {
                            hasDataSourceConfig = true;
                            break;                                                        
                        }
                    }
                    if (!hasDataSourceConfig)
                        itemSyncGroups.add(syncGroup);
                }
            } else {
                itemSyncGroups = syncGroups;
            }
            Window window = new Window();
            window.setCaption("Synchronization Data Source Configuration");
            window.setModal(true);
            window.setClosable(false);
            window.setResizable(false);
            window.center();
            window.setWidth(50, Unit.PERCENTAGE);
            window.setHeight(80, Unit.PERCENTAGE);

            VerticalLayout mainLayout = new VerticalLayout();
            mainLayout.setSizeFull();

            Panel pnlContent = new Panel();
            pnlContent.setSizeFull();
            pnlContent.addStyleName(ValoTheme.PANEL_BORDERLESS);

            VerticalLayout vltContent = new VerticalLayout();
            vltContent.setWidth(100, Unit.PERCENTAGE);
            vltContent.setHeightUndefined();

            GridLayout gltContent = new GridLayout();

            gltContent.setWidth(80, Unit.PERCENTAGE);
            gltContent.setRows(21);
            gltContent.setColumns(2);
            gltContent.setSpacing(true);

            gltContent.setColumnExpandRatio(0, 1f);
            gltContent.setColumnExpandRatio(1, 9f);

            int width = 100;
            
            final HashMap<String, AbstractComponent> components = new HashMap();
            
            final ComboBox<RemoteSynchronizationGroup> cbmSynGroup = new ComboBox();
            cbmSynGroup.setItems(itemSyncGroups);
            cbmSynGroup.setWidth(width, Unit.PERCENTAGE);
            cbmSynGroup.setItemCaptionGenerator(new ItemCaptionGenerator<RemoteSynchronizationGroup>() {
                
                @Override
                public String apply(RemoteSynchronizationGroup item) {
                    return item.getName();
                }
            });

            TextField txtName = new TextField();
            components.put(NAME, txtName);
            if (parameters.containsKey(NAME))
                txtName.setValue(parameters.getProperty(NAME));
            txtName.setWidth(width, Unit.PERCENTAGE);

            TextField txtIpAddress = new TextField();
            components.put(IP_ADDRESS, txtIpAddress);
            if (parameters.containsKey(IP_ADDRESS))
                txtIpAddress.setValue(parameters.getProperty(IP_ADDRESS));
            txtIpAddress.setWidth(width, Unit.PERCENTAGE);

            TextField txtPort = new TextField();
            components.put(PORT, txtPort);
            if (parameters.containsKey(PORT))
                txtPort.setValue(parameters.getProperty(PORT));
            txtPort.setWidth(width, Unit.PERCENTAGE);

            ComboBox<String> cmbVersion = new ComboBox<>();
            components.put(VERSION, cmbVersion);
            cmbVersion.setValue(parameters.getProperty(VERSION));
            cmbVersion.setWidth(width, Unit.PERCENTAGE);
            cmbVersion.setItems(Arrays.asList("3", "2c")); //NOI18N
            
            TextField txtCommunity = new TextField();
            components.put(COMMUNITY, txtCommunity);
            if (parameters.containsKey(COMMUNITY))
                txtCommunity.setValue(parameters.getProperty(COMMUNITY));
            txtCommunity.setWidth(width, Unit.PERCENTAGE);

            ComboBox<String> cmbAuthProtocol = new ComboBox<>();
            components.put(AUTH_PROTOCOL, cmbAuthProtocol);
            cmbAuthProtocol.setValue(parameters.getProperty(AUTH_PROTOCOL));
            cmbAuthProtocol.setWidth(width, Unit.PERCENTAGE);
            cmbAuthProtocol.setItems(Arrays.asList("MD5")); //NOI18N

            TextField txtAuthPass = new TextField();
            components.put(AUTH_PASS, txtAuthPass);
            if (parameters.containsKey(AUTH_PASS))
                txtAuthPass.setValue(parameters.getProperty(AUTH_PASS));
            txtAuthPass.setWidth(width, Unit.PERCENTAGE);

            ComboBox<String> cmbSecurityLevel = new ComboBox<>();
            components.put(SECURITY_LEVEL, cmbSecurityLevel);
            cmbSecurityLevel.setValue(parameters.getProperty(SECURITY_LEVEL));
            cmbSecurityLevel.setWidth(width, Unit.PERCENTAGE);
            cmbSecurityLevel.setItems(Arrays.asList("noAuthNoPriv", "authNoPriv", "authPriv")); //NOI18N

            TextField txtContextName = new TextField();
            components.put(CONTEXT_NAME, txtContextName);
            if (parameters.containsKey(CONTEXT_NAME))
                txtContextName.setValue(parameters.getProperty(CONTEXT_NAME));
            txtContextName.setWidth(width, Unit.PERCENTAGE);

            TextField txtSecurityName = new TextField();
            components.put(SECURITY_NAME, txtSecurityName);
            if (parameters.containsKey(SECURITY_NAME))
                txtSecurityName.setValue(parameters.getProperty(SECURITY_NAME));
            txtSecurityName.setWidth(width, Unit.PERCENTAGE);

            ComboBox<String> cmbPrivacyProtocol = new ComboBox<>();
            components.put(PRIVACY_PROTOCOL, cmbPrivacyProtocol);
            cmbPrivacyProtocol.setValue(parameters.getProperty(PRIVACY_PROTOCOL));
            cmbPrivacyProtocol.setWidth(width, Unit.PERCENTAGE);
            cmbPrivacyProtocol.setItems(Arrays.asList("DES")); //NOI18N

            TextField txtPrivacyPass = new TextField();
            components.put(PRIVACY_PASS, txtPrivacyPass);
            if (parameters.containsKey(PRIVACY_PASS))
                txtPrivacyPass.setValue(parameters.getProperty(PRIVACY_PASS));
            txtPrivacyPass.setWidth(width, Unit.PERCENTAGE);
            
            TextField txtSSHPort = new TextField();
            components.put(SSH_PORT, txtSSHPort);
            if (parameters.containsKey(SSH_PORT))
                txtSSHPort.setValue(parameters.getProperty(SSH_PORT));
            txtSSHPort.setWidth(width, Unit.PERCENTAGE);
                                    
            TextField txtSSHUser = new TextField();
            components.put(SSH_USER, txtSSHUser);
            if (parameters.containsKey(SSH_USER))
                txtSSHUser.setValue(parameters.getProperty(SSH_USER));
            txtSSHUser.setWidth(width, Unit.PERCENTAGE);

            TextField txtSSHPassword = new TextField();
            components.put(SSH_PASSWORD, txtSSHPassword);
            if (parameters.containsKey(SSH_PASSWORD))
                txtSSHPassword.setValue(parameters.getProperty(SSH_PASSWORD));
            txtSSHPassword.setWidth(width, Unit.PERCENTAGE);
            
            Label lblGeneral = new Label("General");
            lblGeneral.addStyleNames(ValoTheme.LABEL_LARGE, ValoTheme.LABEL_BOLD);
            
            Label lblSNMP = new Label("SNMP");
            lblSNMP.addStyleNames(ValoTheme.LABEL_LARGE, ValoTheme.LABEL_BOLD);

            Label lbl2c = new Label("SNMP Version 2c");
            lbl2c.addStyleName(ValoTheme.LABEL_BOLD);

            Label lbl3 = new Label("SNMP Version 3");
            lbl3.addStyleName(ValoTheme.LABEL_BOLD);

            Label lblSSH = new Label("SSH");
            lblSSH.addStyleNames(ValoTheme.LABEL_LARGE, ValoTheme.LABEL_BOLD);

            Label lblDevice = new Label(device.getName());        
            lblDevice.addStyleName(ValoTheme.LABEL_BOLD);
            
            HorizontalLayout hlySyncGroups = new HorizontalLayout();
            hlySyncGroups.setWidth(100, Unit.PERCENTAGE);
            hlySyncGroups.setHeight(300, Unit.PIXELS);

            gltContent.addComponent(new Label("Device"));
            gltContent.addComponent(lblDevice);

            gltContent.addComponent(lblGeneral, 0, 1, 1, 1);
            
            gltContent.addComponent(new Label("Sync Group"));
            gltContent.addComponent(cbmSynGroup);
            
            gltContent.addComponent(new Label("Name"));
            gltContent.addComponent(txtName);

            gltContent.addComponent(new Label("Ip Address"));
            gltContent.addComponent(txtIpAddress);

            gltContent.addComponent(new Label("Port"));
            gltContent.addComponent(txtPort);        

            gltContent.addComponent(lblSNMP, 0, 6, 1, 6);

            gltContent.addComponent(new Label("Version"));
            gltContent.addComponent(cmbVersion);

            gltContent.addComponent(lbl2c, 0, 8, 1, 8);

            gltContent.addComponent(new Label("Community"));
            gltContent.addComponent(txtCommunity);

            gltContent.addComponent(lbl3, 0, 10, 1, 10);

            gltContent.addComponent(new Label("Auth Protocol"));
            gltContent.addComponent(cmbAuthProtocol);

            gltContent.addComponent(new Label("Auth Pass"));
            gltContent.addComponent(txtAuthPass);

            gltContent.addComponent(new Label("Security Level"));
            gltContent.addComponent(cmbSecurityLevel);

            gltContent.addComponent(new Label("Context Name"));
            gltContent.addComponent(txtContextName);

            gltContent.addComponent(new Label("Security Name"));
            gltContent.addComponent(txtSecurityName);

            gltContent.addComponent(new Label("Privacy Protocol"));
            gltContent.addComponent(cmbPrivacyProtocol);

            gltContent.addComponent(new Label("Privacy Pass"));
            gltContent.addComponent(txtPrivacyPass);

            gltContent.addComponent(lblSSH, 0, 18, 1, 18);
            
            gltContent.addComponent(new Label("SSH Port"));
            gltContent.addComponent(txtSSHPort);

            gltContent.addComponent(new Label("SSH User"));
            gltContent.addComponent(txtSSHUser);

            gltContent.addComponent(new Label("SSH Password"));
            gltContent.addComponent(txtSSHPassword);

            vltContent.addComponent(gltContent);
            vltContent.setComponentAlignment(gltContent, Alignment.TOP_CENTER);
            
            pnlContent.setContent(vltContent);

            HorizontalLayout lytButtons = new HorizontalLayout();
            lytButtons.setWidthUndefined();
            lytButtons.setHeight(100, Unit.PERCENTAGE);

            Button btnOk = new Button("Ok");
            btnOk.setWidth(70, Unit.PIXELS);

            Button btnCancel = new Button("Cancel");
            btnCancel.setWidth(70, Unit.PIXELS);
            
            btnOk.addClickListener(new Button.ClickListener() {
                
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    
                    List<StringPair> newParameters = new ArrayList();
                    newParameters.add(new StringPair(DEVICE_ID, String.valueOf(device.getId())));
                    newParameters.add(new StringPair(DEVICE_CLASS, device.getClassName()));

                    for (String parameter : components.keySet()) {
                        
                        if (components.get(parameter) instanceof TextField) {
                            TextField textField = (TextField) components.get(parameter);
                            if (textField.getValue() != null)
                                newParameters.add(new StringPair(parameter, textField.getValue()));
                        }
                        else if (components.get(parameter) instanceof ComboBox) {
                            ComboBox<String> comboBox = (ComboBox<String>) components.get(parameter);
                            if (comboBox.getValue() != null)
                                newParameters.add(new StringPair(parameter, comboBox.getValue()));
                        }
                    }
                    
                    try {
                        RemoteSynchronizationConfiguration syncConfig = wsBean.getSyncDataSourceConfiguration(
                            device.getId(), 
                            remoteSession.getIpAddress(), 
                            remoteSession.getSessionId());
                        
                        try {
                            wsBean.updateSyncDataSourceConfiguration(
                                syncConfig.getId(), 
                                newParameters, 
                                remoteSession.getIpAddress(), 
                                remoteSession.getSessionId());
                            Notifications.showInfo("Synchronization Data Source Configuration was Updated Successfully");
                            window.close();
                        } catch(ServerSideException ex) {
                            Notifications.showError(ex.getMessage());
                        }
                    } catch(ServerSideException ex) {
                        for (StringPair newParameter : newParameters) {
                            if (NAME.equals(newParameter.getKey())) {
                                newParameters.remove(newParameter);
                                break;
                            }
                        }
                        
                        if (cbmSynGroup != null && cbmSynGroup.getValue() != null) {
                            try {
                                wsBean.createSynchronizationDataSourceConfig(
                                    device.getId(), 
                                    cbmSynGroup.getValue().getId(), 
                                    device.getName() + " [Datasource config]", 
                                    newParameters, 
                                    remoteSession.getIpAddress(), 
                                    remoteSession.getSessionId());

                                Notifications.showInfo("Synchronization Data Source Configuration was Created Successfully");
                                window.close();                                
                            } catch(ServerSideException serverSideEx) {
                                Notifications.showError(serverSideEx.getMessage());                                                                                    
                            }
                        }
                        else
                            Notifications.showWarning("Select a Sync Group");                                                
                    }
                }
            });
            
            btnCancel.addClickListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    window.close();
                }
            });

            lytButtons.addComponent(btnOk);
            lytButtons.addComponent(btnCancel);

            lytButtons.setComponentAlignment(btnOk, Alignment.MIDDLE_CENTER);
            lytButtons.setComponentAlignment(btnCancel, Alignment.MIDDLE_CENTER);

            mainLayout.addComponent(pnlContent);        
            mainLayout.addComponent(lytButtons);

            mainLayout.setComponentAlignment(pnlContent, Alignment.TOP_CENTER);
            mainLayout.setComponentAlignment(lytButtons, Alignment.BOTTOM_CENTER);

            mainLayout.setExpandRatio(pnlContent, 0.9f);
            mainLayout.setExpandRatio(lytButtons, 0.1f);

            window.setContent(mainLayout);
            return window;     
            
        } catch(NumberFormatException | ServerSideException ex) {
            Notifications.showError(ex.getMessage());
            return null;            
        }
    }

    @Override
    public Panel launchEmbedded() {
        throw new UnsupportedOperationException("This application can not run in embedded mode");
    }

    @Override
    public int getType() {
        return TYPE_WEB;
    }
}
