/**
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.core.containment.nodes.actions;

import java.awt.event.ActionEvent;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.containment.HierarchyCustomizerConfigurationObject;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.containment.nodes.ClassMetadataChildren;
import org.inventory.core.containment.nodes.ClassMetadataNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Implements the "remove a class from container hierarchy" action
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class RemovePosibleChildAction extends GenericInventoryAction {

    ClassMetadataNode node;

    public RemovePosibleChildAction(){}
    public RemovePosibleChildAction(ClassMetadataNode node){
        putValue(NAME, java.util.ResourceBundle.getBundle("org/inventory/core/containment/Bundle").getString("LBL_REMOVE"));
        this.node = node;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        CommunicationsStub com = CommunicationsStub.getInstance();
        
        HierarchyCustomizerConfigurationObject configObj = Lookup.getDefault()
            .lookup(HierarchyCustomizerConfigurationObject.class);
        
        boolean addedChildrenSuccessfully;

        if ((boolean) configObj.getProperty(HierarchyCustomizerConfigurationObject.PROPERTY_ENABLE_SPECIAL)) 
            addedChildrenSuccessfully = com.removePossibleSpecialChildren(((ClassMetadataNode)node.getParentNode()).getObject().getOid(),new long[]{node.getObject().getOid()});
        else 
            addedChildrenSuccessfully = com.removePossibleChildren(((ClassMetadataNode)node.getParentNode()).getObject().getOid(),new long[]{node.getObject().getOid()});
        
        if (addedChildrenSuccessfully){

            ((ClassMetadataChildren)node.getParentNode().getChildren()).remove(new Node[]{ node });
            com.refreshCache(false, false, false, true);

            NotificationUtil.getInstance().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE,
                    java.util.ResourceBundle.getBundle("org/inventory/core/containment/Bundle").getString("LBL_HIERARCHY_UPDATE_TEXT"));
        }
        else
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE,com.getError());
    }
    
    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_CONTAINMENT_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
}