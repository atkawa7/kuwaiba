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

import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;

/**
 * Logic associated to the first step of the New Container wizard
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class NewContainerWizardPanel1 implements WizardDescriptor.Panel<WizardDescriptor>,
        WizardDescriptor.ValidatingPanel<WizardDescriptor> {
 
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private NewContainerVisualPanel1 component;
    
    @Override
    public NewContainerVisualPanel1 getComponent() {
        if (component == null)
            component = new NewContainerVisualPanel1();
        
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public boolean isValid() {
        // The Next/Finish button will always be enabled, but to pass to the next/final step, the information will be validated first
        return true;
    }

    @Override
    public void readSettings(WizardDescriptor wiz) { }

    @Override
    public void storeSettings(WizardDescriptor wiz) { }

    @Override
    public void validate() throws WizardValidationException {
        if (component.getContainerName().trim().isEmpty())
            throw new WizardValidationException(component, "The name of the connection can not be empty", "The name of the connection can not be empty");
    }    

    @Override
    public void addChangeListener(ChangeListener l) { }

    @Override
    public void removeChangeListener(ChangeListener l) { }
}
