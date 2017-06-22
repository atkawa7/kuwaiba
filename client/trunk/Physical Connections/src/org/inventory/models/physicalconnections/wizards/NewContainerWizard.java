/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.models.physicalconnections.wizards;

import java.awt.Component;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;

/**
 * The New Container wizard itself
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public final class NewContainerWizard {
    private ObjectNode aSide;
    private ObjectNode bSide;

    public NewContainerWizard(ObjectNode aSide, ObjectNode bSide) {
        this.aSide = aSide;
        this.bSide = bSide;
    }
    
    public void show() {
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>();
        panels.add(new NewContainerWizardPanel1());
        panels.add(new NewContainerWizardPanel2(aSide, bSide));
        String[] steps = new String[panels.size()];
        for (int i = 0; i < panels.size(); i++) {
            Component c = panels.get(i).getComponent();
            steps[i] = c.getName();
            if (c instanceof JComponent) {
                JComponent jc = (JComponent) c;
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
            }
        }
        WizardDescriptor wiz = new WizardDescriptor(new WizardDescriptor.ArrayIterator<>(panels));
        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle("New Container");
        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
            JOptionPane.showMessageDialog(null, "Listoooooo");
        }
    }
}
