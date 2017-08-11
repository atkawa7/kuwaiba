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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import static javax.swing.Action.NAME;
import javax.swing.JOptionPane;
import org.inventory.automation.tasks.nodes.TaskNode;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalTask;
import org.inventory.core.services.api.actions.ComposedAction;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.SubMenuDialog;
import org.inventory.core.services.utils.SubMenuItem;
import org.openide.util.Utilities;

/**
 * Adds a custom parameter to the task
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
class RemoveParameterFromTaskAction extends GenericInventoryAction implements ComposedAction {
    
    RemoveParameterFromTaskAction() {
        putValue(NAME, "Remove Parameter...");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        HashMap<String, String> parameters = Utilities.actionsGlobalContext().lookup(LocalTask.class).getParameters();
        if (parameters.isEmpty()) {
            JOptionPane.showMessageDialog(null, "There are no parameters to the selected task", 
                "Information", JOptionPane.INFORMATION_MESSAGE);
            
        } else {
            List<SubMenuItem> subMenuItems = new ArrayList();
            for (String parameter : parameters.keySet())
                subMenuItems.add(new SubMenuItem(parameter));
            SubMenuDialog.getInstance((String) getValue(NAME), this).showSubmenu(subMenuItems);
        }        
    }
    
    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_TASK_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void finalActionPerformed(ActionEvent e) {
        if (e != null && e.getSource() instanceof SubMenuDialog) {
            CommunicationsStub com = CommunicationsStub.getInstance();

            String parameterName = ((SubMenuDialog) e.getSource()).getSelectedSubMenuItem().getCaption();

            HashMap<String, String> remoteParameters = new HashMap<>();
            remoteParameters.put(parameterName, null); //"null" means delete it

            LocalTask task = Utilities.actionsGlobalContext().lookup (LocalTask.class);

            if (com.updateTaskParameters(task.getId(), remoteParameters)) {
                task.getParameters().remove(parameterName);
                Utilities.actionsGlobalContext().lookup (TaskNode.class).resetPropertySheet();
                NotificationUtil.getInstance().showSimplePopup("Information", NotificationUtil.INFO_MESSAGE, "Parameter deleted successfully");
            } else
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
        }
    }
}
