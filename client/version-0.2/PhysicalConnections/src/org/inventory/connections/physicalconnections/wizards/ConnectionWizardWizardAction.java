/*
 *  Copyright 2010 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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
package org.inventory.connections.physicalconnections.wizards;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.views.LocalEdge;
import org.inventory.core.services.interfaces.LocalObject;
import org.inventory.core.services.interfaces.LocalObjectListItem;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.Lookup;

// An example action demonstrating how the wizard could be called from within
// your code. You can copy-paste the code below wherever you need.
public final class ConnectionWizardWizardAction implements ActionListener {

    private WizardDescriptor.Panel[] panels;
    private ConnectionWizard myWizard;
    private LocalObject newConnection;

    public ConnectionWizardWizardAction(ConnectionWizard myWizard) {
        this.myWizard = myWizard;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        WizardDescriptor wizardDescriptor = new WizardDescriptor(getPanels());
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
        wizardDescriptor.setTitle("Physical Connections Wizard");
        wizardDescriptor.putProperty("connectionTypeClass",LocalEdge.getConnectionType(myWizard.getConnectionClass())); //NOI18N
        wizardDescriptor.putProperty("wizardType",myWizard.getWizardType()); //NOI18N
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);

        dialog.setVisible(true);
        dialog.toFront();
        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            Long aSide = (Long)wizardDescriptor.getProperty("aSide");
            Long bSide = (Long)wizardDescriptor.getProperty("bSide");
            String name = (String)wizardDescriptor.getProperty("name");
            LocalObjectListItem type = (LocalObjectListItem)wizardDescriptor.getProperty("type");
            if (myWizard.getWizardType() == ConnectionWizard.WIZARDTYPE_CONTAINERS)
                newConnection = CommunicationsStub.getInstance().createPhysicalContainerConnection(
                    aSide,
                    bSide,
                    myWizard.getConnectionClass(),
                    myWizard.getConnectionParent());
            else
                newConnection = CommunicationsStub.getInstance().createPhysicalConnection(
                    aSide,
                    bSide,
                    myWizard.getConnectionClass(),
                    myWizard.getConnectionParent());
            if (setConnectionDetails(newConnection.getOid(), name, type))
                JOptionPane.showMessageDialog(null, "The object was successfully created","New Connection",JOptionPane.INFORMATION_MESSAGE);
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

    public LocalObject getNewConnection() {
        return this.newConnection;
    }

    public boolean setConnectionDetails(Long oid, String name, LocalObjectListItem type){
        try{
            LocalObject update = Lookup.getDefault().lookup(LocalObject.class);
            update.setLocalObject(myWizard.getConnectionClass(),
                    new String[]{"name","type"}, new Object[]{name,type.getId()});
            
            update.setOid(oid);
            if(!CommunicationsStub.getInstance().saveObject(update)){
                JOptionPane.showMessageDialog(null, "The object could not be updated \n"+CommunicationsStub.getInstance().getError(),
                        "New Connection",JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
        }catch(Exception e){
            JOptionPane.showMessageDialog(null, "The object could not be updated","New Connection",JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
}
