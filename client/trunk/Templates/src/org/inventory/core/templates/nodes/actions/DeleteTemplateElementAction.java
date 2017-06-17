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
package org.inventory.core.templates.nodes.actions;

import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.templates.nodes.TemplateElementNode;
import org.inventory.navigation.navigationtree.nodes.AbstractChildren;
import org.openide.util.Utilities;

/**
 * Deletes a template element or a template itself
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
class DeleteTemplateElementAction extends GenericInventoryAction {
    
    private CommunicationsStub com = CommunicationsStub.getInstance();
    
    DeleteTemplateElementAction() {
        putValue(NAME, "Delete Template Element");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {       
        if (JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this template element? All its children will be deleted as well.", 
                "Warning", JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {
            TemplateElementNode selectedNode = Utilities.actionsGlobalContext().lookup(TemplateElementNode.class);
            
            if (CommunicationsStub.getInstance().deleteTemplateElement(
                    selectedNode.getLookup().lookup(LocalObjectLight.class).getClassName(), 
                    selectedNode.getLookup().lookup(LocalObjectLight.class).getOid())) {
                ((AbstractChildren)selectedNode.getParentNode().getChildren()).addNotify();
                NotificationUtil.getInstance().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE, "Template element deleted successfully");
            } else
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        }
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_TEMPLATES, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
}
