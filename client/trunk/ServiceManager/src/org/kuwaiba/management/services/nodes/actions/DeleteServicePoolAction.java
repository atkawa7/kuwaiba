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
import java.util.Iterator;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.kuwaiba.management.services.nodes.CustomerNode;
import org.kuwaiba.management.services.nodes.CustomerChildren;
import org.kuwaiba.management.services.nodes.ServicePoolNode;
import org.openide.util.Utilities;

/**
 * Action to delete a business object
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
class DeleteServicePoolAction extends AbstractAction {

    public DeleteServicePoolAction() {
        putValue(NAME, "Delete Service Pool");
    }
    
    @Override
    public void actionPerformed(ActionEvent ev) {

        if(JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this pool? All services associated will be deleted too",
                "Delete Service Pool",JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {

            Iterator<? extends ServicePoolNode> selectedNodes = Utilities.actionsGlobalContext().lookupResult(ServicePoolNode.class).allInstances().iterator();
            
            while (selectedNodes.hasNext()) {
                ServicePoolNode selectedNode = selectedNodes.next();
                
                if (!CommunicationsStub.getInstance().deletePool(selectedNode.getPool().getOid()))
                    NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                else
                    ((CustomerChildren)((CustomerNode)selectedNode.getParentNode()).getChildren()).addNotify();
            }               
        }
    }
}
