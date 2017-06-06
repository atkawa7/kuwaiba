/*
 * Copyright (c) 2016 adrian.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    adrian - initial API and implementation and/or initial documentation
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
        putValue(NAME, java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_DELETE"));
        com = CommunicationsStub.getInstance();
    }
    
    public static DeleteSubnetPoolAction getInstance() {
        return instance == null ? instance = new DeleteSubnetPoolAction() : instance;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Iterator selectedNodes = Utilities.actionsGlobalContext().lookupResult(SubnetPoolNode.class).allInstances().iterator();
        SubnetPoolNode selectedNode = null;
        SubnetPoolNode parentNode = null;
        
        if (!selectedNodes.hasNext())
            return;

        while (selectedNodes.hasNext()) {
            selectedNode = (SubnetPoolNode)selectedNodes.next();
            parentNode = (SubnetPoolNode)selectedNode.getParentNode();
        }
        
        if (com.deleteSubnetPool(selectedNode.getSubnetPool().getOid())){
            
            ((SubnetPoolChildren)parentNode.getChildren()).addNotify();
            NotificationUtil.getInstance().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE, 
                    java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_DELETION_TEXT_OK"));
        }
        else
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_IP_ADDRESS_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
    
    
}
