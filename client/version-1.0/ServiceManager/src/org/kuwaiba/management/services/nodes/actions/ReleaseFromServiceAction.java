/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.kuwaiba.management.services.nodes.actions;

import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.List;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.actions.GenericObjectNodeAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.kuwaiba.management.services.nodes.ServiceChildren;
import org.kuwaiba.management.services.nodes.ServiceNode;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.ServiceProvider;

/**
 * This action releases de relationship between the object and the service
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@ServiceProvider(service=GenericObjectNodeAction.class)
public class ReleaseFromServiceAction extends GenericObjectNodeAction implements Presenter.Popup {
    
    @Override
    public void actionPerformed(ActionEvent e) {
        
        Iterator<? extends ObjectNode> selectedNodes = Utilities.actionsGlobalContext().lookupResult(ObjectNode.class).allInstances().iterator();
        
        boolean success = true;
        while (selectedNodes.hasNext()) {
            ObjectNode selectedNode = selectedNodes.next();
            if (CommunicationsStub.getInstance().releaseObjectFromService(selectedNode.getObject().getClassName(), 
                selectedNode.getObject().getOid(), Long.valueOf(((JMenuItem)e.getSource()).getName()))) {
                if (selectedNode.getParentNode() instanceof ServiceNode)
                    ((ServiceChildren)selectedNode.getParentNode().getChildren()).addNotify();
            } else {
                success = false;
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            }
        }
        
        if (success)
            NotificationUtil.getInstance().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE, "The selected resources were released from the service");
    }

    @Override
    public String getValidator() {
        return null; //Enable this action for any object
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenu mnuServices = new JMenu(java.util.ResourceBundle.getBundle("org/kuwaiba/management/services/Bundle").getString("LBL_RELEASE_ELEMENT"));
        mnuServices.setEnabled(false);
        
        Iterator<? extends ObjectNode> selectedNodes = Utilities.actionsGlobalContext().lookupResult(ObjectNode.class).allInstances().iterator();
        
        if (selectedNodes.hasNext()) {
        
            ObjectNode selectedNode = selectedNodes.next(); //Uses the last selected only

            List<LocalObjectLight> services = CommunicationsStub.getInstance().getSpecialAttribute(selectedNode.getObject().getClassName(), 
                    selectedNode.getObject().getOid(), "uses");

            if (services != null) {

                if (!services.isEmpty()) {
                    for (LocalObjectLight service : services){
                        JMenuItem smiServices = new JMenuItem(service.toString());
                        smiServices.setName(String.valueOf(service.getOid()));
                        smiServices.addActionListener(this);
                        mnuServices.add(smiServices);
                    }
                    mnuServices.setEnabled(true);
                }
            } else
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        }
        
        return mnuServices;
    }
    
}