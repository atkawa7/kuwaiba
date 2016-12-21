/**
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.inventory.modules.mpls.wizard;

import com.neotropic.inventory.modules.mpls.MPLSConfigurationObject;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.GridLayout;
import java.text.MessageFormat;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.ExplorablePanel;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.explorer.view.BeanTreeView;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 * This is the wizard to make MPLS connections
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class MPLSConnectionWizard {
    
    private CommunicationsStub com = CommunicationsStub.getInstance();
    
    public LocalObjectLight run(LocalObjectLight equipmentA, LocalObjectLight equipmentB) {        
        MPLSConfigurationObject configObject = Lookup.getDefault().lookup(MPLSConfigurationObject.class);
        WizardDescriptor wizardDescriptor;
        wizardDescriptor = new WizardDescriptor(new WizardDescriptor.Panel[] {
                    new ConnectionGeneralInfoStep((Connections)configObject.getProperty("connectionType")),
                    new ChooseConnectionEndpointsStep(equipmentA, equipmentB)});
        
        initWizardDescriptor(wizardDescriptor, new String[] { "Set a connection name", "Choose the endpoints" });
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);

        dialog.setVisible(true);
        dialog.toFront();
        
        //The thread will be blocked either Cancel or Finish is clicked
        if (wizardDescriptor.getValue() == WizardDescriptor.FINISH_OPTION) {
            LocalObjectLight sourcePort = (LocalObjectLight)wizardDescriptor.getProperty("sourcePort");
            LocalObjectLight targetPort = (LocalObjectLight)wizardDescriptor.getProperty("targetPort");
            String connectionName = (String)wizardDescriptor.getProperty("connectionName");
            LocalObjectLight newTransportLink = com.createMPLSLink(sourcePort, targetPort, "MPLSLink", connectionName);
            if (newTransportLink == null) {
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
                return null;
            } else 
                return newTransportLink;
        } else
            return null;
    }
    
    public void initWizardDescriptor(WizardDescriptor wizardDescriptor, String[] labels) {
        //How the title of the panels should be displayed (by default it says something like "PANEL_NAME wizard STEPX of Y")
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
        //See WizardDescriptor.PROP_AUTO_WIZARD_STYLE documentation for a complete list of things you are enabling here
        wizardDescriptor.putProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE);
        //An image and the list of steps should be shown in a panel on the left side of the wizard?
        wizardDescriptor.putProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE);
        //Should the steps be numbered in the panel on the left side?
        wizardDescriptor.putProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE);
        //The list of steps on the left panel of the wizard
        wizardDescriptor.putProperty(WizardDescriptor.PROP_CONTENT_DATA, labels);
        wizardDescriptor.setTitle("MPLS Connection Wizard");
    }
    
    public enum Connections {
        CONNECTION_MPLSLINK
    }
    
     private class ConnectionGeneralInfoStep implements WizardDescriptor.ValidatingPanel<WizardDescriptor> {
        private JComplexDialogPanel thePanel;

        public ConnectionGeneralInfoStep(Connections connection) {
            final JTextField txtConnectionName = new JTextField(20);                 
            txtConnectionName.setName("txtConnectionName"); //NOI18N
            
            
            thePanel = new JComplexDialogPanel(new String[] {"Connection name"}, new JComponent[] {txtConnectionName});
            thePanel.setName("General Information");
            //Shows what step we're in on the left panel of the wizard
            thePanel.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, 0);
        }
        
        @Override
        public Component getComponent() {
            return thePanel;
        }

        @Override
        public HelpCtx getHelp() {
            return null;
        }

        @Override
        public void readSettings(WizardDescriptor settings) {}

        @Override
        public void storeSettings(WizardDescriptor settings) {
            settings.putProperty("connectionName", ((JTextField)thePanel.getComponent("txtConnectionName")).getText());
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public void addChangeListener(ChangeListener l) {
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
        }
        
        @Override
        public void validate() throws WizardValidationException {
            if (((JTextField)thePanel.getComponent("txtConnectionName")).getText().trim().isEmpty())
                throw new WizardValidationException(thePanel.getComponent("txtConnectionName"), "The connection name can not be empty", null);
        }   
    }
    
    private class ChooseConnectionEndpointsStep implements WizardDescriptor.ValidatingPanel<WizardDescriptor> {
        private ExplorablePanel pnlTreeASide;
        private ExplorablePanel pnlTreeBSide;
        private JPanel thePanel;
        private LocalObjectLight sourcePort, targetPort;

        public ChooseConnectionEndpointsStep(LocalObjectLight equipmentA, LocalObjectLight equipmentB) {
            thePanel = new JPanel();
            
            BeanTreeView treeASide = new BeanTreeView();
            BeanTreeView treeBSide = new BeanTreeView();
            
            pnlTreeASide = new ExplorablePanel();
            pnlTreeBSide = new ExplorablePanel();
            
            pnlTreeASide.add(treeASide);
            pnlTreeBSide.add(treeBSide);
            
            pnlTreeASide.getExplorerManager().setRootContext(new ObjectNode(equipmentA));
            pnlTreeBSide.getExplorerManager().setRootContext(new ObjectNode(equipmentB));
            
            JScrollPane pnlScrollTreeASide = new JScrollPane();
            JScrollPane pnlScrollTreeBSide = new JScrollPane();
            
            pnlScrollTreeASide.setViewportView(pnlTreeASide);
            pnlScrollTreeBSide.setViewportView(pnlTreeBSide);
            
            GridLayout layout = new GridLayout(1, 2);
            thePanel.setLayout(layout);
            thePanel.add(pnlScrollTreeASide);
            thePanel.add(pnlScrollTreeBSide);
            
            thePanel.setName("Select the endpoints");
            //Shows what step we're in on the left panel of the wizard
            thePanel.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, 1);
        }

        @Override
        public Component getComponent() {
            return thePanel;
        }

        @Override
        public HelpCtx getHelp() {
            return null;
        }

        @Override
        public void readSettings(WizardDescriptor settings) {
        }

        @Override
        public void storeSettings(WizardDescriptor settings) {
             settings.putProperty("sourcePort", sourcePort);
             settings.putProperty("targetPort", targetPort);
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public void addChangeListener(ChangeListener l) {
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
        }

        @Override
        public void validate() throws WizardValidationException {
            sourcePort = pnlTreeASide.getLookup().lookup(LocalObjectLight.class);
            if (sourcePort == null || !com.isSubclassOf(sourcePort.getClassName(), Constants.CLASS_GENERICPORT))
                throw new WizardValidationException(pnlTreeASide, "You have to select a source port on the left panel", null);
            
            targetPort = pnlTreeBSide.getLookup().lookup(LocalObjectLight.class);
            if (targetPort == null || !com.isSubclassOf(targetPort.getClassName(), Constants.CLASS_GENERICPORT))
                throw new WizardValidationException(pnlTreeASide, "You have to select a target port on the right panel", null);
        }
    }
}
