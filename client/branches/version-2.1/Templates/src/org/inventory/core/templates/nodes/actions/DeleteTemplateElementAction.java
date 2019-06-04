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
import java.util.Iterator;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.services.utils.ImageIconResource;
import org.inventory.core.templates.nodes.TemplateElementNode;
import org.inventory.navigation.navigationtree.nodes.AbstractChildren;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/**
 * Deletes a template element or a template itself
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class DeleteTemplateElementAction extends GenericInventoryAction implements Presenter.Popup{
    public static String ACTION_MAP_KEY = "DeleteTemplateElementAction"; //NOI18N
    private static DeleteTemplateElementAction instance;
    private final JMenuItem popupPresenter;
    
    private final CommunicationsStub com = CommunicationsStub.getInstance();
    
    private DeleteTemplateElementAction() {
        putValue(NAME, "Delete Template Element");
        popupPresenter = new JMenuItem((String) getValue(NAME), ImageIconResource.WARNING_ICON);
        popupPresenter.addActionListener(this);
    }
        
    public static DeleteTemplateElementAction getInstance() {
        return instance == null ? instance = new DeleteTemplateElementAction() : instance;
    }
    
    @Override
    public JMenuItem getPopupPresenter() {
        return popupPresenter;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {       
        if (JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this template element? All its children will be deleted as well.", 
                I18N.gm("warning"), JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {
            
            Iterator<? extends TemplateElementNode> selectedNodes = Utilities.actionsGlobalContext().lookupResult(TemplateElementNode.class).allInstances().iterator();
            
            if (!selectedNodes.hasNext())
                return;
            
            while (selectedNodes.hasNext()) {
                TemplateElementNode selectedNode = selectedNodes.next();
                
                if (CommunicationsStub.getInstance().deleteTemplateElement(
                        selectedNode.getLookup().lookup(LocalObjectLight.class).getClassName(), 
                        selectedNode.getLookup().lookup(LocalObjectLight.class).getId())) {
                    ((AbstractChildren)selectedNode.getParentNode().getChildren()).addNotify();                    
                } else {
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                        NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                    return;
                }
            }
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("success"), 
                NotificationUtil.INFO_MESSAGE, "Template element deleted successfully");
        }
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_TEMPLATES, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
}
