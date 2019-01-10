/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalScriptQuery;
import org.inventory.core.services.api.actions.ComposedAction;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.services.utils.SubMenuDialog;
import org.inventory.core.services.utils.SubMenuItem;
import org.inventory.scriptqueries.nodes.ScriptQueryNode;
import org.openide.util.Utilities;

/**
 * Removes a parameter from a Script Query
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class RemoveParameterFromScriptQueryAction extends GenericInventoryAction implements ComposedAction {
    
    public RemoveParameterFromScriptQueryAction() {
        putValue(NAME, "Remove Parameter...");
    }
    
    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_QUERY_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        HashMap<String, String> parameters = Utilities.actionsGlobalContext().lookup(LocalScriptQuery.class).getParameters();
        if (parameters.isEmpty()) {
            JOptionPane.showMessageDialog(null, "There are no parameters to the selected Script Query", 
                I18N.gm("information"), JOptionPane.INFORMATION_MESSAGE);
            
        } else {
            List<SubMenuItem> subMenuItems = new ArrayList();
            
            for (String parameter : parameters.keySet())
                subMenuItems.add(new SubMenuItem(parameter));
            
            SubMenuDialog.getInstance((String) getValue(NAME), this).showSubmenu(subMenuItems);
        }
    }

    @Override
    public void finalActionPerformed(ActionEvent e) {
        if (e != null && e.getSource() instanceof SubMenuDialog) {
            
            String parameterName = ((SubMenuDialog) e.getSource()).getSelectedSubMenuItem().getCaption();
            
            HashMap<String, String> parameters = new HashMap<>();
            parameters.put(parameterName, null); // "null" means delete it
            
            LocalScriptQuery scriptQuery = Utilities.actionsGlobalContext().lookup(LocalScriptQuery.class);
            
            if (CommunicationsStub.getInstance().updateScriptQueryParameters(scriptQuery.getId(), parameters)) {
                scriptQuery.getParameters().remove(parameterName);
                Utilities.actionsGlobalContext().lookup(ScriptQueryNode.class).resetPropertySheet();
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("information"), 
                    NotificationUtil.INFO_MESSAGE, "Parameter deleted successfully");
                
            } else
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                    NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        }
    }
    
}
