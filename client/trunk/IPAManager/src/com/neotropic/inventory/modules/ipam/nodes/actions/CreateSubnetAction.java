/**
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

import com.neotropic.inventory.modules.ipam.nodes.SubnetChildren;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.JComplexDialogPanel;
import com.neotropic.inventory.modules.ipam.nodes.SubnetNode;
import com.neotropic.inventory.modules.ipam.nodes.SubnetPoolNode;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JMenuItem;
import org.inventory.communications.core.LocalObject;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.inventory.navigation.applicationnodes.pools.PoolChildren;


/**
 *
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class CreateSubnetAction extends AbstractAction{
    /**
     * Reference to the communications stub singleton
     */
    private CommunicationsStub com;
    private SubnetEngine subnetEngine;
    private SubnetPoolNode subnetPoolNode;
    private SubnetNode subnetNode;

    public CreateSubnetAction(SubnetPoolNode subnetPoolNode) {
        putValue(NAME, java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_NEW_SUBNET"));
        com = CommunicationsStub.getInstance();
        this.subnetPoolNode = subnetPoolNode;
    }
    
    public CreateSubnetAction(SubnetNode subnetNode) {
        subnetEngine = new SubnetEngine();
        putValue(NAME, java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_NEW_SUBNET"));
        com = CommunicationsStub.getInstance();
        this.subnetNode = subnetNode;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        LocalObjectLight[] services = CommunicationsStub.getInstance().getObjectsOfClassLight(Constants.CLASS_GENERICSERVICE);
        LocalObjectLight[] owners = CommunicationsStub.getInstance().getObjectsOfClassLight(Constants.CLASS_LOCATIONOWNER);
        LocalObjectLight[] devices = CommunicationsStub.getInstance().getObjectsOfClassLight(Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);
        LocalObjectLight[] vlans = CommunicationsStub.getInstance().getObjectsOfClassLight(Constants.CLASS_VLAN);
        
        JTextField txtName = new JTextField(), txtDescription =  new JTextField();
        txtName.setPreferredSize(new Dimension(120, 18));
        txtName.setName("txtName"); //NOI18N
        txtDescription.setName("txtDescription");//NOI18N
        
        JComboBox<LocalObjectLight> servicesList = new JComboBox<>(services);
        servicesList.setName("servicesList"); //NOI18N
        JComboBox<LocalObjectLight> ownersList = new JComboBox<>(owners);
        ownersList.setName("ownersList"); //NOI18N
        JComboBox<LocalObjectLight> devicesList = new JComboBox<>(devices);
        devicesList.setName("devicesList"); //NOI18N
        JComboBox<LocalObjectLight> vlanList = new JComboBox<>(vlans);
        vlanList.setName("vlanList"); //NOI18N
        
        JComplexDialogPanel pnlMyDialog = new JComplexDialogPanel(
                new String[]{java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_NAME"),
                    java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_DESCRIPTION"),
                    java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_SERVICES"),
                    java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_OWNERS"),
                    java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_DEVICES"),
                    java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_VLANS"),
                },
                new JComponent []{txtName, txtDescription, servicesList, ownersList, devicesList, vlanList});
       
        if(JOptionPane.showConfirmDialog(null,
                pnlMyDialog,
                java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_NEW_SUBNET"),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION){
            
            //if(!subnetEngine.isIPAddress(txtName.getText()))
//                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, "The ip is wrong");
            long id = 0;
            LocalObjectLight subnet = subnetPoolNode.getSubnetPool();
            List<LocalObjectLight> pools = com.getPools(Constants.CLASS_SUBNET);
            
            LocalObjectLight newObject = com.createPoolItem(subnet.getOid(), Constants.CLASS_SUBNET);
            LocalObject objectInfo = com.getObjectInfo(Constants.CLASS_SUBNET, newObject.getOid());
            objectInfo.setName(txtName.getText());
            //VLAN
            //owner
            //networkIP
            //broadcastIP
            //hosts
            //device
            //service
            System.out.println("asdasd");
//            LocalObjectLight newObject = com.createPoolItem(subnetNode.getSubnet().getOid(), ((JMenuItem)e.getSource()).getName());
//
//            
            if (newObject == null)
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
            else{
                if (!((SubnetChildren)subnetPoolNode.getChildren()).isCollapsed())
                    subnetPoolNode.getChildren().add(new SubnetNode[]{new SubnetNode(newObject)});
                NotificationUtil.getInstance().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE, java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_CREATED"));
            }
            
            
            NotificationUtil.getInstance().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE, 
                    java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_CREATED"));
        }
    }
    

    
}
