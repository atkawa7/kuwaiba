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
import java.util.Iterator;
import java.util.List;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/**
 * Releases a relation between a VRF and a VLAN
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class ReleaseSubnetFromVlanAction  extends GenericInventoryAction implements Presenter.Popup {
    
    private static ReleaseSubnetFromVlanAction instance;
    
    private ReleaseSubnetFromVlanAction() { }
    
    public static ReleaseSubnetFromVlanAction getInstance() {
        return instance == null ? instance = new ReleaseSubnetFromVlanAction() : instance;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        JMenuItem eventSource = (JMenuItem)e.getSource();
        if (CommunicationsStub.getInstance().releaseSubnetFromVLAN(
                (long)eventSource.getClientProperty("subnetId"), (long)eventSource.getClientProperty("vlanId")))
            NotificationUtil.getInstance().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE, 
                    java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_SUCCESS"));
        else
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenu mnuVLANs = new JMenu(java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_RELEASE_VLAN"));
        Iterator<? extends ObjectNode> selectedNodes = Utilities.actionsGlobalContext().lookupResult(ObjectNode.class).allInstances().iterator();
        
        if (!selectedNodes.hasNext())
            return null;
        
        ObjectNode selectedNode = (ObjectNode)selectedNodes.next();
        
        List<LocalObjectLight> vlans = CommunicationsStub.getInstance().getSpecialAttribute(selectedNode.getObject().getClassName(), 
                selectedNode.getObject().getOid(), Constants.RELATIONSHIP_IPAMBELONGSTOVLAN);

        if (vlans != null) {
            if (vlans.isEmpty())
                mnuVLANs.setEnabled(false);
            else {
                for (LocalObjectLight vlan : vlans){
                    JMenuItem smiVLAN = new JMenuItem(vlan.toString());
                    smiVLAN.putClientProperty("subnetId", selectedNode.getObject().getOid()); //NOI18N
                    smiVLAN.putClientProperty("vlanId", vlan.getOid()); //NOI18N
                    smiVLAN.addActionListener(this);
                    mnuVLANs.add(smiVLAN);
                }
            }
            return mnuVLANs;
        } else {
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            return null;
        } 
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_IP_ADDRESS_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
    
}
