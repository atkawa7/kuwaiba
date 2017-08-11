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
 *  under the License.
 */
package org.inventory.customization.listmanager.nodes.actions;

import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.customization.listmanager.nodes.ListTypeItemNode;
import org.inventory.navigation.navigationtree.nodes.AbstractChildren;

/**
 * Action to delete an a list type item
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public final class DeleteListTypeAction extends GenericInventoryAction {

    private ListTypeItemNode node;
    
    public DeleteListTypeAction(ListTypeItemNode node) {
        putValue(NAME, "Delete Item");
        this.node = node;
    }
    
    @Override
    public void actionPerformed(ActionEvent ev) {

        if(JOptionPane.showConfirmDialog(null, "Delete Item",
                "Are you sure you want to delete this list type item?",JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION){

            if (CommunicationsStub.getInstance().deleteListTypeItem(node.getObject().getClassName(), node.getObject().getOid(),false)){
                NotificationUtil.getInstance().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE, "List type item deleted successfully");
                ((AbstractChildren)node.getParentNode().getChildren()).addNotify();
                //Refresh cache
                CommunicationsStub.getInstance().getList(node.getObject().getClassName(), false, true);
            }
            else
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        }
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_LIST_TYPE_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
}
