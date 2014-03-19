/*
 *  Copyright 2010-2014 Neotropic SAS <contact@neotropic.co>.
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 */

package org.inventory.core.usermanager.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalUserObject;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.usermanager.UserManagerService;
import org.inventory.core.usermanager.nodes.UserChildren;
import org.inventory.core.usermanager.nodes.UserNode;
import org.openide.nodes.Node;

/**
 * This action adds an user
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class AddUserAction extends AbstractAction{
  
    /**
     * The object used for making the invocations to the web service
     */
    private CommunicationsStub com;
    /**
     * Reference to the UserManagerService useful to refresh the UI.
     * For some reason the calling to the method add() to add a node to the table doesn't
     * show the new node
     */
    private UserManagerService ums;

    public AddUserAction(UserManagerService ums){
        this.com = CommunicationsStub.getInstance();
        this.ums = ums;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        LocalUserObject luo = com.addUser();
        if (luo == null)
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
        else{
            ((UserChildren)ums.getUsersRoot().getChildren()).add(new Node[]{new UserNode(luo)});
            ums.refreshUserList();
            NotificationUtil.getInstance().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE, "User created successfully");
        }
    }
}
