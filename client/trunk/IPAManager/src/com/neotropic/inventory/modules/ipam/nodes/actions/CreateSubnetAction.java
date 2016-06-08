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

import com.neotropic.inventory.modules.ipam.engine.SubnetEngine;
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
import com.neotropic.inventory.modules.ipam.nodes.SubnetPoolChildren;
import com.neotropic.inventory.modules.ipam.nodes.SubnetPoolNode;
import java.awt.Dimension;
import java.util.Iterator;
import java.util.List;
import org.inventory.communications.core.LocalObject;
import org.openide.util.Utilities;


/**
 *
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class CreateSubnetAction extends AbstractAction{
    /**
     * Reference to the communications stub singleton
     */
    private CommunicationsStub com;
    private SubnetPoolNode subnetPoolNode;
    private SubnetNode subnetNode;
    
    public CreateSubnetAction(SubnetPoolNode subnetPoolNode) {
        putValue(NAME, java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_NEW_SUBNET"));
        com = CommunicationsStub.getInstance();
        this.subnetPoolNode = subnetPoolNode;
    }
    
    public CreateSubnetAction(SubnetNode subnetNode) {
        putValue(NAME, java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_NEW_SUBNET"));
        com = CommunicationsStub.getInstance();
        this.subnetNode = subnetNode;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Iterator selectedNodes = Utilities.actionsGlobalContext().lookupResult(SubnetPoolNode.class).allInstances().iterator();
        String name = "";
        long id = 0;
        int type = 0;
        
        if (!selectedNodes.hasNext())
            return;
        
         while (selectedNodes.hasNext()) {
            SubnetPoolNode selectedNode = (SubnetPoolNode)selectedNodes.next();
            name = selectedNode.getSubnetPool().getName();
            id = selectedNode.getSubnetPool().getOid();
        }
        LocalObject subnetPool = com.getSubnetPool(id);
        type = (int)subnetPool.getAttribute("type");
         
        LocalObjectLight[] services = CommunicationsStub.getInstance().getObjectsOfClassLight(Constants.CLASS_GENERICSERVICE);
        LocalObjectLight[] owners = CommunicationsStub.getInstance().getObjectsOfClassLight(Constants.CLASS_LOCATIONOWNER);
        LocalObjectLight[] devices = CommunicationsStub.getInstance().getObjectsOfClassLight(Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);
        LocalObjectLight[] vlans = CommunicationsStub.getInstance().getObjectsOfClassLight(Constants.CLASS_VLAN);
        
        JTextField txtName = new JTextField(), txtDescription =  new JTextField();
        txtName.setPreferredSize(new Dimension(160, 18));
        txtName.setName("txtName"); //NOI18N
        txtDescription.setName("txtDescription");//NOI18N
        txtDescription.setPreferredSize(new Dimension(160, 30));
        javax.swing.JLabel lblError;
        
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
                },
                new JComponent []{txtName, txtDescription});
       
        if(JOptionPane.showConfirmDialog(null,
                pnlMyDialog,
                java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_NEW_SUBNET"),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION){

            String[] attributeNames = new String[5];
            String[] attributeValues = new String[5];
            
            attributeNames[0] = Constants.PROPERTY_NAME;
                attributeValues[0] = txtName.getText();
                attributeNames[1] = Constants.PROPERTY_DESCRIPTION;
                attributeValues[1] = txtDescription.getText();
                attributeNames[2] = Constants.PROPERTY_BROADCASTIP;
                attributeNames[3] = Constants.PROPERTY_NETWORKIP;
                attributeNames[4] = Constants.PROPERTY_HOSTS;
                SubnetEngine subnetEngine = new SubnetEngine();
            
            String ipCIDR = txtName.getText();
            int bits = 0;
            if(type == 6){
                if(txtName.getText().contains("/"));
                String[] split = txtName.getText().split("/");
                ipCIDR = split[0];
                bits = Integer.parseInt(split[1]);
            }
            boolean isAnIPAddress = SubnetEngine.isIPAddress(ipCIDR);
            if(!isAnIPAddress && bits > 0 && bits < 128)
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, 
                        java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_INVALID_CIDR"));
            
            
            else{
                if(type == 4){
                    subnetEngine.calculateSubnets(txtName.getText());
                    List<String> subnets = subnetEngine.getSubnets();
                    attributeValues[2] = subnets.get(subnets.size()-1);
                    attributeValues[3] = subnets.get(0);
                    attributeValues[4] = Integer.toString(subnetEngine.calculateNumberOfHosts());
                }
                else if(type == 6){
                    List<String> subnets = subnetEngine.calculateSubnetsIpv6(txtName.getText());
                    attributeValues[2] = subnets.get(subnets.size()-1);
                    attributeValues[3] = subnets.get(0);
                    attributeValues[4] = Integer.toString(subnetEngine.calculateNumberOfHostsIpV6());
                }

                LocalObjectLight newSubnet = com.createSubnet(subnetPoolNode.getSubnetPool().getOid(), 
                        new LocalObject(Constants.CLASS_SUBNET, 0, attributeNames, attributeValues));

                if (newSubnet == null)
                    NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
                else{
                    if (!((SubnetPoolChildren)subnetPoolNode.getChildren()).isCollapsed())
                        subnetPoolNode.getChildren().add(new SubnetNode[]{new SubnetNode(newSubnet)});
                    NotificationUtil.getInstance().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE, java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_CREATED"));
                }
            }
        }
    }    
}
