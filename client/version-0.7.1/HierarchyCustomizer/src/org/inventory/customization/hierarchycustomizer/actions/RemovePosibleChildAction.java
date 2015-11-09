/**
 *  Copyright 2010-2015 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.customization.hierarchycustomizer.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.customization.hierarchycustomizer.nodes.ClassMetadataChildren;
import org.inventory.customization.hierarchycustomizer.nodes.ClassMetadataNode;
import org.openide.nodes.Node;

/**
 * Implements the "remove a class from container hierarchy" action
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class RemovePosibleChildAction extends AbstractAction{

    ClassMetadataNode node;

    public RemovePosibleChildAction(){}
    public RemovePosibleChildAction(ClassMetadataNode node){
        putValue(NAME, java.util.ResourceBundle.getBundle("org/inventory/customization/hierarchycustomizer/Bundle").getString("LBL_REMOVE"));
        this.node = node;
    }

    public void actionPerformed(ActionEvent e) {
        CommunicationsStub com = CommunicationsStub.getInstance();
        if (com.removePossibleChildren(
                ((ClassMetadataNode)node.getParentNode()).getObject().getOid(),new long[]{node.getObject().getOid()})){

            ((ClassMetadataChildren)node.getParentNode().getChildren()).remove(new Node[]{node});
            com.refreshCache(false, false, false, true);

            NotificationUtil.getInstance().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE,
                    java.util.ResourceBundle.getBundle("org/inventory/customization/hierarchycustomizer/Bundle").getString("LBL_HIERARCHY_UPDATE_TEXT"));
        }
        else
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE,com.getError());
    }
}