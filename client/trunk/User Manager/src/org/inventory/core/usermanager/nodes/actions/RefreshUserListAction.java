/*
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

package org.inventory.core.usermanager.nodes.actions;

import java.awt.event.ActionEvent;
import java.util.Iterator;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.usermanager.nodes.GroupNode;
import org.openide.util.Utilities;

/**
 * Refreshes a group node
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
class RefreshUserListAction extends GenericInventoryAction {

    public RefreshUserListAction() {
        putValue(NAME, "Refresh User List");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Iterator<? extends GroupNode> selectedNodes = 
                Utilities.actionsGlobalContext().lookupResult(GroupNode.class).allInstances().iterator();

        if (!selectedNodes.hasNext())
            return;
        
        ((GroupNode.UserChildren)selectedNodes.next().getChildren()).addNotify();
    }
    
    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_USER_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ);
    }
}
