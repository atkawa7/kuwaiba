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
import com.neotropic.inventory.modules.ipam.nodes.SubnetNode;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JFrame;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObject;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.actions.GenericObjectNodeAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.applicationnodes.objectnodes.AbstractChildren;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * Allows to relate an IP of a subnet with a GenericNetworkElement
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
@ServiceProvider(service=GenericObjectNodeAction.class)
public class AddIPAddressAction extends GenericObjectNodeAction {

    /**
     * Reference to the communications stub singleton
     */
    private CommunicationsStub com;
    private SubnetNode subnetNode;
    private int maskBits;
    private List<LocalObjectLight> subnetUsedIps;
    
    public AddIPAddressAction() {
        putValue(NAME, java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_ADD_IP_ADDRESS"));
        com = CommunicationsStub.getInstance();
        maskBits = 32;
        subnetUsedIps = new ArrayList<>();
    }
    
    @Override
    public String getValidator() {
        return Constants.VALIDATOR_SUBNET;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Iterator<? extends SubnetNode> selectedNodes = Utilities.actionsGlobalContext().lookupResult(SubnetNode.class).allInstances().iterator();
        String className = "";
        long id = 0;
        if (!selectedNodes.hasNext())
            return;
        
        while (selectedNodes.hasNext()) {
            subnetNode = (SubnetNode)selectedNodes.next();
            className = subnetNode.getObject().getClassName();
            id = subnetNode.getObject().getOid();
        }
        LocalObject subnet = com.getSubnet(id, className);
        String networkIp = (String)subnet.getAttribute(Constants.PROPERTY_NETWORKIP);
        String broadcastIp = (String)subnet.getAttribute(Constants.PROPERTY_BROADCASTIP);
        String cidr = subnet.getName();
        int type = 0;
        if(className.equals(Constants.CLASS_SUBNET_IPV4))
            type = 4;
        if(className.equals(Constants.CLASS_SUBNET_IPV6))
            type = 6;
        
        String nextIp = "";
        String lastUsedIP;
        subnetUsedIps = com.getSubnetUsedIps(id, className);
        if(!subnetUsedIps.isEmpty())
            lastUsedIP = subnetUsedIps.get(0).getName();
        else
            lastUsedIP = networkIp;
        
        if(type == Constants.IPV4_TYPE){
            String[] split = cidr.split("/");
            maskBits = Integer.parseInt(split[1]);
            nextIp = SubnetEngine.nextIpv4(networkIp, broadcastIp, lastUsedIP, maskBits);
        }
        else if(type == Constants.IPV6_TYPE){
            String[] split = cidr.split("/");
            maskBits = Integer.parseInt(split[1]);
            nextIp = SubnetEngine.nextIpv6(networkIp, broadcastIp, lastUsedIP, maskBits);
        }
        AddIPAddressFrame addIpFrame = new AddIPAddressFrame(id, networkIp, broadcastIp, className, nextIp);
        addIpFrame.setVisible(true);
    }
    
    
    private class AddIPAddressFrame extends JFrame{
        private javax.swing.JButton btnAddIPAddress;
        private javax.swing.JButton btnClose;
        private javax.swing.JTextField txtIpAddress;
        private javax.swing.JTextField txtDescription;
        private javax.swing.JLabel lblDescription;
        private javax.swing.JLabel lblError;
        private javax.swing.JLabel lblIpAddress;
        private javax.swing.JPanel pnl;

        private String networkIp;
        private String broadcastIp;
        private String nextIp;
        private String className;
        private long parentId;

        public AddIPAddressFrame(long parentId, String networkIp, String broadcastIp, String className, String nextIp) {
            this.networkIp = networkIp;
            this.broadcastIp = broadcastIp;
            this.className = className;
            this.parentId = parentId;
            this.nextIp = nextIp;
            initComponents();
            txtDescription.requestFocus();
        }

        @SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">  
        public final void initComponents(){
            setLayout(new BorderLayout());
            setTitle(java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_ADD_IP_ADDRESS"));
            setPreferredSize(new java.awt.Dimension(470, 170));
            setLocationRelativeTo(null);
            pnl = new javax.swing.JPanel();
            btnAddIPAddress = new javax.swing.JButton();
            btnClose = new javax.swing.JButton();
            btnClose.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            });
            txtIpAddress = new javax.swing.JTextField();
            txtIpAddress.setText(nextIp);
            txtDescription = new javax.swing.JTextField();
            lblIpAddress = new javax.swing.JLabel();
            lblDescription = new javax.swing.JLabel();
            lblError = new javax.swing.JLabel();
            lblError.setVisible(false);
            btnAddIPAddress.setText(java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_ADD_IP_ADDRESS"));
            btnAddIPAddress.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    btnAddSubnetActionPerformed(evt);
                }
            });

            btnClose.setText(java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_CLOSE"));

            lblIpAddress.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
            lblIpAddress.setText(java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_IP_ADDRESS"));

            lblDescription.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
            lblDescription.setText(java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_DESCRIPTION"));

            lblError.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
            lblError.setForeground(new java.awt.Color(255, 51, 51));
            lblError.setText(java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_INVALID_CIDR"));

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
                            .addComponent(btnAddIPAddress))
                        .addGroup(pnlLayout.createSequentialGroup()
                            .addGroup(pnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(lblIpAddress)
                                .addComponent(lblDescription))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addGroup(pnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(txtIpAddress)
                                .addComponent(txtDescription)
                                .addComponent(lblError, javax.swing.GroupLayout.DEFAULT_SIZE, 272, Short.MAX_VALUE))))
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
                        .addComponent(txtDescription, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(18, 18, 18)
                    .addGroup(pnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnClose)
                        .addComponent(btnAddIPAddress))
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );

            getContentPane().add(pnl, java.awt.BorderLayout.CENTER);
            pack();
        }// </editor-fold>                        

        private void btnAddSubnetActionPerformed(java.awt.event.ActionEvent evt) { 
            
            String ipAddress = txtIpAddress.getText();
            boolean isUsed = false;
            boolean itBelong = false;
            boolean isNetworIp = false;
            boolean isBroadcastIp = false;
            
            if(SubnetEngine.isIPAddress(ipAddress)){
                //looking for used IPs
                for (LocalObjectLight subnetUsedIp : subnetUsedIps) {
                    if(subnetUsedIp.getName().equals(ipAddress)){
                        isUsed = true;
                        break;
                    }
                }
                
                if(className.equals(Constants.CLASS_SUBNET_IPV4)){
                    if(!SubnetEngine.belongsTo(networkIp, ipAddress, maskBits))
                        itBelong = true;
                }
                else if (className.equals(Constants.CLASS_SUBNET_IPV6)){   
                    if(!SubnetEngine.belongsToIpv6(networkIp, ipAddress, maskBits))
                        itBelong = true;
                }
                if(ipAddress.equals(networkIp))
                    isNetworIp = true;
                
                if(ipAddress.equals(broadcastIp))
                    isBroadcastIp = true;

                if(!isUsed && !itBelong && !isBroadcastIp && ! isNetworIp){    
                    lblError.setVisible(false);
                    String[] attributeNames = new String[2];
                    String[] attributeValues = new String[2];

                    attributeNames[0] = Constants.PROPERTY_NAME;
                    attributeNames[1] = Constants.PROPERTY_DESCRIPTION;
                    attributeValues[0] = ipAddress;
                    attributeValues[1] = txtDescription.getText();

                    LocalObjectLight addedIP = CommunicationsStub.getInstance().addIP(parentId, className,
                            new LocalObject(className, 0, attributeNames, attributeValues));

                    if (addedIP == null)
                        NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
                    else {
                        ((AbstractChildren)subnetNode.getChildren()).addNotify();
                        NotificationUtil.getInstance().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE, java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_CREATED"));
                    }
                    dispose();
                }
                else if(itBelong){
                    lblError.setText("This IP is outside of the subnet");
                    lblError.setVisible(true);
                }
                else if(isUsed){
                    lblError.setText("This IP is in use");
                    lblError.setVisible(true);  
                }
                else if(isNetworIp){
                    lblError.setText("The network IP can not be use");
                    lblError.setVisible(true);  
                }
                else if(isBroadcastIp){
                    lblError.setText("The broadcast IP can not be use");
                    lblError.setVisible(true);  
                }
            }
            else
                lblError.setVisible(true);
        }
    }
}
