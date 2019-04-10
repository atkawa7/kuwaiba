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

package com.neotropic.inventory.modules.ipam.nodes.actions.generic;

import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalValidator;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.navigation.navigationtree.nodes.AbstractChildren;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * Custom action to delete bridge domains. The BridgeDomainInterface instances within will be deleted as well
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@ServiceProvider(service=GenericObjectNodeAction.class)
public class GeneralPurposeDeleteBridgeDomainAction extends GenericObjectNodeAction {

    public GeneralPurposeDeleteBridgeDomainAction() {
        putValue(NAME, "Delete Bridge Domain");
    }
    
    @Override
    public LocalValidator[] getValidators() {
        return null;
    }

    @Override
    public String[] appliesTo() {
        return new String[] { Constants.CLASS_BRIDGEDOMAIN };
    }

    @Override
    public int numberOfNodes() {
        return 1;
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_IP_ADDRESS_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this bridge domain? All bridge domain interfaces will be deleted as well",
                "Delete Bridge Domain",JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            
            ObjectNode selectedNode = Utilities.actionsGlobalContext().lookup(ObjectNode.class);
                        
            if (CommunicationsStub.getInstance().deleteObject(selectedObjects.get(0).getClassName(), selectedObjects.get(0).getId(), true)) {
                //If the node is on a tree, update the list
                if (selectedNode.getParentNode() != null && AbstractChildren.class.isInstance(selectedNode.getParentNode().getChildren()))
                    ((AbstractChildren)selectedNode.getParentNode().getChildren()).addNotify();
                
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("success"), 
                        NotificationUtil.INFO_MESSAGE, "The bridge domain was deleted successfully");
            }
            else
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        }
    }

}
