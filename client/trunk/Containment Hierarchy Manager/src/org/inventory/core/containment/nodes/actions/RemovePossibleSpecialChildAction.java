/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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
import org.inventory.core.services.i18n.I18N;
import org.openide.nodes.Node;
import org.openide.util.Utilities;

/**
 * Action to remove a possible special child
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class RemovePossibleSpecialChildAction extends GenericInventoryAction {
    private static RemovePossibleSpecialChildAction instance;
    
    private RemovePossibleSpecialChildAction() {
        putValue(NAME, I18N.gm("remove_special_child"));
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
            long childId = selectedNode.getObject().getId();
            long parentId = ((ClassMetadataNode) selectedNode.getParentNode()).getObject().getId();
            
            if (CommunicationsStub.getInstance().removePossibleSpecialChildren(parentId, new long [] {childId})) {
                ((ClassMetadataSpecialChildren) selectedNode.getParentNode().getChildren()).remove(new Node[] {selectedNode});
                CommunicationsStub.getInstance().refreshCache(false, false, false, false, true);
                
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("success"), NotificationUtil.INFO_MESSAGE,
                    I18N.gm("operation_complete_successfully"));
            } else {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            }
        }
    }
}
