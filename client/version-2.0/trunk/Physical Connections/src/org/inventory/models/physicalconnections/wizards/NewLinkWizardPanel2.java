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

import javax.swing.event.ChangeListener;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.util.Constants;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;

/**
 * Logic of the second step of the New Link wizard
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class NewLinkWizardPanel2 implements WizardDescriptor.Panel<WizardDescriptor>,
        WizardDescriptor.ValidatingPanel<WizardDescriptor> {

    private ObjectNode aSide;
    private ObjectNode bSide;
    
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private NewLinkVisualPanel2 component;
    
    public NewLinkWizardPanel2(ObjectNode aSide, ObjectNode bSide) {
        this.aSide = aSide;
        this.bSide = bSide;
    }

    @Override
    public NewLinkVisualPanel2 getComponent() {
        if (component == null) {
            component = new NewLinkVisualPanel2(aSide, bSide);
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
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
    public void readSettings(WizardDescriptor wiz) { }

    @Override
    public void storeSettings(WizardDescriptor wiz) { }
    
    @Override
    public void validate() throws WizardValidationException {
        if (component.getSelectedAEndpoint() == null || component.getSelectedBEndpoint() == null)
            throw new WizardValidationException(component, "You need to select both sides of the connection", 
                    "You need to select both sides of the connection");
        
        if (!CommunicationsStub.getInstance().isSubclassOf(component.getSelectedAEndpoint().getClassName(), Constants.CLASS_GENERICPORT) || 
                !CommunicationsStub.getInstance().isSubclassOf(component.getSelectedBEndpoint().getClassName(), Constants.CLASS_GENERICPORT))
            throw new WizardValidationException(component, "Only ports can be connected using links", "Only ports can be connected using links");
        
        String endpointConnected = "";
        if (
        !CommunicationsStub.getInstance().getSpecialAttribute(component.getSelectedAEndpoint().getClassName(), component.getSelectedAEndpoint().getId(), "endpointA").isEmpty() ||
        !CommunicationsStub.getInstance().getSpecialAttribute(component.getSelectedAEndpoint().getClassName(), component.getSelectedAEndpoint().getId(), "endpointB").isEmpty()
        ) {
            endpointConnected = String.format("The selected endpoint %s is already connected", component.getSelectedAEndpoint());
        }
        
        if (
        !CommunicationsStub.getInstance().getSpecialAttribute(component.getSelectedBEndpoint().getClassName(), component.getSelectedBEndpoint().getId(), "endpointA").isEmpty() ||
        !CommunicationsStub.getInstance().getSpecialAttribute(component.getSelectedBEndpoint().getClassName(), component.getSelectedBEndpoint().getId(), "endpointB").isEmpty()
        ) {
            if (!"".equals(endpointConnected))
                endpointConnected += ", ";
            endpointConnected += String.format("The selected endpoint %s is already connected", component.getSelectedBEndpoint());
        }
        
        if (!"".equals(endpointConnected))
            throw new WizardValidationException(component, endpointConnected, endpointConnected);
    }

}
