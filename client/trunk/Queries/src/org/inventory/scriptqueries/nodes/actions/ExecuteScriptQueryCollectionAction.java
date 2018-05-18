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
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalScriptQuery;
import org.inventory.communications.core.LocalScriptQueryResult;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.openide.util.Utilities;

/**
 * Executes a Script Query Collection
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ExecuteScriptQueryCollectionAction extends GenericInventoryAction {
    
    public ExecuteScriptQueryCollectionAction() {
        putValue(NAME, "Execute to Collection");
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_QUERY_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        
        LocalScriptQuery scriptQuery = Utilities.actionsGlobalContext().lookup(LocalScriptQuery.class);
        
        LocalScriptQueryResult result  = CommunicationsStub.getInstance().executeScriptQueryCollection(scriptQuery.getId());
        
        if (result != null && result.getResult() != null && result.getResult() instanceof List) {
            
            JPanel pnlResult = new JPanel();
            pnlResult.setLayout(new BoxLayout(pnlResult, BoxLayout.Y_AXIS));
                                    
            for (Object resultItem : (List) result.getResult())
                pnlResult.add(new JLabel(resultItem.toString()));
                                        
            JOptionPane.showConfirmDialog(null, result.getResult().toString(), "Script Query Result", 
                JOptionPane.OK_OPTION);
        }
    }
    
}
