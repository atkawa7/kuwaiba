/*
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
 *  under the License.
 */
package org.inventory.navigation.applicationnodes.listmanagernodes.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.applicationnodes.listmanagernodes.ListTypeItemNode;
import org.inventory.navigation.applicationnodes.objectnodes.AbstractChildren;

/**
 * Action to delete an a list type item
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public final class DeleteListTypeAction extends AbstractAction {

    private ListTypeItemNode node;
    
    public DeleteListTypeAction(ListTypeItemNode node) {
        putValue(NAME, java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_DELETE"));
        this.node = node;
    }
    
    @Override
    public void actionPerformed(ActionEvent ev) {

        if(JOptionPane.showConfirmDialog(null, java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_DELETE_LIST_TYPE_ITEM"),
                java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_CONFIRMATION"),JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION){

            if (CommunicationsStub.getInstance().deleteListTypeItem(node.getObject().getClassName(), node.getObject().getOid(),false)){
                NotificationUtil.getInstance().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE, java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_DELETION_TEXT_OK"));
                ((AbstractChildren)node.getParentNode().getChildren()).addNotify();
                //Refresh cache
                CommunicationsStub.getInstance().getList(node.getObject().getClassName(), false, true);
            }
            else
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        }
    }
}
