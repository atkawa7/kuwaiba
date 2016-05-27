/*
 * Copyright (c) 2016 gir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    gir - initial API and implementation and/or initial documentation
 */
package com.neotropic.inventory.modules.sdh.wizard;

import com.neotropic.inventory.modules.sdh.SDHConfigurationObject;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.GridLayout;
import java.text.MessageFormat;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadataLight;
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
 * This is the wizard to make SDH connections
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class SDHConnectionWizard {
    private CommunicationsStub com = CommunicationsStub.getInstance();

    public LocalObjectLight run(LocalObjectLight equipmentA, LocalObjectLight equipmentB) {
        SDHConfigurationObject configObject = Lookup.getDefault().lookup(SDHConfigurationObject.class);
        WizardDescriptor wizardDescriptor = 
                new WizardDescriptor(new WizardDescriptor.Panel[] {
                    new ConnectionGeneralInfoStep(configObject.getProperty("connectionType") == null ? Connections.CONNECTION_TRANSPORTLINK : (Connections)configObject.getProperty("connectionType")),
                    new ConnectionEndpointsStep(equipmentA, equipmentB)});

        //How the title of the panels should be displayed (by default it says something like "PANEL_NAME wizard STEPX of Y")
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
        //See WizardDescriptor.PROP_AUTO_WIZARD_STYLE documentation for a complete list of things you are enabling here
        wizardDescriptor.putProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE);
        //An image and the list of steps should be shown in a panel on the left side of the wizard?
        wizardDescriptor.putProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE);
        //Should the steps be numbered in the panel on the left side?
        wizardDescriptor.putProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE);
        //The list of steps on the left panel of the wizard
        wizardDescriptor.putProperty(WizardDescriptor.PROP_CONTENT_DATA, new String[] { "Fill in the general information", "Choose the endpoints" });
        
        wizardDescriptor.setTitle("SDH Connection Wizard");
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);

        dialog.setVisible(true);
        dialog.toFront();
        
        //The thread will be blocked either Cancel or Finish is clicked
        if (wizardDescriptor.getValue() == WizardDescriptor.FINISH_OPTION) {
            LocalObjectLight sourcePort = (LocalObjectLight)wizardDescriptor.getProperty("sourcePort");
            LocalObjectLight targetPort = (LocalObjectLight)wizardDescriptor.getProperty("targetPort");
            LocalClassMetadataLight connectionType = (LocalClassMetadataLight)wizardDescriptor.getProperty("connectionType");
            String connectionName = (String)wizardDescriptor.getProperty("connectionName");
            LocalObjectLight newTransportLink = com.createSDHTransportLink(sourcePort, targetPort, connectionType.getClassName(), connectionName);
            if (newTransportLink == null) {
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
                return null;
            } else 
                return newTransportLink;
        } else
            return null;
    }
    
    private class ConnectionGeneralInfoStep implements WizardDescriptor.ValidatingPanel {
        private JComplexDialogPanel thePanel;

        public ConnectionGeneralInfoStep(Connections connection) {
            final JTextField txtConnectionName = new JTextField(20);                 
            txtConnectionName.setName("txtConnectionName"); //NOI18N
            final JComboBox<LocalClassMetadataLight> lstConnectionTypes;
            
            LocalClassMetadataLight[] connectionClasses;
            
            switch (connection) {
                case CONNECTION_TRANSPORTLINK:
                    connectionClasses = com.getLightSubclasses("GenericSDHTransportLink", false, false);
                    break;
                case CONNECTION_CONTAINERLINK:
                    connectionClasses = com.getLightSubclasses("GenericSDHContainerLink", false, false);
                    break;
                case CONNECTION_TRIBUTARYLINK:
                    connectionClasses = com.getLightSubclasses("GenericSDHContainerLink", false, false);
                    break;
                default:
                    connectionClasses = new LocalClassMetadataLight[0];
            }
            
            if (connectionClasses == null )
                lstConnectionTypes = new JComboBox<>();
            else
                lstConnectionTypes = new JComboBox<>(connectionClasses);
            
            lstConnectionTypes.setName("lstConnectionTypes"); //NOI18N
            
            thePanel = new JComplexDialogPanel(new String[] {"Connection name", "Connection type"}, new JComponent[] {txtConnectionName, lstConnectionTypes});
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
        public void readSettings(Object settings) {}

        @Override
        public void storeSettings(Object settings) {
            ((WizardDescriptor)settings).putProperty("connectionName", ((JTextField)thePanel.getComponent("txtConnectionName")).getText());
            ((WizardDescriptor)settings).putProperty("connectionType", ((JComboBox)thePanel.getComponent("lstConnectionTypes")).getSelectedItem());
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
    
    private class ConnectionEndpointsStep implements WizardDescriptor.ValidatingPanel {
        private ExplorablePanel pnlTreeASide;
        private ExplorablePanel pnlTreeBSide;
        private JPanel thePanel;
        private LocalObjectLight sourcePort, targetPort;

        public ConnectionEndpointsStep(LocalObjectLight equipmentA, LocalObjectLight equipmentB) {
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
        public void readSettings(Object settings) {
        }

        @Override
        public void storeSettings(Object settings) {
             ((WizardDescriptor)settings).putProperty("sourcePort", sourcePort);
             ((WizardDescriptor)settings).putProperty("targetPort", targetPort);
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
            if (sourcePort == null || !CommunicationsStub.getInstance().isSubclassOf(sourcePort.getClassName(), Constants.CLASS_GENERICPORT))
                throw new WizardValidationException(pnlTreeASide, "You have to select a source port on the left panel", null);
            
            targetPort = pnlTreeBSide.getLookup().lookup(LocalObjectLight.class);
            if (targetPort == null || !CommunicationsStub.getInstance().isSubclassOf(targetPort.getClassName(), Constants.CLASS_GENERICPORT))
                throw new WizardValidationException(pnlTreeASide, "You have to select a target port on the right panel", null);
        }
    }
    
    public enum Connections {
        CONNECTION_TRANSPORTLINK,
        CONNECTION_CONTAINERLINK,
        CONNECTION_TRIBUTARYLINK
    }
}
