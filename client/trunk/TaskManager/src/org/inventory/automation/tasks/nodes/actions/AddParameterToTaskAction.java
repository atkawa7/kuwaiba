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
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import org.inventory.automation.tasks.nodes.TaskNode;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalTask;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.openide.util.Utilities;

/**
 * Adds a custom parameter to the task
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
class AddParameterToTaskAction extends AbstractAction {
    
    AddParameterToTaskAction() {
        putValue(NAME, "Add Parameter...");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        CommunicationsStub com = CommunicationsStub.getInstance();
        
        JTextField txtName = new JTextField(10);
        txtName.setName("txtName");
        
        JTextField txtValue = new JTextField(10);
        txtValue.setName("txtValue");
        
        JComplexDialogPanel pnlGeneralInfo = new JComplexDialogPanel(
                                    new String[] { "Name" , "Value" }, new JComponent[] { txtName, txtValue });
        
        if (JOptionPane.showConfirmDialog(null, pnlGeneralInfo, "New Parameter", 
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            
            String newParameterName = ((JTextField)pnlGeneralInfo.getComponent("txtName")).getText();            
            String newParameterValue = ((JTextField)pnlGeneralInfo.getComponent("txtValue")).getText();
            
            if (newParameterName.trim().isEmpty() || newParameterValue.trim().isEmpty()){
                JOptionPane.showMessageDialog(null, "You have to fill in both fields");
                actionPerformed(e);
                return;
            }
            
            HashMap<String, String> parameters = new HashMap<>();
            
            LocalTask task = Utilities.actionsGlobalContext().lookup (LocalTask.class);
            
            //Add existing parameters
            for (String existingParameter : task.getParameters().keySet())
                parameters.put(existingParameter, task.getParameters().get(existingParameter));
            
            //Add the one we want to create (or update)
            parameters.put(newParameterName, newParameterValue);
                        
            if (!com.updateTaskParameters(task.getId(), parameters))
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
            else {
                task.setParameters(parameters);
                Utilities.actionsGlobalContext().lookup (TaskNode.class).resetPropertySheet();
                NotificationUtil.getInstance().showSimplePopup("Information", NotificationUtil.INFO_MESSAGE, "Parameter added successfully");
            }
            
        }
            
        
    }
    
}
