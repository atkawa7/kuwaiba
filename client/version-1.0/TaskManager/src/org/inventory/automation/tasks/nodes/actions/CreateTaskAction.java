/**
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
package org.inventory.automation.tasks.nodes.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import org.inventory.automation.tasks.nodes.TaskManagerRootNode;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalTask;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.openide.util.Utilities;

/**
 * Creates a task
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
class CreateTaskAction extends AbstractAction {
    
    CreateTaskAction() {
        putValue(NAME, "Create Task");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        CommunicationsStub com = CommunicationsStub.getInstance();
        
        JTextField txtName = new JTextField(10);
        txtName.setName("txtName");
        
        JTextField txtDescription = new JTextField(10);
        txtDescription.setName("txtDescription");
        
        JComplexDialogPanel pnlGeneralInfo = new JComplexDialogPanel(
                                    new String[] { "Name" , "Description" }, new JComponent[] { txtName, txtDescription });
        
        if (JOptionPane.showConfirmDialog(null, pnlGeneralInfo, "New Task", 
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            LocalTask newTask = com.createTask(((JTextField)pnlGeneralInfo.getComponent("txtName")).getText(), 
                    ((JTextField)pnlGeneralInfo.getComponent("txtDescription")).getText(), 
                    true, "", null, null, null);
            
            if (newTask == null)
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
            else {
                ((TaskManagerRootNode.TaskManagerRootChildren)Utilities.actionsGlobalContext().lookup (TaskManagerRootNode.class).getChildren()).addNotify();
                NotificationUtil.getInstance().showSimplePopup("Information", NotificationUtil.INFO_MESSAGE, "Task created successfully");
            }
        }
            
        
    }
    
}
