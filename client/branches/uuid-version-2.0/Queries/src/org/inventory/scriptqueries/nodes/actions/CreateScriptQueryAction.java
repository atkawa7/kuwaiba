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
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.scriptqueries.nodes.ScriptQueryRootChildren;
import org.inventory.scriptqueries.nodes.ScriptQueryRootNode;
import org.openide.util.Utilities;

/**
 * Creates a script query
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class CreateScriptQueryAction extends GenericInventoryAction {
    
    public CreateScriptQueryAction() {
        putValue(NAME, "Create Script Query");
    }
    
    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_QUERY_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        long scriptQueryId = CommunicationsStub.getInstance().createScriptQuery("", "", "", "false", null);
        if (scriptQueryId == -1)
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        else {
            ScriptQueryRootNode node = Utilities.actionsGlobalContext().lookup(ScriptQueryRootNode.class);
            ((ScriptQueryRootChildren) node.getChildren()).addNotify();
            
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("information"), 
                NotificationUtil.INFO_MESSAGE, "Script Query created successfully");
        }
    }
    
}
