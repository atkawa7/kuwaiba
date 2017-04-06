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
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalUserGroupObject;
import org.inventory.communications.core.LocalUserObject;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.usermanager.nodes.UserNode;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/**
 * Relates a user to an existing group
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
class RelateToGroupAction extends AbstractAction implements Presenter.Popup {
    
    private LocalUserObject currentUser;
    
    public RelateToGroupAction() {
        putValue(NAME, "Relate to Group");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        LocalUserGroupObject destinationGroup = (LocalUserGroupObject)((JMenuItem)e.getSource()).getClientProperty("destinationGroup");
        if (CommunicationsStub.getInstance().addUserToGroup(currentUser.getUserId(), destinationGroup.getId()))
            NotificationUtil.getInstance().showSimplePopup("Information", NotificationUtil.INFO_MESSAGE,
                    String.format("The user %s has been successfully added to group %s", currentUser.toString(), destinationGroup));
        else
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenu mnuGroups = new JMenu();
        mnuGroups.setEnabled(false);
        Iterator<? extends UserNode> selectedNodes = 
                Utilities.actionsGlobalContext().lookupResult(UserNode.class).allInstances().iterator();

        if (selectedNodes.hasNext()) {
            UserNode selectedNode = selectedNodes.next(); //This action will be applied only to the last selected node
            LocalUserGroupObject currentGroup = selectedNode.getParentNode().getLookup().lookup(LocalUserGroupObject.class);
            currentUser = selectedNode.getLookup().lookup(LocalUserObject.class);
            
            List<LocalUserGroupObject> allGroups = CommunicationsStub.getInstance().getGroups();
            for (LocalUserGroupObject aGroup : allGroups) { //We should display only the other groups
                if (aGroup != currentGroup) {
                    JMenuItem mnuGroup = new JMenuItem(aGroup.getName());
                    mnuGroup.putClientProperty("destinationGroup", aGroup);
                    mnuGroups.add(mnuGroup);
                }
            }
            
            if (mnuGroups.getItemCount() != 0)
                mnuGroups.setEnabled(true);
        }
        return mnuGroups;
    }
}
