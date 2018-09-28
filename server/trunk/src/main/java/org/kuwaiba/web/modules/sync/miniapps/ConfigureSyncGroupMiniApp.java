/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.RadioButtonGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.List;
import java.util.Properties;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.util.StringPair;
import org.kuwaiba.apis.web.gui.miniapps.AbstractMiniApplication;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSynchronizationGroup;

/**
 * This mini application allows to configure a synchronization group, and add data source configurations to it. It's also possible that instead of creating a sync group from scratch, the user chooses an existing one
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
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
        Wizard aWizard = new Wizard(new ChooseConfigurationStep(inputParameters));
        WizardWindow wdwSyncGroupConfiguration = new WizardWindow("Sync Group Configuration");
        wdwSyncGroupConfiguration.setModal(true);
        wdwSyncGroupConfiguration.center();
        
        HorizontalLayout lytContent = new HorizontalLayout(new Panel(), aWizard, new Panel());
        lytContent.setSizeFull();
        lytContent.setExpandRatio(aWizard, 8);
        
        wdwSyncGroupConfiguration.setContent(lytContent);
        aWizard.addEventListener(wdwSyncGroupConfiguration);
        return wdwSyncGroupConfiguration;
    }

    @Override
    public Panel launchEmbedded() {
        throw new UnsupportedOperationException("This application can not run in embedded mode");
    }

    @Override
    public int getType() {
        return TYPE_WEB;
    }
    
    private class WizardWindow extends Window implements WizardEventListener {

        public WizardWindow(String caption) {
            super(caption);
            setHeight(80, Unit.PERCENTAGE);
        }
        
        @Override
        public void eventFired(WizardEvent event) {
            
            switch(event.getType()) {
                case WizardEvent.TYPE_FINAL_STEP:
                    Notifications.showInfo("Action completed successfully");
                    close();
                    break;
                case WizardEvent.TYPE_NEXT_STEP:
                    setCaption(event.getMessage());
                    break;
                case WizardEvent.TYPE_CANCEL:
                    close();
            }
        }
    }
    
    
    /**
     * Interface implemented by all the steps in the wizard
     */
    private interface Step extends Component {
        
        /**
         * What to do next. This method also validates if the information requested in the step is correct. Should be called upon the user clicking on th "Next/Finish" button
         * @return Null if it's the final step, or the next step otherwise
         * @throws InvalidArgumentException If the information provided by the user in the current step in missing or inconsistent
         */
        public Step next() throws InvalidArgumentException;
        /**
         * Indicates if the step is the last one
         * @return True if the step is the last one, false otherwise
         */
        public boolean isFinal();
    }
    
    /**
     * Interface to be implemented by all components interested in listening to the events fired by a {@link Wizard}
     */
    private interface WizardEventListener extends EventListener {
        /**
         * What to do when an event is fired
         */
        public void eventFired(WizardEvent event);
    }
    
    private class WizardEvent {
        /**
         * When passing from one step to the next
         */
        public final static int TYPE_NEXT_STEP = 1;
        /**
         * When the wizard ends and the user hits Finish
         */
        public final static int TYPE_FINAL_STEP = 2;
        /**
         * If passing from one step to the next is rejected by the validate() method
         */
        public final static int TYPE_STEP_REJECTED = 3;
        /**
         * The user hit "Cancel"
         */
        public final static int TYPE_CANCEL = 4;
        /**
         * Type of event. See TYPE_XXX for possible values
         */
        private int type;
        /**
         * The message associated to the event
         */
        private String message;

        public WizardEvent(int type, String message) {
            this.type = type;
            this.message = message;
        }

        public int getType() {
            return type;
        }

        public String getMessage() {
            return message;
        }
    }
    
    /**
     * The actual wizard
     */
    private class Wizard extends VerticalLayout {
        /**
         * Main panel
         */
        private Panel pnlMain;
        /**
         * The next or finish action
         */
        private Button btnAction;
        /**
         * The cancel action
         */
        private Button btnCancel;
        /**
         * Reference to the current step
         */
        private Step currentStep;
        /**
         * The event listeners
         */
        private List<WizardEventListener> listeners;
        
        public Wizard(Step firstStep) {
            this.setSizeFull();
            this.pnlMain = new Panel();
            this.btnAction = new Button();
            this.btnCancel = new Button("Cancel");
            
            this.btnAction.addClickListener((event) -> {
                try {
                    Step nextStep = currentStep.next();
                    if (nextStep != null) {
                        currentStep = nextStep;
                        pnlMain.setContent(currentStep);

                        if (currentStep.isFinal())
                            btnAction.setCaption("Finish");
                        else
                            btnAction.setCaption("Next");

                        fireEvent(new WizardEvent(WizardEvent.TYPE_NEXT_STEP, currentStep.getCaption()));
                    } else 
                        fireEvent(new WizardEvent(WizardEvent.TYPE_FINAL_STEP, ""));
                } catch (InvalidArgumentException ex) {
                    fireEvent(new WizardEvent(WizardEvent.TYPE_STEP_REJECTED, ex.getLocalizedMessage()));
                    Notifications.showError(ex.getLocalizedMessage());
                }
                
            });
            
            btnCancel.addClickListener((event) -> {
                fireEvent(new WizardEvent(WizardEvent.TYPE_CANCEL, ""));
            });
            
            this.pnlMain.setContent(firstStep);
            this.currentStep = firstStep;
            if (currentStep.isFinal())
                this.btnAction.setCaption("Finish");
            else
                this.btnAction.setCaption("Next");
            
            HorizontalLayout lytButtons = new HorizontalLayout(btnAction, btnCancel);
            
            this.addComponents(pnlMain, lytButtons);
            this.setComponentAlignment(lytButtons, Alignment.MIDDLE_RIGHT);
            
            this.listeners = new ArrayList<>();
        }
        
        public void fireEvent(WizardEvent event) {
            listeners.forEach((listener) -> { listener.eventFired(event); });
        }
        
        public void addEventListener(WizardEventListener listener) {
            listeners.add(listener);
        }
        
        public void clearListeners() {
            listeners.clear();
        }
    }
    
    /**
     * In this step the user chooses if he/she wants to add the device to an existing sync group or create one from scratch
     */
    private class ChooseConfigurationStep extends VerticalLayout implements Step {
        /**
         * The options to be considered
         */
        private RadioButtonGroup<Option> chkOptions;
        /**
         * Own properties to be transferred from step to step
         */
        private Properties properties;
        
        public ChooseConfigurationStep(Properties properties) {
            this.setSizeFull();
            this.setCaption("Choose Configuration");
            this.properties = properties;
            this.chkOptions = new RadioButtonGroup<>("What kind of configuration do you want to start?");
            this.chkOptions.setItems(new Option(1, "Select an existing Synchronization Group"), new Option(2, "Create a Synchronization Group from scratch"));
            this.addComponent(chkOptions);
        }

        @Override
        public boolean isFinal() {
            return false;
        }

        @Override
        public Step next() throws InvalidArgumentException {
            Option selectedOption = this.chkOptions.getSelectedItem().isPresent() ? this.chkOptions.getSelectedItem().get() : null;
            if (selectedOption == null)
                throw new InvalidArgumentException("You must choose an option before continuing");
            
            if (selectedOption.getId() == 1)
                return new ChooseSyncGroupStep(properties);
            else
                return new NewSyncGroupStep(properties);
           
        }
        
        /**
         * Dummy class used to display the options in the radio button group
         */
        private class Option {
            /**
             * The id of the option
             */
            private int id;
            /**
             * The readable option
             */
            private String message;

            public Option(int id, String message) {
                this.id = id;
                this.message = message;
            }

            public int getId() {
                return id;
            }

            public String getMessage() {
                return message;
            }
            
            @Override
            public String toString() {
                return message;
            }
        }
    }
    
    /**
     * This steps allows the user to set the initial configuration of a new sync group
     */
    private class NewSyncGroupStep extends FormLayout implements Step {
        /**
         * Field to input the name of the new sync group
         */
        private TextField txtName;
        /**
         * Field to input the description of the new sync group
         */
        private TextField txtDescription;
        /**
         * Field to input the provider of the new sync group
         */
        private ComboBox<SyncProvider> cmbSyncProviders;
        /**
         * Own properties
         */
        private Properties properties;
        
        public NewSyncGroupStep(Properties properties) {
            this.setSizeFull();
            this.setCaption("Create Sync Group");
            this.properties = properties;
            this.txtName = new TextField("Name");
            this.txtDescription = new TextField("Description");
            this.cmbSyncProviders = new ComboBox<>("Provider", 
                    Arrays.asList(new SyncProvider("com.neotropic.kuwaiba.sync.connectors.snmp.reference.ReferenceSnmpSyncProvider", "Hardware/Interfaces from entityMIB"),
                                  new SyncProvider("com.neotropic.kuwaiba.sync.connectors.snmp.mpls.SnmpMplsSyncProvider", "General MPLS Information"),
                                  new SyncProvider("com.neotropic.kuwaiba.sync.connectors.snmp.ip.IPAddressesSyncProvider", "IP Addresses"), 
                                  new SyncProvider("com.neotropic.kuwaiba.sync.connectors.snmp.vlan.SnmpCiscoVlansSyncProvider", "VLANs"),
                                  new SyncProvider("com.neotropic.kuwaiba.sync.connectors.ssh.bdi.BridgeDomainSyncProvider", "Bridge Domains")));
            this.cmbSyncProviders.setEmptySelectionAllowed(false);
            this.addComponents(txtName, txtDescription, cmbSyncProviders);
        }
        
        @Override
        public boolean isFinal() {
            return false;
        }

        @Override
        public Step next() throws InvalidArgumentException {
            if (txtName.getValue().trim().isEmpty() || !cmbSyncProviders.getSelectedItem().isPresent())
                throw new InvalidArgumentException("Make sure you provided a valid name and provider");
            try {
                properties.put("syncGroupId", wsBean.createSynchronizationGroup(txtName.getValue(), cmbSyncProviders.getSelectedItem().get().getProviderId(), 
                        Page.getCurrent().getWebBrowser().getAddress(),
                        ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()));
                return new NewDataSourceConfigurationStep(properties);
            } catch (ServerSideException ex) {
                throw new InvalidArgumentException(ex.getLocalizedMessage());
            }
            
        }
        
        /**
         * Dummy class used to display the sync providers in a combo box
         */
        private class SyncProvider {
            /**
             * Label to be used in the combo box
             */
            private String displayName;
            /**
             * Provider id (the FQN of the class of the sync provider)
             */
            private String providerId;

            public SyncProvider(String providerId, String displayName) {
                this.displayName = displayName;
                this.providerId = providerId;
            }

            public String getDisplayName() {
                return displayName;
            }

            public String getProviderId() {
                return providerId;
            }
            
            @Override
            public String toString() {
                return displayName;
            }
        }
    }
    
    /**
     * This step allows to create a data source configuration associated to the the sync group created/chosen previously
     */
    private class NewDataSourceConfigurationStep extends FormLayout implements Step {
        /**
         * The field that captures the name of the data source configuration
         */
        private TextField txtName;
        /**
         * The field that captures the IP address property of the data source configuration
         */
        private TextField txtIpAddress;
        /**
         * The field that captures the  port property of the data source configuration
         */
        private TextField txtPort;
        /**
         * The field that captures the user property of the data source configuration (if applicable)
         */
        private TextField txtUser;
        /**
         * The field that captures the password property of the data source configuration (if applicable)
         */
        private PasswordField txtPassword;
        /**
         * Own properties
         */
        private Properties properties;
        
        public NewDataSourceConfigurationStep(Properties properties) {
            this.setSizeFull();
            this.setCaption("New Sync Data Source");
            this.properties = properties;
            this.txtName = new TextField("Name");
            this.txtName.setRequiredIndicatorVisible(true);
            this.txtIpAddress = new TextField("IP Address");
            this.txtPort = new TextField("Port");
            this.txtUser = new TextField("User");
            this.txtPassword = new PasswordField("Password");
            this.addComponents(txtName, txtIpAddress, txtPort, txtUser, txtPassword);
        }

        @Override
        public boolean isFinal() {
            return true;
        }

        @Override
        public Step next() throws InvalidArgumentException {
            if (txtName.getValue().trim().isEmpty())
                throw new InvalidArgumentException("Make sure you provided a valid name for this Data Source Configuration");
            try {
                wsBean.createSynchronizationDataSourceConfig((long)properties.get("syncGroupId"), txtName.getValue(), 
                        Arrays.asList(new StringPair("deviceId", properties.getProperty("deviceId")), new StringPair("deviceClass", properties.getProperty("deviceClass")), 
                                new StringPair("ipAddress", txtIpAddress.getValue()), new StringPair("port", txtPort.getValue()),
                                new StringPair("user", txtUser.getValue()), new StringPair("password", txtPassword.getValue())), 
                        Page.getCurrent().getWebBrowser().getAddress(),
                        ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
                return null;
            } catch (ServerSideException ex) {
                throw new InvalidArgumentException(ex.getLocalizedMessage());
            }
        }
        
    }
    
    /**
     * This step allows to choose an existing sync group
     */
    private class ChooseSyncGroupStep extends VerticalLayout implements Step {
        /**
         * The table to choose the sync groups from
         */
        private Grid<RemoteSynchronizationGroup> tblSyncGroups;
        /**
         * The own properties
         */
        private Properties properties;
        
        public ChooseSyncGroupStep(Properties properties) {
            this.setSizeFull();
            this.setCaption("Choose Sync Group");
            this.properties = properties;
            this.tblSyncGroups = new Grid<>("Select a sync group from the list");
            try {
                tblSyncGroups.setItems(wsBean.getSynchronizationGroups(Page.getCurrent().getWebBrowser().getAddress(), 
                        ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()));
                
                tblSyncGroups.addColumn(RemoteSynchronizationGroup::getName).setCaption("Name");
                tblSyncGroups.addColumn(RemoteSynchronizationGroup::getProvider).setCaption("Provider");
            } catch (ServerSideException ex) {
                Notifications.showError(ex.getLocalizedMessage());
            }
            this.addComponent(tblSyncGroups);
        }
        
        @Override
        public boolean isFinal() {
            return false;
        }

        @Override
        public Step next() throws InvalidArgumentException {
            if (tblSyncGroups.getSelectedItems().isEmpty()) 
                throw new InvalidArgumentException("Select a Synchronization Group from the list");

            properties.put("syncGroupId", tblSyncGroups.getSelectedItems().iterator().next().getId());
            return new NewDataSourceConfigurationStep(properties);
        }
    }
}
