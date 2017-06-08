/*
 * Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
import javax.swing.JOptionPane;
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
 * Relates a subnet with a VLAN
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class ReleaseSubnetFromVFRAction extends GenericInventoryAction implements Presenter.Popup {
    
    private static ReleaseSubnetFromVFRAction instance;

    private ReleaseSubnetFromVFRAction() { }
    
    public static ReleaseSubnetFromVFRAction getInstance() {
        return instance == null ? instance = new ReleaseSubnetFromVFRAction() : instance;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (JOptionPane.showConfirmDialog(null, 
                "Are you sure you want to delete this relationship?", "Warning", 
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            if (CommunicationsStub.getInstance().releaseSubnetFromVFR((long)((JMenuItem)e.getSource()).getClientProperty("subnetId"),  //NOI18N
                    (long)((JMenuItem)e.getSource()).getClientProperty("vfrId"))) //NOI18N
                NotificationUtil.getInstance().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE, 
                        java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_SUCCESS"));
            else
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        }
    }
    
    @Override
    public JMenuItem getPopupPresenter() {
        JMenu mnuVFRs = new JMenu(java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_RELEASE_VRF"));
        Iterator<? extends ObjectNode> selectedNodes = Utilities.actionsGlobalContext().lookupResult(ObjectNode.class).allInstances().iterator();
        
        if (!selectedNodes.hasNext())
            return null;
        
        ObjectNode selectedNode = (ObjectNode)selectedNodes.next();
        
        List<LocalObjectLight> vfrs = CommunicationsStub.getInstance().getSpecialAttribute(selectedNode.getObject().getClassName(), 
                selectedNode.getObject().getOid(), Constants.RELATIONSHIP_IPAMBELONGSTOVRFINSTANCE);

        if (vfrs != null) {
            if (vfrs.isEmpty())
                mnuVFRs.setEnabled(false);
            else {
                for (LocalObjectLight vfr : vfrs){
                    JMenuItem smiVLAN = new JMenuItem(vfr.toString());
                    smiVLAN.putClientProperty("subnetId", selectedNode.getObject().getOid()); //NOI18N
                    smiVLAN.putClientProperty("vfrId", vfr.getOid()); //NOI18N
                    smiVLAN.addActionListener(this);
                    mnuVFRs.add(smiVLAN);
                }
            }
            return mnuVFRs;
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