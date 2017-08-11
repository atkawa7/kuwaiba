/*
 *  Copyright 2010-2017, Neotropic SAS <contact@neotropic.co>
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.inventory.customization.classhierarchy.nodes.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalAttributeMetadata;
import org.inventory.communications.core.LocalClassMetadata;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.communications.core.caching.Cache;
import org.inventory.core.services.api.actions.ComposedAction;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.utils.SubMenuDialog;
import org.inventory.core.services.utils.SubMenuItem;
import org.inventory.customization.classhierarchy.nodes.ClassMetadataNode;

/**
 * Deletes an attribute
 * @author Adrian Martinez Molina <charles.bedon@kuwaiba.org>
 */
public class DeleteAttributeAction extends GenericInventoryAction implements ComposedAction {

    private ClassMetadataNode classNode;
    private CommunicationsStub com;

    public DeleteAttributeAction() {
        putValue(NAME, "Delete Attribute...");
        com = CommunicationsStub.getInstance();
    }

    public DeleteAttributeAction(ClassMetadataNode classNode) {
        this();
        this.classNode = classNode;
    }
    
    @Override
    public void actionPerformed(ActionEvent ae) {
        LocalClassMetadata metaForThisClass = com.getMetaForClass(classNode.getClassMetadata().getOid(), false);
        if (metaForThisClass == null) {
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
        } else {
            List<SubMenuItem> subMenuItems = new ArrayList();
            for (LocalAttributeMetadata anAttribute : metaForThisClass.getAttributes())
                subMenuItems.add(new SubMenuItem(anAttribute.getName()));
            SubMenuDialog.getInstance((String) getValue(NAME), this).showSubmenu(subMenuItems);
        }
    }
        
    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_DATA_MODEL_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void finalActionPerformed(ActionEvent e) {
        if (e != null && e.getSource() instanceof SubMenuDialog) {
            if (JOptionPane.showConfirmDialog(null, "Are you sure you want to perform this operation? All subclasses will be modified as well", 
                    "Class metadata operation", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION){
                if (com.deleteAttribute(classNode.getClassMetadata().getOid(), ((SubMenuDialog) e.getSource()).getSelectedSubMenuItem().getCaption())) {
                    NotificationUtil.getInstance().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE, "Attribute deleted successfully");
                    //Force a cache reload
                    Cache.getInstace().resetAll();
                    //Refresh the class node
                    classNode.refresh();
                }
                else
                    NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
            }
        }
    }
}
