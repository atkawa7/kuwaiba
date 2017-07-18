/**
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
package org.inventory.automation.tasks.nodes.actions;

import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.JOptionPane;
import org.inventory.automation.tasks.nodes.TaskNode;
import org.inventory.automation.tasks.nodes.TaskUserNode;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalTask;
import org.inventory.communications.core.LocalUserObjectLight;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.openide.util.Utilities;

/**
 * Unsubscribes a user from a task
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
class UnsubscribeUserAction extends GenericInventoryAction {
    
    public UnsubscribeUserAction() {
        putValue(NAME, "Unsubscribe User");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        
        if (JOptionPane.showConfirmDialog(null, "Are you sure you want to unsubscribe this user?", 
                "Unsubscribe User", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            
            TaskUserNode userNode = Utilities.actionsGlobalContext().lookup(TaskUserNode.class);
            TaskNode taskNode = (TaskNode)userNode.getParentNode();
            LocalTask task = taskNode.getLookup().lookup(LocalTask.class);
            
            CommunicationsStub com = CommunicationsStub.getInstance();
            if (com.unsubscribeUserFromTask(userNode.getLookup().lookup(LocalUserObjectLight.class).getId(), 
                    task.getId())) {
                
                NotificationUtil.getInstance().showSimplePopup("User Subscription", NotificationUtil.INFO_MESSAGE, "User subscription canceled");
                
                List<LocalUserObjectLight> subscribers = com.getSubscribersForTask(task.getId());
                
                if (subscribers == null)
                    NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
                else {
                    task.setUsers(subscribers);
                    ((TaskNode.TaskChildren)taskNode.getChildren()).addNotify();
                }
            } else
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
        }
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_TASK_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
}
