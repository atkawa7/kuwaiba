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
import java.util.Iterator;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalUserGroupObject;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.inventory.core.usermanager.nodes.UserManagerRootNode;
import org.openide.util.Utilities;

/**
 * Creates a group
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
class CreateGroupAction extends GenericInventoryAction {

    public CreateGroupAction() {
        putValue(NAME, "Create Group");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Iterator<? extends UserManagerRootNode> selectedNodes = 
                Utilities.actionsGlobalContext().lookupResult(UserManagerRootNode.class).allInstances().iterator();

        if (!selectedNodes.hasNext())
            return;
        
        UserManagerRootNode selectedNode = selectedNodes.next();
        JTextField txtName = new JTextField();
        txtName.setName("txtName");
        txtName.putClientProperty(JComplexDialogPanel.PROPERTY_MANDATORY, true);
        
        JTextField txtDescription = new JTextField();
        txtDescription.setName("txtDescription");
        
        JComplexDialogPanel pnlNewGroup = new JComplexDialogPanel(new String[] { "Name", "Description" }, 
                new JComponent[] { txtName, txtDescription });
        
        if (JOptionPane.showConfirmDialog(null, pnlNewGroup, "New Group", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            LocalUserGroupObject newGroup = CommunicationsStub.getInstance().createGroup(((JTextField)pnlNewGroup.getComponent("txtName")).getText(), 
                    ((JTextField)pnlNewGroup.getComponent("txtDescription")).getText());
            
            if (newGroup == null)
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            else {       
                ((UserManagerRootNode.GroupChildren)selectedNode.getChildren()).addNotify();
                NotificationUtil.getInstance().showSimplePopup("Information", NotificationUtil.INFO_MESSAGE, "Group created successfully");
            }
        }
    }
    
    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_USER_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
}
