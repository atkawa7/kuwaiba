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
package org.inventory.navigation.applicationnodes.objectnodes.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.actions.ObjectAction;
import org.inventory.core.services.api.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.exceptions.ObjectActionException;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Action to delete a business object
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@ServiceProvider(service=ObjectAction.class)
public final class DeleteBusinessObjectAction extends AbstractAction implements ObjectAction {

    private ObjectNode node;

    public DeleteBusinessObjectAction() {
        putValue(NAME, java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_DELETE"));
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,0));
        putValue(MNEMONIC_KEY,KeyEvent.VK_D);
    }


    public DeleteBusinessObjectAction(ObjectNode _node) {
        this();
        this.node = _node;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {

        if(JOptionPane.showConfirmDialog(null, java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_DELETE_BUSINESS_OBJECT"),
                java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_CONFIRMATION"),JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION){

            NotificationUtil nu = Lookup.getDefault().lookup(NotificationUtil.class);
            if (CommunicationsStub.getInstance().deleteObject(node.getObject().getClassName(),
                    node.getObject().getOid())){
                if (node.getParentNode() != null) //Delete can be called for nodes outside the tree structure
                                                  //e.g. In a search result list
                    node.getParentNode().getChildren().remove(new Node[]{node});
                nu.showSimplePopup(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_DELETION_TITLE"), NotificationUtil.INFO, java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_DELETION_TEXT_OK"));
            }
            else
                nu.showSimplePopup(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_DELETION_TEXT_ERROR"),
                        NotificationUtil.ERROR, CommunicationsStub.getInstance().getError());
        }
    }

    @Override
    public void setObject(LocalObjectLight lol) throws ObjectActionException {
        this.node = new ObjectNode(lol);
    }

    @Override
    public int getType() {
        return ObjectAction.DELETE_BUSINESS_OBJECT;
    }
}
