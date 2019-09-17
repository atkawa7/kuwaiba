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
package org.inventory.core.contacts.actions;

import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalValidator;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.navigation.navigationtree.nodes.AbstractChildren;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * A custom delete action that deletes a contact
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@ServiceProvider(service=GenericObjectNodeAction.class)
public class DeleteContactAction extends GenericObjectNodeAction {

    public DeleteContactAction() {
        this.putValue(NAME, "Delete Contact"); 
    }
    
    @Override
    public LocalValidator[] getValidators() {
        return null;
    }

    @Override
    public String[] appliesTo() {
        return new String[] { "GenericContact" }; //NOI18N        
    }

    @Override
    public int numberOfNodes() {
        return 1;
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_CONTACTS, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ObjectNode selectedNode = Utilities.actionsGlobalContext().lookup(ObjectNode.class);
               
        if (JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this contact?", 
                    "Delete Contact", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {

            if (CommunicationsStub.getInstance().deleteContact(selectedNode.getObject().getClassName(), selectedNode.getObject().getId())) {
                //If the node is on a tree, update the list
                if (selectedNode.getParentNode() != null && AbstractChildren.class.isInstance(selectedNode.getParentNode().getChildren()))
                    ((AbstractChildren)selectedNode.getParentNode().getChildren()).addNotify();
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("information"), NotificationUtil.INFO_MESSAGE, "The contact was deleted successfully");
            }
            else 
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.INFO_MESSAGE, CommunicationsStub.getInstance().getError());
        }
    }
    
}
