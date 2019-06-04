/**
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
package com.neotropic.inventory.modules.sdh.actions.generic;

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
 * Deletes a TributaryLink and all its relationships
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@ServiceProvider(service=GenericObjectNodeAction.class)
public class GeneralPurposeDeleteSDHTributaryLink extends GenericObjectNodeAction {

    public GeneralPurposeDeleteSDHTributaryLink() {
        this.putValue(NAME, "Delete Tributary Link"); 
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ObjectNode selectedNode = Utilities.actionsGlobalContext().lookup(ObjectNode.class);
        
        if (selectedNode == null)
            JOptionPane.showMessageDialog(null, "You must select a node first");
        else {
            if (JOptionPane.showConfirmDialog(null, 
                    "This will delete all relevant relationships. \n Are you sure you want to do this?", 
                    "Delete Tributary Link", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {

                if (CommunicationsStub.getInstance().deleteSDHTributaryLink(selectedNode.getObject().getClassName(), selectedNode.getObject().getId())) {
                    //If the node is on a tree, update the list
                    if (selectedNode.getParentNode() != null && AbstractChildren.class.isInstance(selectedNode.getParentNode().getChildren()))
                        ((AbstractChildren)selectedNode.getParentNode().getChildren()).addNotify();
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("information"), NotificationUtil.INFO_MESSAGE, "Tributary link deleted successfully");
                }
                else 
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.INFO_MESSAGE, CommunicationsStub.getInstance().getError());
            }
        }
    }

    @Override
    public LocalValidator[] getValidators() {
        return null;
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_SDH_MODULE, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public String[] appliesTo() {
        return new String[] {Constants.CLASS_GENERICSDHTRIBUTARYLINK};
    }
    
    @Override
    public int numberOfNodes() {
        return 1;
    }
}
    