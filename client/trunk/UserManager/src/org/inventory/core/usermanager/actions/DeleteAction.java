/*
 *  Copyright 2010-2015 Neotropic SAS <contact@neotropic.co>.
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
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.usermanager.UserManagerService;
import org.inventory.core.usermanager.nodes.GroupNode;
import org.inventory.core.usermanager.nodes.UserNode;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;

public class DeleteAction extends AbstractAction {
    /**
     * Node to be deleted
     */
    private AbstractNode node;

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

    public DeleteAction(AbstractNode node, UserManagerService ums){
        com = CommunicationsStub.getInstance();
        this.node = node;
        this.ums = ums;
        putValue(NAME, "Delete");
    }
    

    @Override
    public void actionPerformed(ActionEvent ev) {

        if (node instanceof UserNode){ //We're gonna delete an user

            if(JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this user?", "Confirmation",JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION)
                return;

            if(com.deleteUsers(new long[]{((UserNode)this.node).getObject().getOid()})){
                NotificationUtil.getInstance().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE, "The user was deleted successfully");
                node.getParentNode().getChildren().remove(new Node[]{node});
                ums.refreshUserList();
            }
            else
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
        }else{ //We're gonna delete a group
            if(JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this group? The associated users won't be deleted if you choose OK","Confirmation",JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION)
                    return;
            if(com.deleteGroups(new long[]{((GroupNode)this.node).getObject().getOid()})){
                NotificationUtil.getInstance().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE, "The group was deleted successfully");
                node.getParentNode().getChildren().remove(new Node[]{node});
                ums.refreshGroupsList();
            }
            else
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
        }
    }
}
