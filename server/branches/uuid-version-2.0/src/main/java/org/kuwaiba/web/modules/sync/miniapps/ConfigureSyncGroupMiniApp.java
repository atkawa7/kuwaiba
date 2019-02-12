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

import com.vaadin.server.Page;
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
        throw new UnsupportedOperationException("This mini application allows to configure a synchronization group, and add data source configurations to it. It's also possible that instead of creating a sync group from scratch, the user chooses an existing one"); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Window launchDetached() {
////        Wizard aWizard = new Wizard(new ChooseConfigurationStep(inputParameters));
////        WizardWindow wdwSyncGroupConfiguration = new WizardWindow("Sync Group Configuration");
////        wdwSyncGroupConfiguration.setModal(true);
////        wdwSyncGroupConfiguration.center();
////        
////        HorizontalLayout lytContent = new HorizontalLayout(new Panel(), aWizard, new Panel());
////        lytContent.setSizeFull();
////        lytContent.setExpandRatio(aWizard, 8);
////        
////        wdwSyncGroupConfiguration.setContent(lytContent);
////        aWizard.addEventListener(wdwSyncGroupConfiguration);
////        return wdwSyncGroupConfiguration;
        if (inputParameters == null)
            return null;
        
        try {
            if (!inputParameters.containsKey("deviceId")) { //NOI18N
                Notifications.showError("Missing input parameter deviceId");
                return null;
            }
            if (!inputParameters.containsKey("deviceClass")) { //NOI18N
                Notifications.showError("Missing input parameter deviceClass");
                return null;
            }
            String deviceId = inputParameters.getProperty("deviceId"); //NOI18N
            String deviceClass = inputParameters.getProperty("deviceClass"); //NOI18N
            
            final RemoteObjectLight device = wsBean.getObjectLight(deviceClass, deviceId, 
                Page.getCurrent().getWebBrowser().getAddress(),
                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N
            
            if (device == null)            
                return null;
            
            List<RemoteSynchronizationGroup> syncGroups = wsBean.getSynchronizationGroups(
                Page.getCurrent().getWebBrowser().getAddress(), 
                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N
                        
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
            final String USER = "user"; //NOI18N
            final String PASSWORD = "password"; //NOI18N
            final String DEVICE_ID = "deviceId"; //NOI18N
            final String DEVICE_CLASS = "deviceClass"; //NOI18N
            
            RemoteSynchronizationConfiguration syncConfig = null;
            
            try {
                syncConfig = wsBean.getSyncDataSourceConfiguration(
                    device.getId(), 
                    Page.getCurrent().getWebBrowser().getAddress(), 
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N
                
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
                       Page.getCurrent().getWebBrowser().getAddress(), 
                       ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N

                    if (dataSourceConfigs == null)
                        return null;
                    
                    boolean hasDataSourceConfig = false;

                    for (RemoteSynchronizationConfiguration dataSourceConfig : dataSourceConfigs) {
                        if (syncConfig.getId() == dataSourceConfig.getId()) {
                            hasDataSourceConfig = true;
                            continue;                                                        
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

            TextField txtUser = new TextField();
            components.put(USER, txtUser);
            if (parameters.containsKey(USER))
                txtUser.setValue(parameters.getProperty(USER));
            txtUser.setWidth(width, Unit.PERCENTAGE);

            TextField txtPassword = new TextField();
            components.put(PASSWORD, txtPassword);
            if (parameters.containsKey(PASSWORD))
                txtPassword.setValue(parameters.getProperty(PASSWORD));
            txtPassword.setWidth(width, Unit.PERCENTAGE);
            
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

            gltContent.addComponent(new Label("User"));
            gltContent.addComponent(txtUser);

            gltContent.addComponent(new Label("Password"));
            gltContent.addComponent(txtPassword);

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
                            Page.getCurrent().getWebBrowser().getAddress(), 
                            ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N
                        
                        try {
                            wsBean.updateSyncDataSourceConfiguration(
                                syncConfig.getId(), 
                                newParameters, 
                                Page.getCurrent().getWebBrowser().getAddress(), 
                                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N
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
                                    newParameters, Page.getCurrent().getWebBrowser().getAddress(), 
                                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N

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
    
////    private class WizardWindow extends Window implements Wizard.WizardEventListener {
////
////        public WizardWindow(String caption) {
////            super(caption);
////            setHeight(80, Unit.PERCENTAGE);
////        }
////        
////        @Override
////        public void eventFired(Wizard.WizardEvent event) {
////            
////            switch(event.getType()) {
////                case Wizard.WizardEvent.TYPE_FINAL_STEP:
////                    Notifications.showInfo("Action completed successfully");
////                    close();
////                    break;
////                case Wizard.WizardEvent.TYPE_NEXT_STEP:
////                    setCaption(event.getInformation().getProperty("title", "Next Step"));
////                    break;
////                case Wizard.WizardEvent.TYPE_CANCEL:
////                    close();
////            }
////        }
////    }
    
////    /**
////     * In this step the user chooses if he/she wants to add the device to an existing sync group or create one from scratch
////     */
////    private class ChooseConfigurationStep extends VerticalLayout implements Wizard.Step {
////        /**
////         * The options to be considered
////         */
////        private RadioButtonGroup<Option> chkOptions;
////        /**
////         * Own properties to be transferred from step to step
////         */
////        private Properties properties;
////        
////        public ChooseConfigurationStep(Properties properties) {
////            this.setSizeFull();
////            this.properties = properties;
////            
////            if (properties.get("deviceId") == null || properties.get("deviceClass") == null) 
////                this.addComponent(new Label("Make sure you set the properties deviceId and deviceClass properly (did you select a device from the list?)"));
////            else {
////                this.properties.put("title", "Choose Configuration");
////                this.chkOptions = new RadioButtonGroup<>("What kind of configuration do you want to start?");
////                this.chkOptions.setItems(new Option(1, "Select an existing Synchronization Group"), new Option(2, "Create a Synchronization Group from scratch"));
////                this.addComponent(chkOptions);
////            }
////            
////            
////        }
////
////        @Override
////        public boolean isFinal() {
////            return false;
////        }
////
////        @Override
////        public Wizard.Step next() throws InvalidArgumentException {
////            Option selectedOption = this.chkOptions.getSelectedItem().isPresent() ? this.chkOptions.getSelectedItem().get() : null;
////            if (selectedOption == null)
////                throw new InvalidArgumentException("You must choose an option before continuing");
////            
////            if (selectedOption.getId() == 1)
////                return new ChooseSyncGroupStep(properties);
////            else
////                return new NewSyncGroupStep(properties);
////           
////        }
////
////        @Override
////        public Properties getProperties() {
////            return properties;
////        }
////        
////        /**
////         * Dummy class used to display the options in the radio button group
////         */
////        private class Option {
////            /**
////             * The id of the option
////             */
////            private int id;
////            /**
////             * The readable option
////             */
////            private String message;
////
////            public Option(int id, String message) {
////                this.id = id;
////                this.message = message;
////            }
////
////            public int getId() {
////                return id;
////            }
////
////            public String getMessage() {
////                return message;
////            }
////            
////            @Override
////            public String toString() {
////                return message;
////            }
////        }
////    }
    
////    /**
////     * This steps allows the user to set the initial configuration of a new sync group
////     */
////    private class NewSyncGroupStep extends FormLayout implements Wizard.Step {
////        /**
////         * Field to input the name of the new sync group
////         */
////        private TextField txtName;
////        /**
////         * Field to input the description of the new sync group
////         */
////        private TextField txtDescription;
////        /**
////         * Field to input the provider of the new sync group
////         */
////        private ComboBox<SyncProvider> cmbSyncProviders;
////        /**
////         * Own properties
////         */
////        private Properties properties;
////        
////        public NewSyncGroupStep(Properties properties) {
////            this.setSizeFull();
////            this.properties = properties;
////            this.properties.put("title", "Create Sync Group");
////            this.txtName = new TextField("Name");
////            this.txtDescription = new TextField("Description");
////            this.cmbSyncProviders = new ComboBox<>("Provider", 
////                    Arrays.asList(new SyncProvider("com.neotropic.kuwaiba.sync.connectors.snmp.reference.ReferenceSnmpSyncProvider", "Hardware/Interfaces from entityMIB"),
////                                  new SyncProvider("com.neotropic.kuwaiba.sync.connectors.snmp.mpls.SnmpMplsSyncProvider", "General MPLS Information"),
////                                  new SyncProvider("com.neotropic.kuwaiba.sync.connectors.snmp.ip.IPAddressesSyncProvider", "IP Addresses"), 
////                                  new SyncProvider("com.neotropic.kuwaiba.sync.connectors.snmp.vlan.SnmpCiscoVlansSyncProvider", "VLANs"),
////                                  new SyncProvider("com.neotropic.kuwaiba.sync.connectors.ssh.bdi.BridgeDomainSyncProvider", "Bridge Domains")));
////            this.cmbSyncProviders.setEmptySelectionAllowed(false);
////            this.addComponents(txtName, txtDescription, cmbSyncProviders);
////        }
////        
////        @Override
////        public boolean isFinal() {
////            return false;
////        }
////
////        @Override
////        public Wizard.Step next() throws InvalidArgumentException {
////            if (txtName.getValue().trim().isEmpty() || !cmbSyncProviders.getSelectedItem().isPresent())
////                throw new InvalidArgumentException("Make sure you provided a valid name and provider");
////            try {
////                properties.put("syncGroupId", wsBean.createSynchronizationGroup(txtName.getValue(), 
////                        Page.getCurrent().getWebBrowser().getAddress(),
////                        ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()));
////                return new NewDataSourceConfigurationStep(properties);
////            } catch (ServerSideException ex) {
////                throw new InvalidArgumentException(ex.getLocalizedMessage());
////            }
////            
////        }
////
////        @Override
////        public Properties getProperties() {
////            return properties;
////        }
////        
////        /**
////         * Dummy class used to display the sync providers in a combo box
////         */
////        private class SyncProvider {
////            /**
////             * Label to be used in the combo box
////             */
////            private String displayName;
////            /**
////             * Provider id (the FQN of the class of the sync provider)
////             */
////            private String providerId;
////
////            public SyncProvider(String providerId, String displayName) {
////                this.displayName = displayName;
////                this.providerId = providerId;
////            }
////
////            public String getDisplayName() {
////                return displayName;
////            }
////
////            public String getProviderId() {
////                return providerId;
////            }
////            
////            @Override
////            public String toString() {
////                return displayName;
////            }
////        }
////    }
    
////    /**
////     * This step allows to create a data source configuration associated to the the sync group created/chosen previously
////     */
////    private class NewDataSourceConfigurationStep extends FormLayout implements Wizard.Step {
////        /**
////         * The field that captures the name of the data source configuration
////         */
////        private TextField txtName;
////        /**
////         * The field that captures the IP address property of the data source configuration
////         */
////        private TextField txtIpAddress;
////        /**
////         * The field that captures the  port property of the data source configuration
////         */
////        private TextField txtPort;
////        /**
////         * The field that captures the user property of the data source configuration (if applicable)
////         */
////        private TextField txtUser;
////        /**
////         * The field that captures the password property of the data source configuration (if applicable)
////         */
////        private PasswordField txtPassword;
////        /**
////         * Own properties
////         */
////        private Properties properties;
////        
////        public NewDataSourceConfigurationStep(Properties properties) {
////            this.setSizeFull();
////            this.properties = properties;
////            this.properties.put("title", "New Sync Data Source");
////            this.txtName = new TextField("Name");
////            this.txtName.setRequiredIndicatorVisible(true);
////            this.txtIpAddress = new TextField("IP Address");
////            this.txtPort = new TextField("Port");
////            this.txtUser = new TextField("User");
////            this.txtPassword = new PasswordField("Password");
////            this.addComponents(txtName, txtIpAddress, txtPort, txtUser, txtPassword);
////        }
////
////        @Override
////        public boolean isFinal() {
////            return true;
////        }
////
////        @Override
////        public Wizard.Step next() throws InvalidArgumentException {
////            if (txtName.getValue().trim().isEmpty())
////                throw new InvalidArgumentException("Make sure you provided a valid name for this Data Source Configuration");
////            //TODO modify this
//////            try {
//////                wsBean.createSynchronizationDataSourceConfig((long)properties.get("syncGroupId"), txtName.getValue(), 
//////                        Arrays.asList(new StringPair("deviceId", properties.getProperty("deviceId")), new StringPair("deviceClass", properties.getProperty("deviceClass")), 
//////                                new StringPair("ipAddress", txtIpAddress.getValue()), new StringPair("port", txtPort.getValue()),
//////                                new StringPair("user", txtUser.getValue()), new StringPair("password", txtPassword.getValue())), 
//////                        Page.getCurrent().getWebBrowser().getAddress(),
//////                        ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
////                return null;
//////            } catch (ServerSideException ex) {
//////                throw new InvalidArgumentException(ex.getLocalizedMessage());
//////            }
////        }
////
////        @Override
////        public Properties getProperties() {
////            return properties;
////        }
////        
////    }
    
////    /**
////     * This step allows to choose an existing sync group
////     */
////    private class ChooseSyncGroupStep extends VerticalLayout implements Wizard.Step {
////        /**
////         * The table to choose the sync groups from
////         */
////        private Grid<RemoteSynchronizationGroup> tblSyncGroups;
////        /**
////         * The own properties
////         */
////        private Properties properties;
////        
////        public ChooseSyncGroupStep(Properties properties) {
////            this.setSizeFull();
////            this.properties = properties;
////            this.properties.put("title", "Choose Sync Group");
////            
////            this.tblSyncGroups = new Grid<>("Select a sync group from the list");
////            try {
////                tblSyncGroups.setItems(wsBean.getSynchronizationGroups(Page.getCurrent().getWebBrowser().getAddress(), 
////                        ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()));
////                
////                tblSyncGroups.addColumn(RemoteSynchronizationGroup::getName).setCaption("Name");
////                //TODO tblSyncGroups.addColumn(RemoteSynchronizationGroup::getProvider).setCaption("Provider");
////            } catch (ServerSideException ex) {
////                Notifications.showError(ex.getLocalizedMessage());
////            }
////            this.addComponent(tblSyncGroups);
////        }
////        
////        @Override
////        public boolean isFinal() {
////            return false;
////        }
////
////        @Override
////        public Wizard.Step next() throws InvalidArgumentException {
////            if (tblSyncGroups.getSelectedItems().isEmpty()) 
////                throw new InvalidArgumentException("Select a Synchronization Group from the list");
////
////            properties.put("syncGroupId", tblSyncGroups.getSelectedItems().iterator().next().getId());
////            return new NewDataSourceConfigurationStep(properties);
////        }
////
////        @Override
////        public Properties getProperties() {
////            return properties;
////        }
////    }
}
