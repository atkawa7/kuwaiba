/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
 *  under the License.
 */
package org.kuwaiba.management.services.nodes.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.kuwaiba.management.services.nodes.ServiceNode;
import org.kuwaiba.management.services.nodes.ServicePoolNode;
import org.openide.nodes.Node;
import org.openide.util.Utilities;

/**
 * Action to delete a business object
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
class DeleteServiceAction extends GenericInventoryAction {

    public DeleteServiceAction() {
        putValue(NAME, "Delete Service");
    }
    
    @Override
    public void actionPerformed(ActionEvent ev) {

        if(JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this service? All resources associated will be freed",
                "Delete Service",JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION){

            Iterator<? extends ServiceNode> selectedNodes = Utilities.actionsGlobalContext().lookupResult(ServiceNode.class).allInstances().iterator();
            
            if (!selectedNodes.hasNext())
                return;
            
            ArrayList<String> classNames = new ArrayList<>();
            ArrayList<String> oids = new ArrayList<>();
            HashSet<Node> parents = new HashSet<>();
            
            while (selectedNodes.hasNext()) {
                ServiceNode selectedNode = selectedNodes.next();
                classNames.add(selectedNode.getObject().getClassName());
                oids.add(selectedNode.getObject().getId());
                parents.add(selectedNode.getParentNode());
            }
                        
            if (CommunicationsStub.getInstance().deleteObjects(classNames, oids)){
                
                for (Node parent : parents)
                    ((ServicePoolNode.ServicePoolChildren)parent.getChildren()).addNotify();
                
                NotificationUtil.getInstance().showSimplePopup("Success", 
                        NotificationUtil.INFO_MESSAGE, "The service was deleted successfully");
            }
            else
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        }
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_SERVICE_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
}
