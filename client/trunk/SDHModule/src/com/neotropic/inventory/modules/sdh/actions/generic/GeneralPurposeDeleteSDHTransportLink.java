/**
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
 */
package com.neotropic.inventory.modules.sdh.actions.generic;

import java.awt.event.ActionEvent;
import static javax.swing.Action.NAME;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.actions.GenericObjectNodeAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.applicationnodes.objectnodes.AbstractChildren;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * General purpose version of DeleteSDHTransportLink. Use it to delete transport links outside the SDH module scene.
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@ServiceProvider(service=GenericObjectNodeAction.class)
public class GeneralPurposeDeleteSDHTransportLink extends GenericObjectNodeAction {

    public GeneralPurposeDeleteSDHTransportLink() {
        this.putValue(NAME, "Delete Transport Link"); 
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ObjectNode selectedNode = Utilities.actionsGlobalContext().lookup(ObjectNode.class);

        if (selectedNode == null)
            JOptionPane.showMessageDialog(null, "You must select a node first");
        else {
            if (JOptionPane.showConfirmDialog(null, 
                    "This will delete all the containers and tributary links \n Are you sure you want to do this?", 
                    "Delete Transport Link", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {

                if (CommunicationsStub.getInstance().deleteSDHTransportLink(object.getClassName(), object.getOid())) {
                    //If the node is on a tree, update the list
                    if (selectedNode.getParentNode() != null && AbstractChildren.class.isInstance(selectedNode.getParentNode().getChildren()))
                        ((AbstractChildren)selectedNode.getParentNode().getChildren()).addNotify();
                    
                    NotificationUtil.getInstance().showSimplePopup("Information", NotificationUtil.INFO_MESSAGE, "Transport link deleted successfully");
                }
                else 
                    NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.INFO_MESSAGE, CommunicationsStub.getInstance().getError());

            }
        }
    }

    @Override
    public String getValidator() {
        return "sdhTransportLink";
    }
}
    