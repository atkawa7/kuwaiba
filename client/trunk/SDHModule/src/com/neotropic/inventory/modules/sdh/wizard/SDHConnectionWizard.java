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
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 * This is the wizard to make SDH connections
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class SDHConnectionWizard {
    private CommunicationsStub com = CommunicationsStub.getInstance();

    public void start(LocalObjectLight aSide, LocalObjectLight bSide) {
        SDHConfigurationObject configObject = Lookup.getDefault().lookup(SDHConfigurationObject.class);
        WizardDescriptor wizardDescriptor = 
                new WizardDescriptor(new WizardDescriptor.Panel[] {
                    new ConnectionGeneralInfoStep(configObject.getProperty("connectionType") == null ? Connections.CONNECTION_TRANSPORTLINK : (Connections)configObject.getProperty("connectionType"))});

        wizardDescriptor.setTitle("SDH Connection Wizard");
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);

        dialog.setVisible(true);
        dialog.toFront();
    }
    
    private class ConnectionGeneralInfoStep implements WizardDescriptor.Panel {
        private JComplexDialogPanel thePanel;

        public ConnectionGeneralInfoStep(Connections connection) {
            JTextField txtConnectionName = new JTextField(20);      
            JComboBox<LocalClassMetadataLight> lstConnectionTypes;
            
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
            
            thePanel = new JComplexDialogPanel(new String[] {"Connection name", "Connection type"}, new JComponent[] {txtConnectionName, lstConnectionTypes});
            thePanel.setSize(500, 500);
            thePanel.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE);
            // Show steps on the left side with the image on the background
            thePanel.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE);
            // Turn on numbering of all stepss
            thePanel.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE);
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
    }
    
    private class ConnectionEndpointsStep extends JPanel {
        private JLabel lblEndpointA;
        private JLabel lblEndpointB;
        private JScrollPane pnlTreeASide;
        private JScrollPane pnlTreeBSide;

        public ConnectionEndpointsStep(LocalObjectLight equipmentA, LocalObjectLight equipmentB) {
            lblEndpointA = new JLabel("Select endpoint A");
            lblEndpointB = new JLabel("Select endpoint B");
            pnlTreeASide = new JScrollPane();
            pnlTreeBSide = new JScrollPane();
            GridLayout layout = new GridLayout(2, 2);
            layout.addLayoutComponent("lblEndpointA", lblEndpointA);
            layout.addLayoutComponent("lblEndpointB", lblEndpointB);
            layout.addLayoutComponent("pnlTreeASide", pnlTreeASide);
            layout.addLayoutComponent("pnlTreeBSide", pnlTreeBSide);
            
            setLayout(layout);
        }
    }
    
    public enum Connections {
        CONNECTION_TRANSPORTLINK,
        CONNECTION_CONTAINERLINK,
        CONNECTION_TRIBUTARYLINK
    }
}
