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
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.customization.classhierarchy.nodes.ClassMetadataChildren;
import org.inventory.customization.classhierarchy.nodes.ClassMetadataNode;

/**
 * Action that requests a metadata class creation
 * @author Adrian Martinez Molina <charles.bedon@kuwaiba.org>
 */
public class CreateClassAction extends GenericInventoryAction {
    
    private ClassMetadataNode node;
    private CommunicationsStub com;

    public CreateClassAction() {
        putValue(NAME, "Create Class");
        com = CommunicationsStub.getInstance();
    }

    public CreateClassAction(ClassMetadataNode node) {
        this();
        this.node = node;
    }
   
    @Override
    public void actionPerformed(ActionEvent ae) {
        String className = JOptionPane.showInputDialog(null, "Please enter the class name");
        
        if (className == null)
            return;
        
        long classId = com.createClassMetadata(className, "","", node.getClassMetadata().getClassName(), 
                true, true, 0, false, true);
        if (classId == -1)
            NotificationUtil.getInstance().showSimplePopup("New Class", NotificationUtil.ERROR_MESSAGE,
                    com.getError());
        else {           
            ((ClassMetadataChildren)node.getChildren()).refreshList();
            NotificationUtil.getInstance().showSimplePopup("New Class", NotificationUtil.INFO_MESSAGE,
                    "Class created successfully");
        }
    }
    
    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_DATA_MODEL_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
 }