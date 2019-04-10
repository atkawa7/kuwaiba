/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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

import java.util.ArrayList;
import javax.swing.event.ChangeListener;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.util.Constants;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;

/**
 * Logic associated to the first step of the New Container wizard
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class NewContainerWizardPanel2 implements WizardDescriptor.Panel<WizardDescriptor>,
        WizardDescriptor.ValidatingPanel<WizardDescriptor> {

    private ObjectNode aSide;
    private ObjectNode bSide;
    
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private NewContainerVisualPanel2 component;

    public NewContainerWizardPanel2(ObjectNode aSide, ObjectNode bSide) {
        this.aSide = aSide;
        this.bSide = bSide;
    }
    
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public NewContainerVisualPanel2 getComponent() {
        if (component == null)
            component = new NewContainerVisualPanel2(aSide, bSide);
        
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx("help.key.here");
    }

    @Override
    public boolean isValid() {
        // If it is always OK to press Next or Finish, then:
        return true;
        // If it depends on some condition (form filled out...) and
        // this condition changes (last form field filled in...) then
        // use ChangeSupport to implement add/removeChangeListener below.
        // WizardDescriptor.ERROR/WARNING/INFORMATION_MESSAGE will also be useful.
    }

    private final ArrayList<ChangeListener> listeners = new ArrayList<>();
    
    @Override
    public void addChangeListener(ChangeListener l) {
        synchronized(listeners) {
            listeners.add(l);
        }
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        synchronized(listeners) {
            listeners.remove(l);
        }
    }

    @Override
    public void readSettings(WizardDescriptor wiz) { }

    @Override
    public void storeSettings(WizardDescriptor wiz) {  }

    @Override
    public void validate() throws WizardValidationException {
        if (component.getSelectedAEndpoint() == null || component.getSelectedBEndpoint() == null)
            throw new WizardValidationException(component, "You need to select both sides of the connection", 
                    "You need to select both sides of the connection");
          if (CommunicationsStub.getInstance().isSubclassOf(component.getSelectedAEndpoint().getClassName(), Constants.CLASS_GENERICPORT) || 
                CommunicationsStub.getInstance().isSubclassOf(component.getSelectedBEndpoint().getClassName(), Constants.CLASS_GENERICPORT))
            throw new WizardValidationException(component, "Ports can not be connected using containers", "Ports can not be connected using containers");
    }
}
