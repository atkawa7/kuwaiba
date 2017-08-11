/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
import static javax.swing.Action.NAME;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalUserGroupObject;
import org.inventory.communications.core.LocalUserObject;
import org.inventory.core.services.api.actions.ComposedAction;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.SubMenuDialog;
import org.inventory.core.services.utils.SubMenuItem;
import org.inventory.core.usermanager.nodes.UserNode;
import org.openide.util.Utilities;

/**
 * Relates a user to an existing group
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
class RelateToGroupAction extends GenericInventoryAction implements ComposedAction {
    
    private LocalUserObject currentUser;
    
    public RelateToGroupAction() {
        putValue(NAME, "Relate to Group...");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Iterator<? extends UserNode> selectedNodes = Utilities.actionsGlobalContext()
            .lookupResult(UserNode.class).allInstances().iterator();
        
        if (selectedNodes.hasNext()) {
            UserNode selectedNode = selectedNodes.next(); //This action will be applied only to the last selected node
            LocalUserGroupObject currentGroup = selectedNode.getParentNode().getLookup().lookup(LocalUserGroupObject.class);
            currentUser = selectedNode.getLookup().lookup(LocalUserObject.class);
            
            List<SubMenuItem> subMenuItems = new ArrayList();
            
            List<LocalUserGroupObject> allGroups = CommunicationsStub.getInstance().getGroups();
            if (allGroups == null) {
                NotificationUtil.getInstance().showSimplePopup("Error", 
                    NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                return;
            }
            
            for (LocalUserGroupObject aGroup : allGroups) { //We should display only the other groups
                if (!aGroup.equals(currentGroup)) {
                    SubMenuItem subMenuItem = new SubMenuItem(aGroup.getName());
                    subMenuItem.addProperty("destinationGroup", aGroup); //NOI18N
                    subMenuItems.add(subMenuItem);
                }
            }
            
            if (allGroups.isEmpty() || subMenuItems.isEmpty())
                JOptionPane.showMessageDialog(null, "There are no other groups", "Information", JOptionPane.INFORMATION_MESSAGE);
            else
                SubMenuDialog.getInstance((String) getValue(NAME), this).showSubmenu(subMenuItems);
        }
    }
    
    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_USER_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void finalActionPerformed(ActionEvent e) {
        if (e != null && e.getSource() instanceof SubMenuDialog) {
            LocalUserGroupObject destinationGroup = (LocalUserGroupObject) ((SubMenuDialog) e.getSource()).getSelectedSubMenuItem().getProperty("destinationGroup"); //NOI18N
            if (CommunicationsStub.getInstance().addUserToGroup(currentUser.getId(), destinationGroup.getId()))
                NotificationUtil.getInstance().showSimplePopup("Information", NotificationUtil.INFO_MESSAGE,
                        String.format("The user %s has been successfully added to group %s", currentUser.toString(), destinationGroup));
            else
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        }
    }
}
