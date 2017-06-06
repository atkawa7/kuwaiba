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
 *  under the License.
 */
package org.kuwaiba.management.services.nodes.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.kuwaiba.management.services.nodes.CustomerNode;
import org.kuwaiba.management.services.nodes.CustomerPoolNode;
import org.openide.nodes.Node;
import org.openide.util.Utilities;

/**
 * Action to delete a business object
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
class DeleteCustomerAction extends AbstractAction {

    public DeleteCustomerAction() {
        putValue(NAME, "Delete Customer");
    }    
    
    @Override
    public void actionPerformed(ActionEvent ev) {

        if(JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this customer? All services associated will be deleted too",
                "Delete Customer",JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {

            Iterator<? extends CustomerNode> selectedNodes = Utilities.actionsGlobalContext().lookupResult(CustomerNode.class).allInstances().iterator();
            
            if (!selectedNodes.hasNext())
                return;
            
            ArrayList<String> classNames = new ArrayList<>();
            ArrayList<Long> oids = new ArrayList<>();
            HashSet<Node> parents = new HashSet<>();
            
            while (selectedNodes.hasNext()) {
                CustomerNode selectedNode = selectedNodes.next();
                classNames.add(selectedNode.getObject().getClassName());
                oids.add(selectedNode.getObject().getOid());
                parents.add(selectedNode.getParentNode());
            }
                        
            if (CommunicationsStub.getInstance().deleteObjects(classNames, oids)){
                
                for (Node parent : parents)
                    ((CustomerPoolNode.CustomerPoolChildren)parent.getChildren()).addNotify();
                
                NotificationUtil.getInstance().showSimplePopup("Success", 
                        NotificationUtil.INFO_MESSAGE, "The customer was deleted successfully");
            }
            else
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        }
    }
}
