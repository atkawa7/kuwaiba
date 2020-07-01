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
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalUserGroupObject;
import org.inventory.communications.core.LocalUserObject;
import org.inventory.communications.core.LocalUserObjectLight;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.inventory.core.usermanager.nodes.GroupNode;
import org.openide.util.Utilities;

/**
 * Creates a user in the current group
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
class CreateUserAction extends GenericInventoryAction {

    public CreateUserAction() {
        putValue(NAME, "Create User");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Iterator<? extends GroupNode> selectedNodes = 
                Utilities.actionsGlobalContext().lookupResult(GroupNode.class).allInstances().iterator();

        if (!selectedNodes.hasNext())
            return;
        
        GroupNode selectedNode = selectedNodes.next();
        
        JTextField txtUsername = new JTextField();
        txtUsername.setName("txtUsername");
        txtUsername.putClientProperty(JComplexDialogPanel.PROPERTY_MANDATORY, true);
        JTextField txtPassword = new JPasswordField();
        txtPassword.setName("txtPassword");
        txtPassword.putClientProperty(JComplexDialogPanel.PROPERTY_MANDATORY, true);
        JTextField txtFirstName = new JTextField();
        txtFirstName.setName("txtFirstName");
        JTextField txtLastName = new JTextField();
        txtLastName.setName("txtLastName");
        JCheckBox chkEnabled = new JCheckBox();
        chkEnabled.setSelected(true);
        chkEnabled.setName("chkEnabled");
        JComboBox<LocalUserObjectLight.UserType> lstUserTypes = new JComboBox<>(new LocalUserObjectLight.UserType[] { new LocalUserObjectLight.UserType("GUI User", 1), 
                                                                    new LocalUserObjectLight.UserType("Web Service Interface User", 2),
                                                                    new LocalUserObjectLight.UserType("Southbound Interface User", 3) });
        lstUserTypes.setName("lstUserTypes");
        lstUserTypes.setSelectedIndex(0);
        
        JComplexDialogPanel pnlNewUser = new JComplexDialogPanel(new String[] { "Username", "Password", "First Name", "Last Name", "Enabled", "Type" }, 
                new JComponent[]{ txtUsername, txtPassword, txtFirstName, txtLastName, chkEnabled, lstUserTypes });
        
        if (JOptionPane.showConfirmDialog(null, pnlNewUser, "New User", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            LocalUserObject newUser = CommunicationsStub.getInstance().createUser(
                    ((JTextField)pnlNewUser.getComponent("txtUsername")).getText(),
                    ((JTextField)pnlNewUser.getComponent("txtFirstName")).getText(),
                    ((JTextField)pnlNewUser.getComponent("txtLastName")).getText(),
                    new String(((JPasswordField)pnlNewUser.getComponent("txtPassword")).getPassword()),
                    ((JCheckBox)pnlNewUser.getComponent("chkEnabled")).isSelected(),
                    ((LocalUserObjectLight.UserType)((JComboBox<LocalUserObjectLight.UserType>)pnlNewUser.getComponent("lstUserTypes")).getSelectedItem()).getType(),
                    selectedNode.getLookup().lookup(LocalUserGroupObject.class).getId());
            
            if (newUser == null)
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            else {       
                ((GroupNode.UserChildren)selectedNode.getChildren()).addNotify();
                NotificationUtil.getInstance().showSimplePopup("Information", NotificationUtil.INFO_MESSAGE, "User created successfully");
            }
        }
    }
    
    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_USER_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
}
