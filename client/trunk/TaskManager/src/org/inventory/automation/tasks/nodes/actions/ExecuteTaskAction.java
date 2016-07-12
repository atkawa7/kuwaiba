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
import org.inventory.automation.tasks.windows.ExecuteTaskResultTopComponent;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalTask;
import org.inventory.communications.core.LocalTaskResult;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.openide.util.Utilities;

/**
 * Executes the selected task
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
class ExecuteTaskAction extends AbstractAction {
    
    public ExecuteTaskAction() {
        putValue(NAME, "Execute Task");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        LocalTask selectedTask = Utilities.actionsGlobalContext().lookup(LocalTask.class);
        LocalTaskResult taskResult = CommunicationsStub.getInstance().executeTask(selectedTask.getId());
        
        if (taskResult == null)
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        else {
            ExecuteTaskResultTopComponent tc = new ExecuteTaskResultTopComponent(taskResult);
            tc.setDisplayName(String.format("Result for task %s", selectedTask.getName()));
            tc.open();
            tc.requestAttention(true);
        }
    }
    
}
