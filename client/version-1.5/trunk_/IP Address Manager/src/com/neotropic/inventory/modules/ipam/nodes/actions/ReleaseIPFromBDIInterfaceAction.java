/*
 * Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
 *
 * Licensed under the EPL License, Version 1.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.neotropic.inventory.modules.ipam.nodes.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import static javax.swing.Action.NAME;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.actions.ComposedAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.SubMenuDialog;
import org.inventory.core.services.utils.SubMenuItem;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.openide.util.lookup.ServiceProvider;

/**
 * Release a relation between service instance and an interface
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
@ServiceProvider(service=GenericObjectNodeAction.class)
public class ReleaseIPFromBDIInterfaceAction extends GenericObjectNodeAction implements ComposedAction {
    
    public ReleaseIPFromBDIInterfaceAction() {
        putValue(NAME, ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_RELEASE_INTERFACE"));
    }
       
    @Override
    public void actionPerformed(ActionEvent e) {
        LocalObjectLight selectedObject = selectedObjects.get(0);
        List<LocalObjectLight> bdis = CommunicationsStub.getInstance().getSpecialAttribute(selectedObject.getClassName(), 
            selectedObject.getOid(), Constants.RELATIONSHIP_IPAMPORTRELATEDTOINTERFACE);
        
        if (bdis != null) {
            if (bdis.isEmpty()) {
                JOptionPane.showMessageDialog(null, "There are no Bridge Domain Interfaces related to the selected IP", 
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            } else {
                List<SubMenuItem> subMenuItems = new ArrayList();
                for (LocalObjectLight bdi : bdis) {
                    SubMenuItem subMenuItem = new SubMenuItem(bdi.toString());
                    subMenuItem.addProperty("bdiId", bdi.getOid()); //NOI18N
                    subMenuItem.addProperty("portClassName", selectedObject.getClassName()); //NOI18N
                    subMenuItem.addProperty("portId", selectedObject.getOid()); //NOI18N
                    subMenuItems.add(subMenuItem);
                }
                SubMenuDialog.getInstance((String) getValue(NAME), this).showSubmenu(subMenuItems);
            }
        } else {
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        }
    }
    
    @Override
    public String getValidator() {
        return Constants.VALIDATOR_LOGICAL_SET;
    }
    
    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_IP_ADDRESS_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void finalActionPerformed(ActionEvent e) {
        if (e != null && e.getSource() instanceof SubMenuDialog) {
            SubMenuItem selectedItem = ((SubMenuDialog) e.getSource()).getSelectedSubMenuItem();
            
            if (CommunicationsStub.getInstance().releasePortFromInterface(
                    (String) selectedItem.getProperty("portClassName"), //NOI18N
                    (long) selectedItem.getProperty("portId"), //NOI18N
                    (long) selectedItem.getProperty("bdiId")) //NOI18N 
                    )
                NotificationUtil.getInstance().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE, 
                        java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_SUCCESS"));
            else
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        }
    }
    
}
