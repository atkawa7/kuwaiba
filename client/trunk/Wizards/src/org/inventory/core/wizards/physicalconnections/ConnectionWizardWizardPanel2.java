/*
 *  Copyright 2010-2014 Neotropic SAS <contact@neotropic.co>.
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
import java.util.List;
import javax.swing.event.ChangeListener;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.communications.core.LocalObjectListItem;
import org.inventory.communications.util.Constants;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

public class ConnectionWizardWizardPanel2 implements WizardDescriptor.Panel {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private Component component;
    private String connectionTypeClass;
    private CommunicationsStub com = CommunicationsStub.getInstance();

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public Component getComponent() {
        if (component == null) {
            component = new ConnectionWizardVisualPanel2();
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx(SampleWizardPanel1.class);
    }

    @Override
    public boolean isValid() {
        // If it is always OK to press Next or Finish, then:
        return true;
    }

    @Override
    public final void addChangeListener(ChangeListener l) {
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
    }

    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    @Override
    public void readSettings(Object settings) {
        this.connectionTypeClass = (String)((WizardDescriptor)settings).getProperty("connectionTypeClass");
        int wizardType = (Integer)((WizardDescriptor)settings).getProperty("wizardType");
        List<LocalObjectListItem> types = com.getList(connectionTypeClass, true, false);
        if (types != null){
            for(LocalObjectListItem type : types)
                ((ConnectionWizardVisualPanel2)component).getCmbConnectionType().addItem(type);
        }
        if (wizardType == ConnectionWizard.WIZARDTYPE_CONTAINERS){
            LocalClassMetadataLight[] portClasses = com.getLightSubclasses(Constants.CLASS_GENERICPHYSICALLINK, false, false);
            if (portClasses != null){
                for(LocalClassMetadataLight portClass : portClasses)
                    ((ConnectionWizardVisualPanel2)component).getCmbChildrenType().addItem(portClass);
            }
        }else {
            ((ConnectionWizardVisualPanel2)component).getCmbChildrenType().addItem(new LocalClassMetadataLight());
            ((ConnectionWizardVisualPanel2)component).hideLinksRelatedInfo();
        }
    }

    @Override
    public void storeSettings(Object settings) {
        ((WizardDescriptor)settings).putProperty(Constants.PROPERTY_NAME, ((ConnectionWizardVisualPanel2)component).getTxtName().getText());
        ((WizardDescriptor)settings).putProperty(Constants.PROPERTY_TYPE, ((ConnectionWizardVisualPanel2)component).getCmbConnectionType().getSelectedItem());
        ((WizardDescriptor)settings).putProperty("childrenType", ((ConnectionWizardVisualPanel2)component).getCmbChildrenType().getSelectedItem().toString());//NOI18N
        ((WizardDescriptor)settings).putProperty("numberOfChildren", ((ConnectionWizardVisualPanel2)component).getSpnNumberOfChildren().getValue());//NOI18N
        
    }
}