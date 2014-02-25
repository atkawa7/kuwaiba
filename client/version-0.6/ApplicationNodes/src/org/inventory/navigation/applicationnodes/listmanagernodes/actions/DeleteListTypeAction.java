/*
 *  Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>.
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
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.applicationnodes.listmanagernodes.ListTypeItemNode;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectChildren;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Action to delete an a list type item
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public final class DeleteListTypeAction extends AbstractAction {

    private ListTypeItemNode node;
    private LocalObjectLight lol;
    
    public DeleteListTypeAction(ListTypeItemNode node) {
        putValue(NAME, java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_DELETE"));
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,0));
        putValue(MNEMONIC_KEY,KeyEvent.VK_D);
        this.node = node;
    }

    /**
     * Dirty quick fix. This should be the only constructor, and no nodes should be manipulated here
     * @param lol 
     */
    public DeleteListTypeAction(LocalObjectLight lol) {
        putValue(NAME, java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_DELETE"));
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,0));
        putValue(MNEMONIC_KEY,KeyEvent.VK_D);
        this.lol = lol;
    }
    
    @Override
    public void actionPerformed(ActionEvent ev) {

        if(JOptionPane.showConfirmDialog(null, java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_DELETE_LIST_TYPE_ITEM"),
                java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_CONFIRMATION"),JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION){

            NotificationUtil nu = Lookup.getDefault().lookup(NotificationUtil.class);
            if (CommunicationsStub.getInstance().deleteListTypeItem(node == null ?  lol.getClassName() : node.getObject().getClassName(),
                    node == null ? lol.getOid() : node.getObject().getOid(),false)){
                if (node != null)
                    ((ObjectChildren)node.getParentNode().getChildren()).remove(new Node[]{node});
                
                nu.showSimplePopup(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_DELETION_TITLE"), NotificationUtil.INFO, java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_DELETION_TEXT_OK"));
                
                //Refresh cache
                CommunicationsStub.getInstance().getList(node.getObject().getClassName(), false, true);
            }
            else
                nu.showSimplePopup(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_DELETION_TEXT_ERROR"),
                        NotificationUtil.ERROR, CommunicationsStub.getInstance().getError());
        }
    }
}
