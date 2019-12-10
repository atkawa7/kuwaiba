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

import java.awt.Component;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;

/**
 * The actual New Link wizard
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public final class NewLinkWizard {
    
    private ObjectNode aSide;
    private ObjectNode bSide;
    private LocalObjectLight parent;
    private List<LocalObjectLight> newConnections;
    private List<LocalObjectLight> existintWireContainersList;
    
    public NewLinkWizard(ObjectNode aSide, ObjectNode bSide, LocalObjectLight parent, List<LocalObjectLight> existintWireContainersList) {
        this.aSide = aSide;
        this.bSide = bSide;
        this.parent = parent;
        this.existintWireContainersList = existintWireContainersList;
        newConnections = new ArrayList<>();
    }
    
    public void show() {
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>();
        NewLinkWizardPanel1 panel1 = new NewLinkWizardPanel1();
        NewLinkWizardPanel2 panel2 = new NewLinkWizardPanel2(aSide, bSide);
        NewLinkWizardPanel0 panel0 = null;
        if(existintWireContainersList != null && !existintWireContainersList.isEmpty()){
            panel0 = new NewLinkWizardPanel0(existintWireContainersList);
            panels.add(panel0);
        }
        panels.add(panel1);
        panels.add(panel2);
        String[] steps = new String[panels.size()];
        for (int i = 0; i < panels.size(); i++) {
            Component c = panels.get(i).getComponent();
            // Default step name to component name of panel.
            steps[i] = c.getName();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
            }
        }
        
        WizardDescriptor wiz = new WizardDescriptor(new WizardDescriptor.ArrayIterator<>(panels));
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle("New Link");
        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
            List<LocalObjectLight> selectedAEndpoints = panel2.getComponent().getSelectedAEndpoint();
            List<LocalObjectLight> selectedBEndpoints = panel2.getComponent().getSelectedBEndpoint();
            
            if(selectedAEndpoints.size() == 1 && selectedBEndpoints.size() == 1){
                newConnections.add(CommunicationsStub.getInstance().createPhysicalConnection(
                        selectedAEndpoints.get(0).getClassName(), selectedAEndpoints.get(0).getId(),
                    selectedBEndpoints.get(0).getClassName(), selectedBEndpoints.get(0).getId(), 
                    panel1.getComponent().getLinkName(), panel1.getComponent().getLinkClass().getClassName(),
                    panel1.getComponent().dontUseTemplate() || panel1.getComponent().getLinkTemplate() == null 
                            ? null : panel1.getComponent().getLinkTemplate().getId()));
            }
            else if(selectedAEndpoints.size() == selectedBEndpoints.size() && selectedAEndpoints.size() > 1){
                newConnections = CommunicationsStub.getInstance().createPhysicalConnections(
                        selectedAEndpoints, selectedBEndpoints,
                        panel1.getComponent().getLinkName(), panel1.getComponent().getLinkClass().getClassName(),
                        panel1.getComponent().dontUseTemplate() || panel1.getComponent().getLinkTemplate() == null
                                ? null : panel1.getComponent().getLinkTemplate().getId());
            }
            
            if (newConnections == null || newConnections.isEmpty())
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            else
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("success"), NotificationUtil.INFO_MESSAGE, "The link" 
                        + (newConnections.size() == 1 ? "" : "s")  
                        + (newConnections.size() == 1 ? " was" : " were")
                        + " created successfully");
        }
    } 
    
    public List<LocalObjectLight> getNewConnections() {
        return newConnections;
    }
}
