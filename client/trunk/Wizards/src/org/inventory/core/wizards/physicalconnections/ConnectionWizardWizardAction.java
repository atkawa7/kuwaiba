/*
 *  Copyright 2010-2015 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.core.wizards.physicalconnections;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import javax.swing.JComponent;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalObjectListItem;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;

/**
 * Connection wizard
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public final class ConnectionWizardWizardAction implements ActionListener {

    private WizardDescriptor.Panel[] panels;
    private ConnectionWizard myWizard;
    private LocalObjectLight newConnection;

    public ConnectionWizardWizardAction(ConnectionWizard myWizard) {
        this.myWizard = myWizard;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        WizardDescriptor wizardDescriptor = new WizardDescriptor(getPanels());
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
        wizardDescriptor.setTitle("Physical Connections Wizard");
        wizardDescriptor.putProperty("connectionTypeClass",Constants.getConnectionType(myWizard.getConnectionClass())); //NOI18N
        wizardDescriptor.putProperty("wizardType",myWizard.getWizardType()); //NOI18N
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);

        dialog.setVisible(true);
        dialog.toFront();
        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            String aSideClass = (String)wizardDescriptor.getProperty("aSideClass");
            Long aSide = (Long)wizardDescriptor.getProperty("aSide");
            String bSideClass = (String)wizardDescriptor.getProperty("bSideClass");
            Long bSide = (Long)wizardDescriptor.getProperty("bSide");
            String name = (String)wizardDescriptor.getProperty("name");
            LocalObjectListItem type = (LocalObjectListItem)wizardDescriptor.getProperty("type");
            newConnection = CommunicationsStub.getInstance().createPhysicalConnection(
                aSideClass,
                aSide,
                bSideClass,
                bSide,
                (myWizard.getConnectionParent() == null) ? null : myWizard.getConnectionParent().getClassName(),
                (myWizard.getConnectionParent() == null) ? -1 : myWizard.getConnectionParent().getOid(),
                name,
                String.valueOf(type.getOid()),
                myWizard.getConnectionClass());

            if (newConnection != null)
                NotificationUtil.getInstance().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE, "The object was created successfully");
            else
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            
            int numberOfChildren = (Integer)wizardDescriptor.getProperty("numberOfChildren");
            if (numberOfChildren > 0){
                if (CommunicationsStub.getInstance().createBulkPhysicalConnections((String)wizardDescriptor.getProperty("childrenType"),
                        (Integer)wizardDescriptor.getProperty("numberOfChildren"), myWizard.getConnectionClass(), newConnection.getOid()) == null)
                    NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                else
                    NotificationUtil.getInstance().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE, "Children connections were created successfully");
            }
        }
    }

    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel[] getPanels() {
        if (panels == null) {
            panels = new WizardDescriptor.Panel[]{
                        new ConnectionWizardWizardPanel1(myWizard.getASide(), myWizard.getBSide()),
                        new ConnectionWizardWizardPanel2()
                    };
            String[] steps = new String[panels.length];
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent();
                // Default step name to component name of panel. Mainly useful
                // for getting the name of the target chooser to appear in the
                // list of steps.
                steps[i] = c.getName();
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i));
                    // Sets steps names for a panel
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                    // Turn on subtitle creation on each step
                    jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE);
                    // Show steps on the left side with the image on the background
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE);
                    // Turn on numbering of all steps
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE);
                }
            }
        }
        return panels;
    }

    public String getName() {
        return "Physical Connection Wizard";
    }

    public LocalObjectLight getNewConnection() {
        return newConnection;
    }
}
