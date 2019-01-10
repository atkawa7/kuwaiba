/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalUserGroupObject;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.usermanager.nodes.GroupNode;
import org.inventory.core.usermanager.nodes.UserManagerRootNode;
import org.openide.nodes.Node;
import org.openide.util.Utilities;

/**
 * Deletes a group and all the users within <b>that are not related to another group</b>
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
class DeleteGroupAction extends GenericInventoryAction {

    public DeleteGroupAction() {
        putValue(NAME, "Delete Group");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Iterator<? extends GroupNode> selectedNodes = 
                Utilities.actionsGlobalContext().lookupResult(GroupNode.class).allInstances().iterator();

        if (JOptionPane.showConfirmDialog(null, "All the users not associated to other groups will be deleted. Are you sure you want to do this?", 
                "Delete Group", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
        
            List<Long> groupsToDelete = new ArrayList<>();
            Node lastSelectedNode = null;
            
            while (selectedNodes.hasNext()) {
                lastSelectedNode = selectedNodes.next();
                groupsToDelete.add(lastSelectedNode.getLookup().lookup(LocalUserGroupObject.class).getId());
            }
            
            if (!groupsToDelete.isEmpty()) {
                if(CommunicationsStub.getInstance().deleteGroups(groupsToDelete)) {
                    ((UserManagerRootNode.GroupChildren)lastSelectedNode.getParentNode().getChildren()).addNotify(); //lastSelectedNode will never be null in this if
                    NotificationUtil.getInstance().showSimplePopup("Information", NotificationUtil.INFO_MESSAGE, "Group deleted successfully");
                } else
                    NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            }
        }
    }
    
    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_USER_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
}
