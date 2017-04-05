/*
 *  Copyright 2010-2016, Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.navigation.applicationnodes.classmetadatanodes.action;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadata;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.applicationnodes.classmetadatanodes.ClassMetadataChildren;
import org.inventory.navigation.applicationnodes.classmetadatanodes.ClassMetadataNode;

/**
 * Action to delete a class metadata
 * @author Adrian Martinez Molina <charles.bedon@kuwaiba.org>
 */
public class DeleteClassAction extends AbstractAction {

    private ClassMetadataNode node;
    private CommunicationsStub com;

    public DeleteClassAction(ClassMetadataNode node) {
        putValue(NAME, java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_DELETE_CLASS"));
        com = CommunicationsStub.getInstance();
        this.node = node;
    }
        
    @Override
    public void actionPerformed(ActionEvent ae) {
        LocalClassMetadata classMetaData = com.getMetaForClass(node.getClassMetadata().getClassName(), false);

        if (JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this class?", 
                "Data Integrity", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION)
            return;
        
        if (com.deleteClassMetadata(classMetaData.getOid())){
            ((ClassMetadataChildren)node.getParentNode().getChildren()).refreshList();
            NotificationUtil.getInstance().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE, "The class was deleted successfully");
        }
        else
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
    }
}