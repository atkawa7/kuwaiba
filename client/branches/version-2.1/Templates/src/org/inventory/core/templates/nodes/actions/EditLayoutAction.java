/**
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadata;
import org.inventory.communications.core.LocalObject;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalObjectListItem;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.util.Constants;
import org.inventory.communications.util.Utils;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.templates.layouts.DeviceLayoutEditorTopComponent;
import org.inventory.core.templates.nodes.TemplateElementNode;
import org.openide.util.Utilities;
import org.openide.windows.WindowManager;

/**
 * Shows the associate layout to a given list type item
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class EditLayoutAction extends GenericInventoryAction {
    
    public EditLayoutAction() {
        putValue(NAME, I18N.gm("action_name_edit_layout"));
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_TEMPLATES, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
    
    @Override
    public boolean isEnabled() {
        TemplateElementNode selectedNode = Utilities.actionsGlobalContext().lookup(TemplateElementNode.class);
        if (selectedNode == null)
            return false;
        
        LocalObjectLight selectedObject = selectedNode.getLookup().lookup(LocalObjectLight.class);
        if (selectedObject == null)
            return false;
        
        try {
            return Utils.classMayHaveDeviceLayout(selectedObject.getClassName());
        } catch (Exception ex) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                NotificationUtil.ERROR_MESSAGE, ex.getMessage());
            return false;
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        LocalClassMetadata customShapeClass = CommunicationsStub.getInstance().getMetaForClass(Constants.CLASS_CUSTOMSHAPE, false);
        if (customShapeClass == null) {
            JOptionPane.showMessageDialog(null, I18N.gm("patch_equipment_model_layout"), I18N.gm("error"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        TemplateElementNode selectedNode = Utilities.actionsGlobalContext().lookup(TemplateElementNode.class);
        LocalObjectLight selectedObject = selectedNode.getLookup().lookup(LocalObjectLight.class);
                
        LocalObject templateElement = CommunicationsStub.getInstance().getTemplateElement(selectedObject.getClassName(), selectedObject.getId());
        if (templateElement == null) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            return;
        }
        LocalObjectListItem model = (LocalObjectListItem) templateElement.getAttributes().get(Constants.ATTRIBUTE_MODEL);

        if (model == null) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("warning"), 
                NotificationUtil.WARNING_MESSAGE, "The attribute \"model\" is not set");
            return;
        }
        DeviceLayoutEditorTopComponent topComponent = (DeviceLayoutEditorTopComponent) WindowManager.getDefault().findTopComponent(DeviceLayoutEditorTopComponent.ID + model.getId());

        if (topComponent == null) {
            topComponent = new DeviceLayoutEditorTopComponent(model);
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
