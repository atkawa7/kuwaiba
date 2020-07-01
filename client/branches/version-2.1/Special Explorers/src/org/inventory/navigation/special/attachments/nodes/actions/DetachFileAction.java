/*
 * Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
 * 
 * Licensed under the EPL License, Version 1.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.inventory.navigation.special.attachments.nodes.actions;

import java.awt.event.ActionEvent;
import java.util.Collection;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalFileObjectLight;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.navigation.special.attachments.nodes.AttachmentsRootNode;
import org.inventory.navigation.special.attachments.nodes.FileObjectNode;
import org.openide.util.Utilities;

/**
 * Detaches a file from an inventory object
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class DetachFileAction extends GenericInventoryAction {

    public DetachFileAction() {
        putValue(NAME, I18N.gm("detach_from_object"));
    }

    
    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_ATTACHMENTS, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        
        if (JOptionPane.showConfirmDialog(null, "Are you sure you want to detach and delete this file?", "Detach File", JOptionPane.OK_OPTION) 
                == JOptionPane.CANCEL_OPTION)
            return;
        
        Collection<? extends FileObjectNode> selectedFileObjectNodes = Utilities.actionsGlobalContext().lookupResult(FileObjectNode.class).allInstances();
        CommunicationsStub com = CommunicationsStub.getInstance();
        
        for (FileObjectNode fileObjectNode : selectedFileObjectNodes) {
            LocalFileObjectLight selectedFileObject = fileObjectNode.getLookup().lookup(LocalFileObjectLight.class);
            LocalObjectLight inventoryObject = fileObjectNode.getParentNode().getLookup().lookup(LocalObjectLight.class);
            if (com.detachFileFromObject(selectedFileObject.getFileOjectId(),  inventoryObject.getClassName(), inventoryObject.getId())) {
                ((AttachmentsRootNode.AttachmentsRootNodeChildren)fileObjectNode.getParentNode().getChildren()).addNotify();
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("information"), NotificationUtil.INFO_MESSAGE, "Files detached and deleted successfully");
            }
            else
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, com.getError());
        }
    }
    
}
