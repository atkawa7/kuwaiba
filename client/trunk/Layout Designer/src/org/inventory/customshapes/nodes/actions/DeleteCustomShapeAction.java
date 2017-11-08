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
 *
 */
package org.inventory.customshapes.nodes.actions;

import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectListItem;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.customshapes.nodes.CustomShapeNode;
import org.inventory.navigation.navigationtree.nodes.AbstractChildren;
import org.openide.util.Utilities;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class DeleteCustomShapeAction extends GenericInventoryAction {
    public DeleteCustomShapeAction() {
        putValue(NAME, I18N.gm("action_name_delete_custom_shape"));
    }    

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_MODELS_LAYOUTS, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        CustomShapeNode node = Utilities.actionsGlobalContext().lookup(CustomShapeNode.class);
        if (node == null)
            return;
        LocalObjectListItem customShape = Utilities.actionsGlobalContext().lookup(LocalObjectListItem.class);
        if (customShape == null)
            return;
        
        if (JOptionPane.showConfirmDialog(null, 
            I18N.gm("confirm_dialog_dlt_custom_shape_message"), 
            I18N.gm("confirm_dialog_dlt_custom_shape_title"), 
            JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            
            if (CommunicationsStub.getInstance().deleteListTypeItem(customShape.getClassName(), customShape.getId(), false)) {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("success"), 
                    NotificationUtil.INFO_MESSAGE, I18N.gm("custom_shape_deleted_successfully"));
                ((AbstractChildren) node.getParentNode().getChildren()).addNotify();
                //Refresh cache
                CommunicationsStub.getInstance().getList(customShape.getClassName(), false, true);
            } else
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                    NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        }
    }
}
