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
import java.util.HashMap;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.inventory.automation.tasks.nodes.TaskNode;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalTask;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/**
 * Adds a custom parameter to the task
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
class RemoveParameterFromTaskAction extends AbstractAction implements Presenter.Popup {
    
    RemoveParameterFromTaskAction() {
        putValue(NAME, "Remove Parameter");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        CommunicationsStub com = CommunicationsStub.getInstance();
        
        String parameterName = ((JMenuItem)e.getSource()).getText();
        
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

    @Override
    public JMenuItem getPopupPresenter() {
        JMenu mnuParameters = new JMenu("Remove Parameter");
        HashMap<String, String> parameters = Utilities.actionsGlobalContext().lookup (LocalTask.class).getParameters();
        if (parameters.isEmpty())
            mnuParameters.setEnabled(false);
        else {
            for (String parameter : parameters.keySet()) {
                JMenuItem mnuParameter  = new JMenuItem(this);
                mnuParameter.setText(parameter);
                mnuParameters.add(mnuParameter);
            }
        }
        return mnuParameters;
    }    
}
