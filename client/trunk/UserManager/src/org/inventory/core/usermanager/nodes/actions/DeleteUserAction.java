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

package org.inventory.core.usermanager.nodes.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalUserObject;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.usermanager.nodes.GroupNode;
import org.inventory.core.usermanager.nodes.UserNode;
import org.openide.nodes.Node;
import org.openide.util.Utilities;

/**
 * Deletes a user
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class DeleteUserAction extends AbstractAction {

    public DeleteUserAction() {
        putValue(NAME, "Delete Users");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Iterator<? extends UserNode> selectedNodes = 
                Utilities.actionsGlobalContext().lookupResult(UserNode.class).allInstances().iterator();

        if (JOptionPane.showConfirmDialog(null, "Only users not associated to other groups will be deleted. Are you sure you want to do this?", 
                "Delete User", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
        
            List<Long> usersToDelete = new ArrayList<>();
            Node lastSelectedNode = null;
            
            while (selectedNodes.hasNext()) {
                lastSelectedNode = selectedNodes.next();
                usersToDelete.add(lastSelectedNode.getLookup().lookup(LocalUserObject.class).getUserId());
            }
            
            if (!usersToDelete.isEmpty()) {
                if(CommunicationsStub.getInstance().deleteUsers(usersToDelete)) {
                    ((GroupNode.UserChildren)lastSelectedNode.getParentNode().getChildren()).addNotify(); //lastSelectedNode eill never be null in this if
                    NotificationUtil.getInstance().showSimplePopup("Information", NotificationUtil.INFO_MESSAGE, "User deleted successfully");
                } else
                    NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            }
        }
    }
}
