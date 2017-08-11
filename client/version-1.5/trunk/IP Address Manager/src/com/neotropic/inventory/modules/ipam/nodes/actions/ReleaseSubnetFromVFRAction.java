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
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import static javax.swing.Action.NAME;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.actions.ComposedAction;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.SubMenuDialog;
import org.inventory.core.services.utils.SubMenuItem;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.openide.util.Utilities;

/**
 * Relates a subnet with a VLAN
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class ReleaseSubnetFromVFRAction extends GenericInventoryAction implements ComposedAction {
    
    private static ReleaseSubnetFromVFRAction instance;

    private ReleaseSubnetFromVFRAction() {
        putValue(NAME, ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_RELEASE_VRF"));
    }
    
    public static ReleaseSubnetFromVFRAction getInstance() {
        return instance == null ? instance = new ReleaseSubnetFromVFRAction() : instance;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Iterator<? extends ObjectNode> selectedNodes = Utilities.actionsGlobalContext().lookupResult(ObjectNode.class).allInstances().iterator();
        if (!selectedNodes.hasNext())
            return;
        
        ObjectNode selectedNode = (ObjectNode) selectedNodes.next();
        
        List<LocalObjectLight> vfrs = CommunicationsStub.getInstance().getSpecialAttribute(selectedNode.getObject().getClassName(), 
            selectedNode.getObject().getOid(), Constants.RELATIONSHIP_IPAMBELONGSTOVRFINSTANCE);
        
        if (vfrs != null) {
            if (vfrs.isEmpty()) {
                JOptionPane.showMessageDialog(null, "There are no VFRs related to the selected Subnet", 
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            } else {
                List<SubMenuItem> subMenuItems = new ArrayList();
                for (LocalObjectLight vfr : vfrs) {
                    SubMenuItem subMenuItem = new SubMenuItem(vfr.toString());
                    subMenuItem.addProperty("subnetId", selectedNode.getObject().getOid()); //NOI18N
                    subMenuItem.addProperty("vfrId", vfr.getOid()); //NOI18N
                    subMenuItems.add(subMenuItem);
                }
                SubMenuDialog.getInstance((String) getValue(NAME), this).showSubmenu(subMenuItems);
            }               
        }
    }
    
    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_IP_ADDRESS_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void finalActionPerformed(ActionEvent e) {
        if (e != null && e.getSource() instanceof SubMenuDialog) {
            if (JOptionPane.showConfirmDialog(null, 
                    "Are you sure you want to delete this relationship?", "Warning", 
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                
                SubMenuItem selectedItem = ((SubMenuDialog) e.getSource()).getSelectedSubMenuItem();
                
                if (CommunicationsStub.getInstance().releaseSubnetFromVFR(
                        (long) selectedItem.getProperty("subnetId"),  //NOI18N
                        (long) selectedItem.getProperty("vfrId"))) //NOI18N
                    NotificationUtil.getInstance().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE, 
                            java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_SUCCESS"));
                else
                    NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            }
        }
    }
}
