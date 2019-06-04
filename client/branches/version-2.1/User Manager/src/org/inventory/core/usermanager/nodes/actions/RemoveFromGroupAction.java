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
import java.util.Iterator;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalUserGroupObject;
import org.inventory.communications.core.LocalUserObject;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.usermanager.nodes.GroupNode;
import org.inventory.core.usermanager.nodes.UserNode;
import org.openide.util.Utilities;

/**
 * Removes a user from a group (provided that the user is already related to at least one other group)
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class RemoveFromGroupAction extends GenericInventoryAction {

    public RemoveFromGroupAction() {
        putValue(NAME, "Remove from Group");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Iterator<? extends UserNode> selectedNodes = 
                Utilities.actionsGlobalContext().lookupResult(UserNode.class).allInstances().iterator();

        if (JOptionPane.showConfirmDialog(null, "Only users already associated to other groups will be released. Are you sure you want to do this?", 
                "Remove User from Group", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
        
            while (selectedNodes.hasNext()) {
                UserNode userNode = selectedNodes.next();
                GroupNode groupNode =  (GroupNode)userNode.getParentNode();
                
                if(CommunicationsStub.getInstance().removeUserFromGroup(userNode.getLookup().lookup(LocalUserObject.class).getId(),
                        groupNode.getLookup().lookup(LocalUserGroupObject.class).getId())) {
                    ((GroupNode.UserChildren)groupNode.getChildren()).addNotify();
                    NotificationUtil.getInstance().showSimplePopup("Information", NotificationUtil.INFO_MESSAGE, "User successfully removed from group");
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
