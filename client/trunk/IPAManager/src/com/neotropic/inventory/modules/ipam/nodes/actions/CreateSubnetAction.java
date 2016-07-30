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
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import com.neotropic.inventory.modules.ipam.nodes.SubnetNode;
import com.neotropic.inventory.modules.ipam.nodes.SubnetPoolChildren;
import com.neotropic.inventory.modules.ipam.nodes.SubnetPoolNode;
import com.neotropic.inventory.modules.ipam.windows.PTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.inventory.communications.core.LocalObject;
import org.openide.util.Utilities;


/**
 * Creates a subnet as a node
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
        Iterator<? extends SubnetPoolNode> selectedNodes = Utilities.actionsGlobalContext().lookupResult(SubnetPoolNode.class).allInstances().iterator();
        String className = "";
        long id = 0;
        
        if (!selectedNodes.hasNext())
            return;
        
        while (selectedNodes.hasNext()) {
            SubnetPoolNode selectedNode = (SubnetPoolNode)selectedNodes.next();
            className = selectedNode.getSubnetPool().getClassName();
            id = selectedNode.getSubnetPool().getOid();
        }
        CreateSubnetFrame subnetFrame = new CreateSubnetFrame(id, className);
        subnetFrame.setVisible(true);
    }    
    
    private class CreateSubnetFrame extends JFrame{


        private javax.swing.JButton btnAddSubnet;
        private javax.swing.JButton btnClose;
        private PTextField txtIpAddress;
        private javax.swing.JTextField txtDescription;
        private javax.swing.JLabel lblDescription;
        private javax.swing.JLabel lblError;
        private javax.swing.JLabel lblIpAddress;
        private javax.swing.JPanel pnl;
        private javax.swing.JCheckBox cbxCreateAllIps;
        
        private String className;
        private long parentId;
        private LocalObjectLight newSubnet;

        public CreateSubnetFrame(long parentId, String className) {
            this.className = className;
            this.parentId = parentId;
            initComponents();
            txtDescription.requestFocus();
        }

        @SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">  
        public final void initComponents(){
            setLayout(new BorderLayout());
            setTitle(java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_ADD_SUBNET"));
            setPreferredSize(new java.awt.Dimension(470, 200));
            setLocationRelativeTo(null);
            pnl = new javax.swing.JPanel();
            btnAddSubnet = new javax.swing.JButton();
            btnClose = new javax.swing.JButton();
            btnClose.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            });
            txtIpAddress = new PTextField(java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_USE_CIDR"));
            txtIpAddress.setForeground(Color.lightGray);
            txtDescription = new javax.swing.JTextField();
            lblIpAddress = new javax.swing.JLabel();
            lblDescription = new javax.swing.JLabel();
            cbxCreateAllIps = new javax.swing.JCheckBox();
            lblError = new javax.swing.JLabel();
            lblError.setVisible(false);
            btnAddSubnet.setText(java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_ADD_SUBNET"));
            btnAddSubnet.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    btnAddSubnetActionPerformed(evt);
                }
            });

            btnClose.setText(java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_CLOSE"));

            lblIpAddress.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
            lblIpAddress.setText(java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_SUBNET"));

            lblDescription.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
            lblDescription.setText(java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_DESCRIPTION"));

            lblError.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
            lblError.setForeground(new java.awt.Color(255, 51, 51));
            lblError.setText(java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_INVALID_CIDR"));
            cbxCreateAllIps.setText("Create all possible IPs");
            
            javax.swing.GroupLayout pnlLayout = new javax.swing.GroupLayout(pnl);
        pnl.setLayout(pnlLayout);
        pnlLayout.setHorizontalGroup(
            pnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlLayout.createSequentialGroup()
                        .addComponent(btnClose)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cbxCreateAllIps)
                        .addGap(18, 18, 18)
                        .addComponent(btnAddSubnet))
                    .addGroup(pnlLayout.createSequentialGroup()
                        .addGroup(pnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblIpAddress)
                            .addComponent(lblDescription))
                        .addGap(18, 18, 18)
                        .addGroup(pnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblError, javax.swing.GroupLayout.DEFAULT_SIZE, 387, Short.MAX_VALUE)
                            .addComponent(txtIpAddress)
                            .addComponent(txtDescription))))
                .addContainerGap())
        );
        pnlLayout.setVerticalGroup(
            pnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlLayout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addGroup(pnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblIpAddress)
                    .addComponent(txtIpAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(4, 4, 4)
                .addComponent(lblError)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblDescription)
                    .addComponent(txtDescription, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnClose)
                    .addComponent(btnAddSubnet)
                    .addComponent(cbxCreateAllIps))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

            getContentPane().add(pnl, java.awt.BorderLayout.CENTER);
            pack();
        }// </editor-fold>                        

        private void btnAddSubnetActionPerformed(java.awt.event.ActionEvent evt) { 
            SubnetEngine subnetEngine = new SubnetEngine();
            String ipCIDR = txtIpAddress.getText();
            boolean createIps = false;
            if(SubnetEngine.isCIDRFormat(ipCIDR)){
                lblError.setVisible(false);
                String[] attributeNames = new String[5];
                String[] attributeValues = new String[5];

                attributeNames[0] = Constants.PROPERTY_NAME;
                attributeNames[1] = Constants.PROPERTY_DESCRIPTION;
                attributeValues[1] = txtDescription.getText();
                attributeNames[2] = Constants.PROPERTY_BROADCASTIP;
                attributeNames[3] = Constants.PROPERTY_NETWORKIP;
                attributeNames[4] = Constants.PROPERTY_HOSTS;

                if(className.equals(Constants.CLASS_SUBNET_IPV4)){
                    subnetEngine.calculateSubnets(ipCIDR);
                    List<String> subnets = subnetEngine.getSubnets();
                    attributeValues[2] = subnets.get(subnets.size()-1);
                    attributeValues[3] = subnets.get(0);
                    attributeValues[4] = Integer.toString(subnetEngine.calculateNumberOfHosts());
                    attributeValues[0] = ipCIDR;
                    
                }else if(className.equals(Constants.CLASS_SUBNET_IPV6)){
                    subnetEngine.calculateSubnetsIpv6(ipCIDR);
                    List<String> subnets = subnetEngine.getSubnets();
                    attributeValues[2] = subnets.get(subnets.size()-1);
                    attributeValues[3] = subnets.get(0);
                    attributeValues[4] = Integer.toString(subnetEngine.calculateNumberOfHostsIpV6());
                    attributeValues[0] = ipCIDR;
                }
                if(cbxCreateAllIps.isSelected()){
                    int dialogButton = JOptionPane.YES_NO_OPTION;
                    int dialogResult = JOptionPane.showConfirmDialog (null, 
                                      "\nThis could be dangerous!!!"
                                    + "\nIf the subnet has to many IP Addresses\n"
                                    + "\nWould You like to create all the possible ip address?","Warning!!",dialogButton);
                    if(dialogResult == JOptionPane.YES_OPTION){
                            createIps=true;
                    }
                }
                
                newSubnet = CommunicationsStub.getInstance().createSubnet(parentId, className,
                            new LocalObject(className, 0, attributeNames, attributeValues));

                if(createIps){
                    String[] ipAttributeNames = new String[2];
                    String[] ipAttributeValues = new String[2];

                    ipAttributeNames[0] = Constants.PROPERTY_NAME;
                    ipAttributeNames[1] = Constants.PROPERTY_DESCRIPTION;
                    
                    LocalObjectLight addedIP = null;
                    String[] split = ipCIDR.split("/");
                    String ip = attributeValues[3];
                    if(className.equals(Constants.CLASS_SUBNET_IPV4)){

                        while(SubnetEngine.belongsTo(attributeValues[3], ip, Integer.parseInt(split[1]))){
                            ip =  SubnetEngine.nextIpv4(attributeValues[3], attributeValues[2], ip, Integer.parseInt(split[1]));
                            if(ip.trim().equals(attributeValues[2].trim()))
                                break;
                            ipAttributeValues[0] = ip;
                            ipAttributeValues[1] = "";
                            addedIP = CommunicationsStub.getInstance().addIP(newSubnet.getOid(), className,
                            new LocalObject(className, 0, ipAttributeNames, ipAttributeValues));
                        }
                    }

                    else if(className.equals(Constants.CLASS_SUBNET_IPV6)){
                        ip = SubnetEngine.nextIpv6(attributeValues[3], attributeValues[2], attributeValues[3], Integer.parseInt(split[1]));
                        while(SubnetEngine.belongsToIpv6(attributeValues[3], ip, Integer.parseInt(split[1]))){
                            ip =  SubnetEngine.nextIpv6(attributeValues[3], attributeValues[2], ip, Integer.parseInt(split[1]));
                            if(ip.trim().equals(attributeValues[2].trim()))
                                break;
                            ipAttributeValues[0] = ip;
                            ipAttributeValues[1] = "";
                            addedIP = CommunicationsStub.getInstance().addIP(newSubnet.getOid(), className, 
                            new LocalObject(className, 0, ipAttributeNames, ipAttributeValues));
                        }
                    }
                }
                if (newSubnet == null)
                    NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
                else{
                    ((SubnetPoolChildren)subnetPoolNode.getChildren()).addNotify();
                    NotificationUtil.getInstance().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE, java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_CREATED"));
                    }
                dispose();
            }
            else
                lblError.setVisible(true);
        }   

        public LocalObjectLight getNewSubnet() {
            return newSubnet;
        }
    }
}
