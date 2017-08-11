/**
 * Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
 *
 * Licensed under the EPL License, Version 1.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.inventory.models.physicalconnections.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.inventory.models.physicalconnections.wizards.NewContainerWizard;
import org.inventory.models.physicalconnections.wizards.NewLinkWizard;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * Creates a connection between two selected nodes anywhere within the application
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
@ServiceProvider(service=GenericObjectNodeAction.class)
public class CreatePhysicalConnectionAction extends GenericObjectNodeAction {
    
    public CreatePhysicalConnectionAction() {
        putValue(NAME, ResourceBundle.getBundle("org/inventory/models/physicalconnections/Bundle").getString("LBL_CREATE_PHYSICAL_CONNECTION"));
    }

    @Override
    public String getValidator() {
        return null;
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_PHYSICAL_VIEW, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
    
    @Override
    public boolean isEnabled() {
        super.isEnabled();
        return selectedObjects.size() == 2;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Iterator<? extends ObjectNode> endpoints = Utilities.actionsGlobalContext().lookupResult(ObjectNode.class).allInstances().iterator();
        List<ObjectNode> endpointNodes = new ArrayList();

        while(endpoints.hasNext())
            endpointNodes.add(endpoints.next());
                
        LocalObjectLight endpointA = endpointNodes.get(0).getObject();
        LocalObjectLight endpointB = endpointNodes.get(1).getObject();
        LocalObjectLight commonParent = CommunicationsStub.getInstance()
            .getCommonParent(endpointA.getClassName(), endpointA.getOid(), 
                             endpointB.getClassName(), endpointB.getOid());
                
        if (commonParent == null) {
            NotificationUtil.getInstance().showSimplePopup("Error", 
                NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            return;
        }
            
        if (commonParent.getOid() == -1L) {
            JOptionPane.showMessageDialog(null, "Can not create a connection between two nodes whose common parent is the root of the hierarchy", 
                "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
                            
        JComboBox cmbConnectionType = new JComboBox(new String[] {"Container", "Link"});
            
        JComplexDialogPanel connTypeDialog = new JComplexDialogPanel(new String[] {"Connection Type: "}, new JComponent [] {cmbConnectionType});
            
        if (JOptionPane.showConfirmDialog(null, connTypeDialog, (String) getValue(NAME), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {

            if (cmbConnectionType.getSelectedIndex() == 0) 
                new NewContainerWizard(endpointNodes.get(0), endpointNodes.get(1), commonParent).show();
            else 
                new NewLinkWizard(endpointNodes.get(0), endpointNodes.get(1), commonParent).show();
        }
    }
}
