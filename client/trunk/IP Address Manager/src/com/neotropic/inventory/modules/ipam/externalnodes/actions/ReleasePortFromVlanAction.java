/*
 * Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.inventory.modules.ipam.externalnodes.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import static javax.swing.Action.NAME;
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
 * Releases a relation between a Port and a VLAN
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
@ActionsGroupType(group=ActionsGroupType.Group.RELEASE_FROM)
@ServiceProvider(service=GenericObjectNodeAction.class)
public class ReleasePortFromVlanAction extends GenericObjectNodeAction implements ComposedAction {
    
    public ReleasePortFromVlanAction() {
        putValue(NAME, I18N.gm("release_from_vlan"));
    }
 
      @Override
    public void actionPerformed(ActionEvent e) {
        LocalObjectLight selectedObject = selectedObjects.get(0);
        List<LocalObjectLight> evlans = CommunicationsStub.getInstance().getSpecialAttribute(
            selectedObject.getClassName(), selectedObject.getId(), Constants.RELATIONSHIP_PORT_BELONGS_TO_VLAN);
        
        if (evlans != null) {
            if (evlans.isEmpty()) {
                JOptionPane.showMessageDialog(null, I18N.gm("no_interfaces_related_to_this_object"), 
                    I18N.gm("information"), JOptionPane.INFORMATION_MESSAGE);
            } else {
                List<SubMenuItem> subMenuItems = new ArrayList<>();
                for (LocalObjectLight evlan : evlans) {
                    SubMenuItem subMenuItem = new SubMenuItem(evlan.toString());
                    subMenuItem.addProperty("portId", selectedObject.getId()); //NOI18N
                    subMenuItem.addProperty("evlanId", evlan.getId()); //NOI18N
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
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_NAVIGATION_TREE, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void finalActionPerformed(ActionEvent e) {
        if (e != null && e.getSource() instanceof SubMenuDialog) {
            if(JOptionPane.showConfirmDialog(null, I18N.gm("want_to_release_evlan"), 
                   I18N.gm("warning"),JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                if (CommunicationsStub.getInstance().releasePortFromVLAN(
                    (String) ((SubMenuDialog) e.getSource()).getSelectedSubMenuItem().getProperty("portId"), //NOI18N
                    (String) ((SubMenuDialog) e.getSource()).getSelectedSubMenuItem().getProperty("evlanId")) //NOI18N
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
