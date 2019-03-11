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

import com.neotropic.inventory.modules.ipam.engine.SubnetEngine;
import com.neotropic.inventory.modules.ipam.nodes.SubnetNode;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.JFrame;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObject;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.navigation.navigationtree.nodes.AbstractChildren;
import org.openide.util.Utilities;

/**
 * Allows to add an IP address that belongs to a subnet
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class AddIPAddressAction extends GenericInventoryAction {

    /**
     * Reference to the communications stub singleton
     */
    private CommunicationsStub com;
    private SubnetNode subnetNode;
    private int maskBits;
    private List<LocalObjectLight> subnetUsedIps;
    private static AddIPAddressAction instance;
    
    private AddIPAddressAction() {
        putValue(NAME, I18N.gm("add_ip_address"));
        com = CommunicationsStub.getInstance();
        maskBits = 32;
        subnetUsedIps = new ArrayList<>();
    }
    
    public static AddIPAddressAction getInstance() {
        return instance == null ? instance = new AddIPAddressAction() : instance;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Iterator<? extends SubnetNode> selectedNodes = Utilities.actionsGlobalContext().lookupResult(SubnetNode.class).allInstances().iterator();
        String className = "";
        String id = "0";
        if (!selectedNodes.hasNext())
            return;
        
        while (selectedNodes.hasNext()) {
            subnetNode = (SubnetNode)selectedNodes.next();
            className = subnetNode.getObject().getClassName();
            id = subnetNode.getObject().getId();
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

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_IP_ADDRESS_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
    
    
    private class AddIPAddressFrame extends JFrame {
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
        private String parentId;

        public AddIPAddressFrame(String parentId, String networkIp, String broadcastIp, String className, String nextIp) {
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
            //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLayout(new BorderLayout());
            setTitle(I18N.gm("add_ip_address"));
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
            btnAddIPAddress.setText(I18N.gm("add_ip_address"));
            btnAddIPAddress.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    btnAddSubnetActionPerformed(evt);
                }
            });

            btnClose.setText(I18N.gm("close"));

            lblIpAddress.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
            lblIpAddress.setText(I18N.gm("ip_address"));

            lblDescription.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
            lblDescription.setText(I18N.gm("description"));

            lblError.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
            lblError.setForeground(new java.awt.Color(255, 51, 51));
            lblError.setText(I18N.gm("invalid_cidr_format"));

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
            List<String> ips = new ArrayList<>();
            boolean isUsed = false;
            
            if(ipAddress.contains("-")){
                String[] split = ipAddress.split("-");
                String firstIp = split[0].trim();
                String lastIp =  split[1].trim();

                if(SubnetEngine.isIPAddress(firstIp) && 
                        SubnetEngine.isIPAddress(lastIp) &&
                        belongToSubnet(firstIp) &&
                        belongToSubnet(lastIp))
                {
                    isUsed = ipIsInUse(ipAddress);
                    if (!isUsed)
                        ips.add(firstIp);
                    String nextRangeIp = firstIp;
                    
                    while(!nextRangeIp.equals(lastIp)){
                        nextRangeIp = nextIpInRange(nextRangeIp, className, networkIp, broadcastIp);
                        isUsed = ipIsInUse(ipAddress);
                        if (!isUsed && nextRangeIp != null)
                            ips.add(nextRangeIp);
                    }
                }
                else{
                    lblError.setText(I18N.gm("ip_misspelled_or_out_of_subnet"));
                    lblError.setVisible(true);  
                }
            }// end if IPs came in a range
            
            else{
                if(SubnetEngine.isIPAddress(ipAddress)){
                    if(belongToSubnet(ipAddress) && !ipIsInUse(ipAddress))
                        ips.add(ipAddress);
                    else{
                        lblError.setText(I18N.gm("ip_outside_of_subnet_or_in_use"));
                        lblError.setVisible(true);
                    }
                }
                else
                    lblError.setVisible(true);
            }//end else one IP
            if(!ips.isEmpty()){
                for (String ip : ips) {
                    HashMap<String, String> attributes = new HashMap<>();
                    attributes.put(Constants.PROPERTY_NAME, ip);
                    attributes.put(Constants.PROPERTY_DESCRIPTION, txtDescription.getText());
                    
                    LocalObjectLight addedIP = CommunicationsStub.getInstance().
                            addIPAddress(parentId, className, ip, attributes);

                    if (addedIP == null)
                        NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, com.getError());
                    else {
                        ((AbstractChildren)subnetNode.getChildren()).addNotify();
                        NotificationUtil.getInstance().showSimplePopup(I18N.gm("success"), NotificationUtil.INFO_MESSAGE, ip + " " + I18N.gm("ip_create_successfully"));
                    }
                    dispose();
                }//end for creating IPs 
            }
        }
    
        private boolean ipIsInUse(String ipAddress){
            for (LocalObjectLight subnetUsedIp : subnetUsedIps) {
                if(subnetUsedIp.getName().equals(ipAddress))
                    return true;
            }
            return false;
        }
    
        private String nextIpInRange(String ipAddress, String className, String networkIp, String broadcastIp){
            String nextRangeIp;
            switch (className) {
                case Constants.CLASS_SUBNET_IPV4:
                    nextRangeIp = SubnetEngine.nextIpv4(networkIp, broadcastIp, ipAddress, maskBits);
                    if(SubnetEngine.belongsTo(networkIp, nextRangeIp, maskBits))
                        return nextRangeIp;
                    break;
                case Constants.CLASS_SUBNET_IPV6:
                    nextRangeIp = SubnetEngine.nextIpv6(networkIp, broadcastIp, ipAddress, maskBits);
                    if(SubnetEngine.belongsToIpv6(networkIp, nextRangeIp, maskBits))
                        return nextRangeIp;
                    break;
            }
            return null;
        }
        
        private boolean belongToSubnet(String ipAddress){
            switch (className) {
                case Constants.CLASS_SUBNET_IPV4:
                    return SubnetEngine.belongsTo(networkIp, ipAddress, maskBits);
                case Constants.CLASS_SUBNET_IPV6:
                    return SubnetEngine.belongsToIpv6(networkIp, ipAddress, maskBits);
            }
            return false;
        }
        
    }
    
        
}
