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
import org.inventory.core.services.interfaces.LocalObjectLight;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;

// An example action demonstrating how the wizard could be called from within
// your code. You can copy-paste the code below wherever you need.
public final class PhysicalConnectionWizardWizardAction implements ActionListener {

    private WizardDescriptor.Panel[] panels;
    private LocalObjectLight aSide;
    private LocalObjectLight bSide;
    private String connectionClass;

    public PhysicalConnectionWizardWizardAction(LocalObjectLight aObject, LocalObjectLight bObject, String connectionClass) {
        this.aSide = aObject;
        this.bSide = bObject;
        this.connectionClass = connectionClass;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        WizardDescriptor wizardDescriptor = new WizardDescriptor(getPanels());
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
        wizardDescriptor.setTitle("Physical Connections Wizard");
        wizardDescriptor.putProperty("connectionClass", connectionClass);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.setVisible(true);
        dialog.toFront();
        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {

        }
    }

    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel[] getPanels() {
        if (panels == null) {
            panels = new WizardDescriptor.Panel[]{
                        new PhysicalConnectionWizardWizardPanel1(aSide, bSide),
                        new PhysicalConnectionWizardWizardPanel2()
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

    public void setASide(LocalObjectLight object) {
        this.aSide = object;
    }

    public void setBSide(LocalObjectLight object) {
        this.bSide = object;
    }

    public void setConnectionClass(String currentConnectionSelection) {
        connectionClass = currentConnectionSelection;
    }
}
