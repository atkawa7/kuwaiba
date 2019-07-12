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
 *
 */
package org.inventory.core.templates.layouts.customshapes.nodes.actions;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.HashMap;
import javax.xml.bind.DatatypeConverter;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.communications.core.LocalObjectListItem;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.util.Constants;
import org.inventory.communications.util.Utils;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.templates.layouts.customshapes.nodes.CustomShapeRootNode;
import org.inventory.navigation.navigationtree.nodes.AbstractChildren;
import org.openide.util.Utilities;

/**
 * This action crates custom shape items
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class CreateCustomShapeAction extends GenericInventoryAction {
    public CreateCustomShapeAction() {
        putValue(NAME, I18N.gm("action_name_create_custom_shape"));
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_TEMPLATES, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
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
            try {
                byte[] byteArray = Utils.getByteArrayFromImage(Utils.createRectangleIcon(Color.BLACK, 33, 33), null);
                
                String byteArrayEncode = DatatypeConverter.printBase64Binary(byteArray);                
////                String iconAttributeValue = "defaultIcon" + ";/;" +  "png" + ";/;" + byteArrayEncode; //NOI18N
////
////                HashMap<String, Object> attributesToUpdate = new HashMap<>();
////                attributesToUpdate.put(Constants.PROPERTY_ICON, iconAttributeValue);
////
////                if(!CommunicationsStub.getInstance().updateObject(loli.getClassName(), loli.getId(), attributesToUpdate)) {
////                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
////                        NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
////                    return;
////                }
            } catch (IOException ex) {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                    NotificationUtil.ERROR_MESSAGE, "The default icon could not be created");
                return;
            }
            ((AbstractChildren) node.getChildren()).addNotify();
            //Refresh cache
            CommunicationsStub.getInstance().getList(customShapeClass.getClassName(), false, true);
        }
    }
}
