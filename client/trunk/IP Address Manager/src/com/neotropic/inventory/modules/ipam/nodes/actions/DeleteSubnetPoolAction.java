/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>
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
package com.neotropic.inventory.modules.ipam.nodes.actions;

import com.neotropic.inventory.modules.ipam.nodes.SubnetPoolChildren;
import com.neotropic.inventory.modules.ipam.nodes.SubnetPoolNode;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.openide.util.Utilities;

/**
 * Deletes a subnet pool
 * @author Adrian Fernando Molina Fernandez <adrian.martinez@kuwaiba.org>
 */
public class DeleteSubnetPoolAction extends GenericInventoryAction {
    /**
     * Reference to the communications stub singleton
     */
    private CommunicationsStub com;
    private static DeleteSubnetPoolAction instance;
    
    private DeleteSubnetPoolAction(){
        putValue(NAME, I18N.gm("delete"));
        com = CommunicationsStub.getInstance();
    }
    
    public static DeleteSubnetPoolAction getInstance() {
        return instance == null ? instance = new DeleteSubnetPoolAction() : instance;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Iterator selectedNodes = Utilities.actionsGlobalContext().lookupResult(SubnetPoolNode.class).allInstances().iterator();
        SubnetPoolNode selectedNode = null;
        
        if (!selectedNodes.hasNext())
            return;

        while (selectedNodes.hasNext())
            selectedNode = (SubnetPoolNode)selectedNodes.next();
        
        if(selectedNode != null){
            SubnetPoolNode parentNode = (SubnetPoolNode)selectedNode.getParentNode();

            if (com.deleteSubnetPool(selectedNode.getSubnetPool().getOid())){

                ((SubnetPoolChildren)parentNode.getChildren()).addNotify();
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("success"), NotificationUtil.INFO_MESSAGE, 
                        I18N.gm("subnet_pool_deleted"));
            }
            else
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, com.getError());
        }
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_IP_ADDRESS_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
}
