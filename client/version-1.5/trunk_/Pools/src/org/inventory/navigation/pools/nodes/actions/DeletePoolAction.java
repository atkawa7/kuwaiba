/**
 * Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
 *
 * Licensed under the EPL License, Version 1.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.inventory.navigation.pools.nodes.actions;

import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.pools.nodes.PoolNode;
import org.openide.nodes.Node;

/**
 * Deletes a pool
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class DeletePoolAction extends GenericInventoryAction {
    /**
     * Reference to the communications stub singleton
     */
    private CommunicationsStub com;
    /**
     * Reference to the root node;
     */
    private PoolNode node;

    public DeletePoolAction(PoolNode pn){
        putValue(NAME, "Delete Pool");
        com = CommunicationsStub.getInstance();
        this.node = pn;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        
        if (JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this pool? All contained elements will be deleted as well", 
                "Warning", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
        
            if (com.deletePool(node.getPool().getOid())){
                node.getParentNode().getChildren().remove(new Node[]{node});
                NotificationUtil.getInstance().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE, 
                        "Pool deleted successfully");
            }
            else
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
        }
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_POOLS, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
}
