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
import static javax.swing.Action.NAME;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import org.inventory.automation.tasks.nodes.TaskManagerRootNode;
import org.inventory.automation.tasks.nodes.TaskNode;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalTask;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.ImageIconResource;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/**
 * Deletes a task
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
class DeleteTaskAction extends GenericInventoryAction implements Presenter.Popup {
    
    private final JMenuItem popupPresenter;
    
    DeleteTaskAction() {
        putValue(NAME, "Delete Task");
        
        popupPresenter = new JMenuItem((String) getValue(NAME), ImageIconResource.WARNING_ICON);
        popupPresenter.addActionListener(this);
    }
    
    @Override
    public JMenuItem getPopupPresenter() {
        return popupPresenter;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        CommunicationsStub com = CommunicationsStub.getInstance();
        
        TaskNode taskNode = Utilities.actionsGlobalContext().lookup (TaskNode.class);
        
        if (JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this task? All users will be unsubscribed", 
                "Delete Task", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
        
            if (com.deleteTask(taskNode.getLookup().lookup(LocalTask.class).getId())) {
                NotificationUtil.getInstance().showSimplePopup("Information", NotificationUtil.INFO_MESSAGE, "Task deleted successfully");
                ((TaskManagerRootNode.TaskManagerRootChildren)taskNode.getParentNode().getChildren()).addNotify();
            } else
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
        }
        
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_TASK_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
}
