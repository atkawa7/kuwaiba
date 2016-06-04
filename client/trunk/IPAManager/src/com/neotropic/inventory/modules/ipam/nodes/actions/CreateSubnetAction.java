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
import com.neotropic.inventory.modules.ipam.nodes.SubnetPoolChildren;
import com.neotropic.inventory.modules.ipam.nodes.SubnetPoolNode;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import org.inventory.communications.core.LocalAttributeMetadata;
import org.inventory.communications.core.LocalClassMetadata;
import org.inventory.communications.core.LocalObject;


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

            String[] attributeNames = new String[5];
            String[] attributeValues = new String[5];
            
            SubnetEngine subnetEngine = new SubnetEngine();
            subnetEngine.calculateSubnets(txtName.getText());
            List<String> subnets = subnetEngine.getSubnets();
            
            attributeNames[0] = Constants.PROPERTY_NAME;
            attributeValues[0] = txtName.getText();
            attributeNames[1] = Constants.PROPERTY_DESCRIPTION;
            attributeValues[1] = txtDescription.getText();
            attributeNames[2] = Constants.PROPERTY_BROADCASTIP;
            attributeValues[2] = subnets.get(subnets.size()-1);
            attributeNames[3] = Constants.PROPERTY_NETWORKIP;
            attributeValues[3] = subnets.get(0);
            attributeNames[4] = Constants.PROPERTY_HOSTS;
            attributeValues[4] = Integer.toString(subnetEngine.calculateNumberOfHosts());
            
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
