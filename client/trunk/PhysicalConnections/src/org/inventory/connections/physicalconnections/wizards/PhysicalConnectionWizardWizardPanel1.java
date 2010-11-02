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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.interfaces.LocalObject;
import org.inventory.core.services.interfaces.LocalObjectLight;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

public class PhysicalConnectionWizardWizardPanel1 implements WizardDescriptor.ValidatingPanel{

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private Component component;
    private boolean isValid = false;
    private LocalObjectLight aSelection;
    private LocalObjectLight bSelection;
    private String connectionClass;
    private Lookup.Result aResult;
    private Lookup.Result bResult;
    private CommunicationsStub com = CommunicationsStub.getInstance();

    PhysicalConnectionWizardWizardPanel1(LocalObjectLight aSide, LocalObjectLight bSide) {
        component = new PhysicalConnectionWizardVisualPanel1(aSide, bSide);
        aResult = ((PhysicalConnectionWizardVisualPanel1)component).getPnlLeft().getLookup().lookupResult(LocalObjectLight.class);
        aResult.addLookupListener(new LookupListener() {
                        @Override
                        public void resultChanged(LookupEvent ev) {
                            Lookup.Result res = (Lookup.Result)ev.getSource();
                            if (res.allInstances().size() == 1){
                                aSelection = (LocalObjectLight)res.allInstances().iterator().next();
                                if (aSelection == null || bSelection == null)
                                    isValid = false;
                                else{
                                    if (com.getLightMetaForClass(aSelection.getClassName(), false).isPhysicalEndpoint() &&
                                        com.getLightMetaForClass(bSelection.getClassName(), false).isPhysicalEndpoint())
                                        isValid = true;
                                    else
                                        isValid = false;
                                }
                                fireChangeEvent();
                            }
                        }
        });

        bResult= ((PhysicalConnectionWizardVisualPanel1)component).getPnlRight().getLookup().lookupResult(LocalObjectLight.class);
        bResult.addLookupListener(new LookupListener() {
                        @Override
                        public void resultChanged(LookupEvent ev) {
                            Lookup.Result res = (Lookup.Result)ev.getSource();
                            if (res.allInstances().size() == 1){
                                bSelection = (LocalObjectLight)res.allInstances().iterator().next();
                                if (aSelection == null || bSelection == null)
                                    isValid = false;
                                else{
                                    if (com.getLightMetaForClass(aSelection.getClassName(), false).isPhysicalEndpoint() &&
                                        com.getLightMetaForClass(bSelection.getClassName(), false).isPhysicalEndpoint())
                                        isValid = true;
                                    else
                                        isValid = false;
                                }
                                fireChangeEvent();
                            }
                        }
        });
    }

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public Component getComponent() {
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
        return isValid;
    }
    
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1); // or can use ChangeSupport in NB 6.0

    @Override
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    @Override
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    protected final void fireChangeEvent() {
        Iterator<ChangeListener> it;
        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            it.next().stateChanged(ev);
        }
    }
     

    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    @Override
    public void readSettings(Object settings) {
        this.connectionClass = (String)((WizardDescriptor)settings).getProperty("connectionClass");
    }

    @Override
    public void storeSettings(Object settings) {
        LocalObject lo = com.createPhysicalConnection(aSelection.getOid(),
                bSelection.getOid(), connectionClass, null);
        if (lo == null)
            ((WizardDescriptor)settings).putProperty("error", com.getError());
        else
            ((WizardDescriptor)settings).putProperty("object", lo);
    }

    public Result getaResult() {
        return aResult;
    }

    public Result getbResult() {
        return bResult;
    }

    @Override
    public void validate() throws WizardValidationException {
        if (!isValid)
            throw new WizardValidationException(null, "The selected objects are not valid endpoints", null);
    }
    
}
