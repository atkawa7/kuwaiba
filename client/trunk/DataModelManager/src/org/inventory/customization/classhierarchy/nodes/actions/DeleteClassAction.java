/*
 *  Copyright 2010-2017, Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.customization.classhierarchy.nodes.actions;

import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadata;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.customization.classhierarchy.nodes.ClassMetadataChildren;
import org.inventory.customization.classhierarchy.nodes.ClassMetadataNode;

/**
 * Action to delete a class metadata
 * @author Adrian Martinez Molina <charles.bedon@kuwaiba.org>
 */
public class DeleteClassAction extends GenericInventoryAction {

    private ClassMetadataNode node;
    private CommunicationsStub com;

    public DeleteClassAction(ClassMetadataNode node) {
        putValue(NAME, I18N.gm("delete_class"));
        com = CommunicationsStub.getInstance();
        this.node = node;
    }
        
    @Override
    public void actionPerformed(ActionEvent ae) {
        LocalClassMetadata classMetaData = com.getMetaForClass(node.getClassMetadata().getClassName(), false);

        if (JOptionPane.showConfirmDialog(null, I18N.gm("confirm_delete_class"), 
                I18N.gm("data_integrity"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION)
            return;
        
        if (com.deleteClassMetadata(classMetaData.getOid())){
            if (node.getParentNode() != null) // null for the class hierarchy view widgets
                ((ClassMetadataChildren)node.getParentNode().getChildren()).refreshList();
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("success"), NotificationUtil.INFO_MESSAGE, I18N.gm("class_deleted"));
        }
        else
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, com.getError());
    }
    
    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_DATA_MODEL_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
}