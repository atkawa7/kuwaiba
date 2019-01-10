/**
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
package org.inventory.automation.tasks.nodes.actions;

import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.JOptionPane;
import org.inventory.automation.tasks.nodes.TaskNode;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalTask;
import org.inventory.communications.core.LocalUserObject;
import org.inventory.communications.core.LocalUserObjectLight;
import org.inventory.core.services.api.actions.ComposedAction;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.api.windows.SelectValueFrame;
import org.inventory.core.services.i18n.I18N;
import org.openide.util.Utilities;

/**
 * Subscribes a user to a task
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
class SubscribeUserAction extends GenericInventoryAction implements ComposedAction {
    
    public SubscribeUserAction() {
        putValue(NAME, "Subscribe User...");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        CommunicationsStub com = CommunicationsStub.getInstance();
        List<LocalUserObject> users = com.getUsers();
        
        SelectValueFrame frame = new SelectValueFrame("Available Users", "Select a user from the list", "Subscribe", users);
        frame.addListener(this);
        frame.setVisible(true);
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_TASK_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void finalActionPerformed(ActionEvent e) {
        TaskNode selectedTaskNode = Utilities.actionsGlobalContext().lookup(TaskNode.class);
        
        if (e.getSource() instanceof SelectValueFrame) {
            SelectValueFrame frame = (SelectValueFrame) e.getSource();
            Object selectedValue = frame.getSelectedValue();
            
            if (selectedValue == null)
                JOptionPane.showMessageDialog(null, "Select a user from the list");
            else {
                if (CommunicationsStub.getInstance().subscribeUserToTask(
                    ((LocalUserObject) selectedValue).getId(), 
                    selectedTaskNode.getLookup().lookup(LocalTask.class).getId())) {
                    
                    JOptionPane.showMessageDialog(null, "User subscribed successfully");
                    
                    LocalTask task = selectedTaskNode.getLookup().lookup(LocalTask.class);
                    
                    List<LocalUserObjectLight> subscribers = CommunicationsStub.getInstance().getSubscribersForTask(task.getId());
                
                    if (subscribers == null)
                        NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                    else {
                        task.setUsers(subscribers);
                        ((TaskNode.TaskChildren)selectedTaskNode.getChildren()).addNotify();
                    }
                    
                    frame.dispose();
                } else
                    JOptionPane.showMessageDialog(null, CommunicationsStub.getInstance().getError(), 
                        I18N.gm("error"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
