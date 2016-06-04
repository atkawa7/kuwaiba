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

import com.neotropic.inventory.modules.ipam.nodes.SubnetPoolNode;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.openide.nodes.Node;

/**
 *
 * @author adrian
 */
public class DeleteSubnetPoolAction extends AbstractAction{
    /**
     * Reference to the communications stub singleton
     */
    private CommunicationsStub com;
    /**
     * Reference to the root node;
     */
    private SubnetPoolNode node;

    public DeleteSubnetPoolAction(SubnetPoolNode pn){
        putValue(NAME, java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_DELETE"));
        com = CommunicationsStub.getInstance();
        this.node = pn;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (com.deleteSubnetPool(node.getSubnetPool().getOid())){
            node.getParentNode().getChildren().remove(new Node[]{node});
            NotificationUtil.getInstance().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE, 
                    java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_DELETION_TEXT_OK"));
        }
        else
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
    }
    
}
