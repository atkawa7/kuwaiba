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

import java.util.List;
import javax.swing.event.ChangeListener;
import org.inventory.communications.core.LocalObjectLight;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;

/**
 * Logic of the pre-step of the New Link wizard this step is only shown if there 
 * are already created containers between the two nodes that you are trying to connect.
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class NewLinkWizardPanel0 implements WizardDescriptor.Panel<WizardDescriptor>,
        WizardDescriptor.ValidatingPanel<WizardDescriptor> {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private NewLinkVisualPanel0 component;
    private List<LocalObjectLight> existintWireContainersList;

    public NewLinkWizardPanel0(List<LocalObjectLight> existintWireContainersList) {
        this.existintWireContainersList = existintWireContainersList;
    }

    @Override
    public NewLinkVisualPanel0 getComponent() {
        if (component == null) {
            component = new NewLinkVisualPanel0(existintWireContainersList);
        }
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
        // The Next/Finish button will always be enabled, but to pass to the next/final step, the information will be validated first
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
        if(!getComponent().noContainer()){
            if(getComponent().getSelectedContainer() == null)
                throw new WizardValidationException(component, 
                        "Choose a container or check do not use any container", 
                        "Choose a container or check do not use any container");
        }
    }  

}
