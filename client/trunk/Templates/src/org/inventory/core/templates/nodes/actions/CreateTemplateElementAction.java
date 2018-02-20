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
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.inventory.core.services.utils.MenuScroller;
import org.inventory.core.templates.nodes.TemplateElementNode;
import org.inventory.navigation.navigationtree.nodes.AbstractChildren;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/**
 * Creates a template element
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
class CreateTemplateElementAction extends GenericInventoryAction implements Presenter.Popup {
    
    private final CommunicationsStub com = CommunicationsStub.getInstance();
    
    CreateTemplateElementAction() {
        putValue(NAME, "New Object");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        
        JTextField txtTemplateElementName = new JTextField(20);
        txtTemplateElementName.setName("txtTemplateElementName"); //NOI18N
        
        JComplexDialogPanel pnlGeneralInfo = new JComplexDialogPanel(
                                    new String[] { "Name" }, new JComponent[] { txtTemplateElementName });
        
        if (JOptionPane.showConfirmDialog(null, pnlGeneralInfo, "New Template Element", 
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            
            TemplateElementNode selectedNode = Utilities.actionsGlobalContext().lookup(TemplateElementNode.class);
            LocalObjectLight selectedObject = selectedNode.getLookup().lookup(LocalObjectLight.class);
            
            LocalObjectLight newTemplateElement = com.createTemplateElement(((JMenuItem)e.getSource()).getName(), selectedObject.getClassName(), 
                    selectedObject.getOid(), ((JTextField)pnlGeneralInfo.getComponent("txtTemplateElementName")).getText());
            
            if (newTemplateElement == null)
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, com.getError());
            else {
                ((AbstractChildren)selectedNode.getChildren()).addNotify();
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("information"), NotificationUtil.INFO_MESSAGE, "Template element created successfully");
            }
        }
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenu mnuPossibleChildren = new JMenu((String) getValue(NAME));

        LocalObjectLight selectedObject = Utilities.actionsGlobalContext().lookup(LocalObjectLight.class);
        
        List<LocalClassMetadataLight> items = com.getPossibleChildren(selectedObject.getClassName(), true);
        
        if (items == null) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.INFO_MESSAGE,
                com.getError());
            mnuPossibleChildren.setEnabled(false);
        }
        else {
            if (items.isEmpty())
                mnuPossibleChildren.setEnabled(false);
            else
                for(LocalClassMetadataLight item: items){
                        JMenuItem smiChildren = new JMenuItem(item.getClassName());
                        smiChildren.setName(item.getClassName());
                        smiChildren.addActionListener(this);
                        mnuPossibleChildren.add(smiChildren);
                }

            MenuScroller.setScrollerFor(mnuPossibleChildren, 20, 100);
        }
        return mnuPossibleChildren;
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_TEMPLATES, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
}
