/**
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.inventory.core.containment.nodes.actions;

import java.awt.event.ActionEvent;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.containment.nodes.ClassMetadataNode;
import org.inventory.core.containment.nodes.ClassMetadataSpecialChildren;
import org.inventory.core.containment.nodes.ClassMetadataSpecialNode;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.openide.nodes.Node;
import org.openide.util.Utilities;

/**
 * Action to remove a possible special child
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class RemovePossibleSpecialChildAction extends GenericInventoryAction {
    private static RemovePossibleSpecialChildAction instance;
    
    private RemovePossibleSpecialChildAction() {
        putValue(NAME, java.util.ResourceBundle.getBundle("org/inventory/core/containment/Bundle").getString("LBL_REMOVE_SPECIAL_CHILD"));
    }
    
    public static RemovePossibleSpecialChildAction getInstance() {
        return instance == null ? instance = new RemovePossibleSpecialChildAction() : instance;
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_CONTAINMENT_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ClassMetadataNode selectedNode = Utilities.actionsGlobalContext().lookup(ClassMetadataSpecialNode.class);
        if (selectedNode != null) {
            long childId = selectedNode.getObject().getOid();
            long parentId = ((ClassMetadataNode) selectedNode.getParentNode()).getObject().getOid();
            
            if (CommunicationsStub.getInstance().removePossibleSpecialChildren(parentId, new long [] {childId})) {
                ((ClassMetadataSpecialChildren) selectedNode.getParentNode().getChildren()).remove(new Node[] {selectedNode});
                CommunicationsStub.getInstance().refreshCache(false, false, false, false, true);
                
                NotificationUtil.getInstance().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE,
                    java.util.ResourceBundle.getBundle("org/inventory/core/containment/Bundle").getString("LBL_HIERARCHY_UPDATE_TEXT"));
            } else {
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            }
        }
    }
}
