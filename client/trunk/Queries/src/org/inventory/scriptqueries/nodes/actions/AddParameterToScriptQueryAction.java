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
package org.inventory.scriptqueries.nodes.actions;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalScriptQuery;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.inventory.scriptqueries.nodes.ScriptQueryNode;
import org.openide.util.Utilities;

/**
 * Adds a parameter to Script Query
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class AddParameterToScriptQueryAction extends GenericInventoryAction {
    
    public AddParameterToScriptQueryAction() {
        putValue(NAME, "Add Parameter...");
    }
    

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_QUERY_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JTextField txtName = new JTextField(10);
        txtName.setName("txtName");
        
        JTextField txtValue = new JTextField(10);
        txtValue.setName("txtValue");
        
        JComplexDialogPanel pnlNewParameter = new JComplexDialogPanel(
            new String[] {"Name", "Value"}, new JComponent[] {txtName, txtValue});
        
        if (JOptionPane.showConfirmDialog(null, pnlNewParameter, "New Parameter", 
            JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            
            String newParameterName = ((JTextField) pnlNewParameter.getComponent("txtName")).getText();
            String newParameterValue = ((JTextField) pnlNewParameter.getComponent("txtValue")).getText();
            
            if (newParameterName.trim().isEmpty() || newParameterValue.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "You have to fill in both fields");
                actionPerformed(e);
                return;                                
            }
            HashMap<String, String> parameters = new HashMap();
            
            LocalScriptQuery scriptQuery = Utilities.actionsGlobalContext().lookup(LocalScriptQuery.class);
            
            for (String parameter : scriptQuery.getParameters().keySet())
                parameters.put(parameter, scriptQuery.getParameters().get(parameter));
            
            parameters.put(newParameterName, newParameterValue);
                                            
            if (CommunicationsStub.getInstance().updateScriptQueryParameters(scriptQuery.getId(), parameters)) {
                scriptQuery.setParameters(parameters);
                Utilities.actionsGlobalContext().lookup(ScriptQueryNode.class).resetPropertySheet();
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("information"), 
                    NotificationUtil.INFO_MESSAGE, "Parameter added successfully");
                
            } else {
                
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                    NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            }
        }
    }
    
}
