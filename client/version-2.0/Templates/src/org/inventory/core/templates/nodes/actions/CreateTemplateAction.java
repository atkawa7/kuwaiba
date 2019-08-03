/**
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.inventory.core.templates.nodes.TemplatesModuleClassNode;
import org.inventory.navigation.navigationtree.nodes.AbstractChildren;
import org.openide.util.Utilities;

/**
 * Creates a template
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
class CreateTemplateAction extends GenericInventoryAction {
    
    private final CommunicationsStub com = CommunicationsStub.getInstance();
    
    CreateTemplateAction() {
        putValue(NAME, "New Template");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        
        JTextField txtTemplateName = new JTextField(20);
        txtTemplateName.setName("txtTemplateName"); //NOI18N
                
        JComplexDialogPanel pnlGeneralInfo = new JComplexDialogPanel(
                                    new String[] { "Name" }, new JComponent[] { txtTemplateName });
        
        if (JOptionPane.showConfirmDialog(null, pnlGeneralInfo, "New Template", 
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            
            TemplatesModuleClassNode selectedNode = Utilities.actionsGlobalContext().lookup(TemplatesModuleClassNode.class);
            LocalClassMetadataLight selectedObject = selectedNode.getLookup().lookup(LocalClassMetadataLight.class);
                        
            LocalObjectLight newTemplate = com.createTemplate(selectedObject.getClassName(), 
                ((JTextField)pnlGeneralInfo.getComponent("txtTemplateName")).getText());
            
            if (newTemplate == null)
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
            else {
                ((AbstractChildren)selectedNode.getChildren()).addNotify();
                NotificationUtil.getInstance().showSimplePopup("Information", NotificationUtil.INFO_MESSAGE, "Template created successfully");
            }
        }
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_TEMPLATES, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
}
