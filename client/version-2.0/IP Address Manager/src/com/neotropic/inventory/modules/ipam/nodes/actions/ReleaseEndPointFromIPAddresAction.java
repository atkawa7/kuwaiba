/*
 * Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalValidator;
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
 * Release a port from an IP address
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
@ActionsGroupType(group=ActionsGroupType.Group.RELEASE_FROM)
@ServiceProvider(service=GenericObjectNodeAction.class)
public class ReleaseEndPointFromIPAddresAction extends GenericObjectNodeAction implements ComposedAction {
    
    public ReleaseEndPointFromIPAddresAction() {
        putValue(NAME, I18N.gm("release_from_ip_address"));
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        LocalObjectLight selectedObject = selectedObjects.get(0);
        List<LocalObjectLight> ipAddresses = CommunicationsStub.getInstance().getSpecialAttribute(
            selectedObject.getClassName(), selectedObject.getId(), Constants.RELATIONSHIP_IPAMHASADDRESS);
        
        if (ipAddresses != null) {
            if (ipAddresses.isEmpty()) {
                JOptionPane.showMessageDialog(null, I18N.gm("no_interfaces_related_to_this_object"), 
                    I18N.gm("information"), JOptionPane.INFORMATION_MESSAGE);
            } else {
                List<SubMenuItem> subMenuItems = new ArrayList<>();
                for (LocalObjectLight ipAddress : ipAddresses) {
                    SubMenuItem subMenuItem = new SubMenuItem(ipAddress.toString());
                    subMenuItem.addProperty("portClassName", selectedObject.getClassName()); //NOI18N
                    subMenuItem.addProperty("portId", selectedObject.getId()); //NOI18N
                    subMenuItem.addProperty("ipAddressId", ipAddress.getId()); //NOI18N
                    subMenuItems.add(subMenuItem);
                }
                SubMenuDialog.getInstance((String) getValue(NAME), this).showSubmenu(subMenuItems);
            }
        } else
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
    }

    @Override
    public LocalValidator[] getValidators() {
        return null;
    }
    
    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_IP_ADDRESS_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void finalActionPerformed(ActionEvent e) {
        if (e != null && e.getSource() instanceof SubMenuDialog) {
            if(JOptionPane.showConfirmDialog(null, I18N.gm("want_to_release_ip_address"), 
                   I18N.gm("warning"),JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                if (CommunicationsStub.getInstance().releasePortFromIPAddress(
                    (String) ((SubMenuDialog) e.getSource()).getSelectedSubMenuItem().getProperty("portClassName"), //NOI18N
                    (String) ((SubMenuDialog) e.getSource()).getSelectedSubMenuItem().getProperty("portId"), //NOI18N
                    (String) ((SubMenuDialog) e.getSource()).getSelectedSubMenuItem().getProperty("ipAddressId")) //NOI18N
                   ) {
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("success"), NotificationUtil.INFO_MESSAGE, 
                        I18N.gm("element_release_successfully"));
                } else
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
           }
        }
    }
    
    @Override
    public String[] appliesTo() {
        return new String [] {Constants.CLASS_GENERICPORT, Constants.CLASS_MPLSTUNNEL};
    }
    
    @Override
    public int numberOfNodes() {
        return -1;
    }
}
