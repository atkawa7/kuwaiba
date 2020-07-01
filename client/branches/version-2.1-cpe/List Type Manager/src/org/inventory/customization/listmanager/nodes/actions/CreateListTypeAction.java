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
package org.inventory.customization.listmanager.nodes.actions;

import java.awt.event.ActionEvent;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectListItem;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.customization.listmanager.nodes.ListTypeNode;
import org.inventory.navigation.navigationtree.nodes.AbstractChildren;

/**
 * Action to create a new list type item
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public final class CreateListTypeAction extends GenericInventoryAction {
    private ListTypeNode node;
    private CommunicationsStub com;

    public CreateListTypeAction(ListTypeNode node) {
        putValue(NAME, "New Item");
        com = CommunicationsStub.getInstance();
        this.node = node;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        LocalObjectListItem myLol = com.createListTypeItem(node.getObject().getClassName());
        if (myLol == null)
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        else {
            ((AbstractChildren)node.getChildren()).addNotify();
            //Refresh cache
            com.getList(node.getObject().getClassName(), false, true);
        }
    }
    
    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_LIST_TYPE_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
}