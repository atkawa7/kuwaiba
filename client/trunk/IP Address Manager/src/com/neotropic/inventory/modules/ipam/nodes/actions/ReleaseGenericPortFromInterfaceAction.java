/*
 * Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.services.utils.SubMenuDialog;
import org.inventory.core.services.utils.SubMenuItem;
import org.inventory.navigation.navigationtree.nodes.actions.ActionsGroupType;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.openide.util.lookup.ServiceProvider;

/**
 * Releases a relation between a service instance and an interface
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
@ActionsGroupType(group=ActionsGroupType.Group.RELEASE_FROM)
@ServiceProvider(service=GenericObjectNodeAction.class)
public class ReleaseGenericPortFromInterfaceAction extends GenericObjectNodeAction implements ComposedAction {
    
    public ReleaseGenericPortFromInterfaceAction() {
        putValue(NAME, ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_RELEASE_INTERFACE"));
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        LocalObjectLight selectedObject = selectedObjects.get(0);
        List<LocalObjectLight> interfaces = CommunicationsStub.getInstance().getSpecialAttribute(selectedObject.getClassName(), 
            selectedObject.getId(), Constants.RELATIONSHIP_IPAMPORTRELATEDTOINTERFACE);
        if (interfaces != null) {
            if (interfaces.isEmpty()) {
                JOptionPane.showMessageDialog(null, "There are no interfaces related to the selected port", 
                    I18N.gm("information"), JOptionPane.INFORMATION_MESSAGE);
            } else {
                List<SubMenuItem> subMenuItems = new ArrayList<>();
                for (LocalObjectLight _interface : interfaces) {
                    SubMenuItem subMenuItem = new SubMenuItem(_interface.toString());
                    subMenuItem.addProperty("serviceInstanceId", _interface.getId()); //NOI18N
                    subMenuItem.addProperty("portClassName", selectedObject.getClassName()); //NOI18N
                    subMenuItem.addProperty("portId", selectedObject.getId()); //NOI18N
                    subMenuItems.add(subMenuItem);
                }
                SubMenuDialog.getInstance((String) getValue(NAME), this).showSubmenu(subMenuItems);
            }
        } else {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());            
        }
    }

    @Override
    public String[] getValidators() {
        return null;
    }
    
    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_IP_ADDRESS_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
    
    @Override
    public void finalActionPerformed(ActionEvent e) {
        if (e != null && e.getSource() instanceof SubMenuDialog) {
            if (JOptionPane.showConfirmDialog(null, 
                    "Are you sure you want to release this interface?", I18N.gm("warning"), 
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                
                SubMenuItem selectedInterface = ((SubMenuDialog) e.getSource()).getSelectedSubMenuItem();
                
                if (CommunicationsStub.getInstance().releasePortFromInterface(
                        (String) selectedInterface.getProperty("portClassName"), //NOI18N
                        (long) selectedInterface.getProperty("portId"), //NOI18N
                        (long) selectedInterface.getProperty("serviceInstanceId")) //NOI18N
                    )
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("success"), NotificationUtil.INFO_MESSAGE, 
                            java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_SUCCESS"));
                else
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            }
        }
    }
    
    @Override
    public String[] appliesTo() {
        return new String [] {Constants.CLASS_GENERICPORT};
    }
    
    @Override
    public int numberOfNodes() {
        return -1;
    }
}
