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
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.communications.core.LocalObjectListItem;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.customshapes.nodes.CustomShapeRootNode;
import org.inventory.navigation.navigationtree.nodes.AbstractChildren;
import org.openide.util.Utilities;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class CreateCustomShapeAction extends GenericInventoryAction {
    public CreateCustomShapeAction() {
        putValue(NAME, I18N.gm("action_name_create_custom_shape"));
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_MODELS_LAYOUTS, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        CustomShapeRootNode node = Utilities.actionsGlobalContext().lookup(CustomShapeRootNode.class);
        if (node == null)
            return;
        
        LocalClassMetadataLight customShapeClass = node.getCustomShapeClass();
        if (customShapeClass == null)
            return;
        
        LocalObjectListItem loli = CommunicationsStub.getInstance().createListTypeItem(customShapeClass.getClassName());
        if (loli == null)
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        else {
            ((AbstractChildren) node.getChildren()).addNotify();
            //Refresh cache
            CommunicationsStub.getInstance().getList(customShapeClass.getClassName(), false, true);
        }
    }
}
