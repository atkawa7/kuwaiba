/**
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.inventory.core.templates.nodes.actions;

import java.awt.event.ActionEvent;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObject;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalObjectListItem;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.templates.nodes.TemplateElementNode;
import org.inventory.core.templates.layouts.DeviceLayoutTopComponent;
import org.openide.util.Utilities;
import org.openide.windows.WindowManager;

/**
 * Shows the associate layout to a given list type item
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class AssociateLayoutAction extends GenericInventoryAction {
    
    public AssociateLayoutAction() {
        putValue(NAME, "Associate Layout");
    }
    
    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_TEMPLATES, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        TemplateElementNode selectedNode = Utilities.actionsGlobalContext().lookup(TemplateElementNode.class);
        LocalObjectLight selectedObject = selectedNode.getLookup().lookup(LocalObjectLight.class);
        
        LocalObject templateElement = CommunicationsStub.getInstance().getTemplateElement(selectedObject.getClassName(), selectedObject.getOid());
        if (templateElement == null) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            return;
        }
        
        LocalObjectListItem loli = null;
                
        for (Object attributeValue : templateElement.getAttributes().values()) {            
            if (attributeValue instanceof LocalObjectListItem) {                
                LocalObjectListItem listItem = (LocalObjectListItem) attributeValue;
                
                if (Constants.CLASS_EQUIPMENTMODEL.equals(listItem.getClassName())) {
                    loli = listItem;
                    break;
                }
            }
        }
        if (loli == null) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                NotificationUtil.ERROR_MESSAGE, "The property \"model\" is not set");
            return;
        }
        
        DeviceLayoutTopComponent topComponent = (DeviceLayoutTopComponent) WindowManager.getDefault().findTopComponent(DeviceLayoutTopComponent.ID + loli.getId());
        
        if (topComponent == null) {
            topComponent = new DeviceLayoutTopComponent(loli);
            topComponent.open();
        } else {
            if (topComponent.isOpened())
                topComponent.requestAttention(true);
            else { //Even after closed, the TCs (even the no-singletons) continue to exist in the NBP's PersistenceManager registry, 
                   //so we will reuse the instance, refreshing the vierw first
                topComponent.refresh();
                topComponent.open();
            }
        }
        topComponent.requestActive();
    }    
}
