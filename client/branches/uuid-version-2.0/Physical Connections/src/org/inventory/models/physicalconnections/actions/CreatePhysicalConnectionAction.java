/*
 * Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalValidator;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.inventory.models.physicalconnections.wizards.NewContainerWizard;
import org.inventory.models.physicalconnections.wizards.NewLinkWizard;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * Creates a connection between two selected nodes anywhere within the application
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@ServiceProvider(service=GenericObjectNodeAction.class)
public class CreatePhysicalConnectionAction extends GenericObjectNodeAction {
    
    public CreatePhysicalConnectionAction() {
        putValue(NAME, I18N.gm("create_physical_connection"));
    }

    @Override
    public LocalValidator[] getValidators() {
        return null; //Enable this action for any object
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_PHYSICAL_VIEW, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Iterator<? extends ObjectNode> endpoints = Utilities.actionsGlobalContext().lookupResult(ObjectNode.class).allInstances().iterator();
        List<ObjectNode> endpointNodes = new ArrayList<>();
        List<LocalObjectLight> existingWireContainersList;

        while(endpoints.hasNext())
            endpointNodes.add(endpoints.next());
                
        LocalObjectLight endpointA = endpointNodes.get(0).getObject();
        LocalObjectLight endpointB = endpointNodes.get(1).getObject();
        LocalObjectLight commonParent = CommunicationsStub.getInstance()
            .getCommonParent(endpointA.getClassName(), endpointA.getId(), 
                             endpointB.getClassName(), endpointB.getId());
        
        if (commonParent == null) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            return;
        }
            
        if (commonParent.getId() != null && commonParent.getId().equals("-1L")) {
            JOptionPane.showMessageDialog(null, I18N.gm("can_not_create_connection_whose_common_parent_is_root_of_hierarchy"), 
                I18N.gm("information"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        //if there are wirecontainers in both of the two selected nodes 
        existingWireContainersList = CommunicationsStub.getInstance().getContainersBetweenObjects(endpointA.getClassName(), endpointA.getId(), 
                endpointB.getClassName(), endpointB.getId(), Constants.CLASS_WIRECONTAINER);
                            
        JComboBox<String> cmbConnectionType = new JComboBox(new String[] {"Container", "Link"});
            
        JComplexDialogPanel connTypeDialog = new JComplexDialogPanel(new String[] {"Connection Type: "}, new JComponent [] {cmbConnectionType});
            
        if (JOptionPane.showConfirmDialog(null, connTypeDialog, (String) getValue(NAME), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {

            if (cmbConnectionType.getSelectedIndex() == 0) 
                new NewContainerWizard(endpointNodes.get(0), endpointNodes.get(1), commonParent).show();
            else 
                new NewLinkWizard(endpointNodes.get(0), endpointNodes.get(1), commonParent, existingWireContainersList).show();
        }
    }

    @Override
    public String[] appliesTo() {
        return null; //Enable this action for any object
    }
    
    @Override
    public int numberOfNodes() {
        return 2;
    }
}
