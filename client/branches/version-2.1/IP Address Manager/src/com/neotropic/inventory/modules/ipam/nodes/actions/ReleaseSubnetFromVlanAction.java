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
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
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
 * Releases a relation between a VRF and a VLAN
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class ReleaseSubnetFromVlanAction  extends GenericInventoryAction implements ComposedAction {
    
    private static ReleaseSubnetFromVlanAction instance;
    
    private ReleaseSubnetFromVlanAction() { 
        putValue(NAME, ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_RELEASE_VLAN"));
    }
    
    public static ReleaseSubnetFromVlanAction getInstance() {
        return instance == null ? instance = new ReleaseSubnetFromVlanAction() : instance;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Iterator<? extends ObjectNode> selectedNodes = Utilities.actionsGlobalContext().lookupResult(ObjectNode.class).allInstances().iterator();
        
        if (!selectedNodes.hasNext())
            return;
        
        ObjectNode selectedNode = (ObjectNode)selectedNodes.next();
        
        List<LocalObjectLight> vlans = CommunicationsStub.getInstance().getSpecialAttribute(selectedNode.getObject().getClassName(), 
                selectedNode.getObject().getId(), Constants.RELATIONSHIP_IPAMBELONGSTOVLAN);
        
        if (vlans != null) {
            if (vlans.isEmpty())
                JOptionPane.showMessageDialog(null, "There are no VLANs related to the selected Subnet", 
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            else {
                List<SubMenuItem> subMenuItems = new ArrayList<>();
                for (LocalObjectLight vlan : vlans){
                    SubMenuItem subMenuItem = new SubMenuItem(vlan.toString());
                    subMenuItem.addProperty("subnetId", selectedNode.getObject().getId()); //NOI18N
                    subMenuItem.addProperty("vlanId", vlan.getId()); //NOI18N
                    subMenuItems.add(subMenuItem);
                }
                SubMenuDialog.getInstance((String) getValue(NAME), this).showSubmenu(subMenuItems);
            }
        } else {
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        }
    }
    
    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_IP_ADDRESS_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void finalActionPerformed(ActionEvent e) {
        if (e != null && e.getSource() instanceof SubMenuDialog) {            
            SubMenuItem selectedItem = ((SubMenuDialog) e.getSource()).getSelectedSubMenuItem();
                        
            if (CommunicationsStub.getInstance().releaseSubnetFromVLAN(
                    (String) selectedItem.getProperty("subnetId"), //NOI18N
                    (String) selectedItem.getProperty("vlanId")) //NOI18N
                )
                NotificationUtil.getInstance().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE, 
                    java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_SUCCESS"));
            else
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        }
    }
}
