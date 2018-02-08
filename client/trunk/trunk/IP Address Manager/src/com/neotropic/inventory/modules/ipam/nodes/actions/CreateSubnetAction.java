/*
 * Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>
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
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import com.neotropic.inventory.modules.ipam.nodes.SubnetNode;
import com.neotropic.inventory.modules.ipam.nodes.SubnetPoolNode;
import com.neotropic.inventory.modules.ipam.windows.PTextField;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.inventory.communications.core.LocalObject;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.i18n.I18N;
import org.inventory.navigation.navigationtree.nodes.AbstractChildren;
import org.openide.nodes.AbstractNode;
import org.openide.util.Utilities;

/**
 * Creates a subnet as a node
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class CreateSubnetAction extends GenericInventoryAction {
    /**
     * Reference to the communications stub singleton
     */
    private CommunicationsStub com;
    private static CreateSubnetAction instance;
    
    private CreateSubnetAction() {
        putValue(NAME, I18N.gm("create_subnet"));
        com = CommunicationsStub.getInstance();
    }
    
    public static CreateSubnetAction getInstance() {
        return instance == null ? instance = new CreateSubnetAction() : instance;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Iterator<? extends AbstractNode> selectedNodes = Utilities.actionsGlobalContext().lookupResult(AbstractNode.class).allInstances().iterator();
        String className;
        String subnetParent;
        long id;
        
        if (!selectedNodes.hasNext())
            return;
        
        AbstractNode selectedNode = selectedNodes.next();
        if(selectedNode instanceof SubnetPoolNode) {
            SubnetPoolNode node = (SubnetPoolNode)selectedNode;
            className = node.getSubnetPool().getClassName();
            id = node.getSubnetPool().getOid();
            subnetParent = null;
        }
        else { //It's a subnet node
            SubnetNode node = (SubnetNode)selectedNode;
            className = node.getObject().getClassName();
            id = node.getObject().getOid();
            subnetParent =  node.getObject().getName();
        }
        CreateSubnetFrame subnetFrame = new CreateSubnetFrame(id, className, subnetParent, selectedNode);
        subnetFrame.setVisible(true);
    }    

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_IP_ADDRESS_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
    
    private class CreateSubnetFrame extends JFrame {

        private javax.swing.JButton btnAddSubnet;
        private javax.swing.JButton btnClose;
        private PTextField txtIpAddress;
        private javax.swing.JTextField txtDescription;
        private javax.swing.JLabel lblDescription;
        private javax.swing.JLabel lblError;
        private javax.swing.JLabel lblIpAddress;
        private javax.swing.JPanel pnl;
        private javax.swing.JCheckBox cbxCreateAllIps;
        
        private final String className;
        private final long parentId;
        private final String subnetParent;
        private LocalObjectLight newSubnet;
        
        private AbstractNode selectedNode;

        public CreateSubnetFrame(long parentId, String className, String subnetParent, AbstractNode selectedNode) {
            this.className = className;
            this.parentId = parentId;
            this.subnetParent = subnetParent;
            this.selectedNode = selectedNode;
            initComponents();
            txtDescription.requestFocus();
            if(subnetParent != null){
                String[] split = subnetParent.split("/");
                txtIpAddress.setText(split[0]+"/");
            }
        }

        @SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">  
        public final void initComponents(){
            //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLayout(new BorderLayout());
            setTitle(I18N.gm("create_subnet"));
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
            txtIpAddress = new PTextField(I18N.gm("use_cdir_format"));
            //txtIpAddress.setForeground(Color.lightGray);
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

            btnClose.setText(I18N.gm("close"));

            lblIpAddress.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
            lblIpAddress.setText(I18N.gm("subnet"));

            lblDescription.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
            lblDescription.setText(I18N.gm("description"));

            lblError.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
            lblError.setForeground(new java.awt.Color(255, 51, 51));
            lblError.setText(I18N.gm("invalid_cidr_format"));
            cbxCreateAllIps.setText(I18N.gm("create_all_subnet_IPs"));
            
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
            boolean isSubnet = false;
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
                    
                    if(subnetParent != null)
                        isSubnet = subnetEngine.isSubnetofSubnet(ipCIDR, subnetParent);
                    
                } else if(className.equals(Constants.CLASS_SUBNET_IPV6)){
                    subnetEngine.calculateSubnetsIpv6(ipCIDR);
                    List<String> subnets = subnetEngine.getSubnets();
                    attributeValues[2] = subnets.get(subnets.size()-1);
                    attributeValues[3] = subnets.get(0);
                    attributeValues[4] = Integer.toString(subnetEngine.calculateNumberOfHostsIpV6());
                    attributeValues[0] = ipCIDR;
                    
                    if(subnetParent != null)
                        isSubnet = subnetEngine.isSubnetofSubnetIPv6(ipCIDR, subnetParent);
                }
                
                if(subnetParent != null && !isSubnet){
                    lblError.setText(I18N.gm("this_is_not_subnet_of")+ subnetParent);
                    lblError.setVisible(true);
                    return;
                }
                
                if(cbxCreateAllIps.isSelected()){
                    int dialogResult = JOptionPane.showConfirmDialog (null, 
                                    I18N.gm("want_to_create_all_ips"), 
                                    I18N.gm("warning"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (dialogResult == JOptionPane.YES_OPTION)
                            createIps = true;
                }
                
                newSubnet = CommunicationsStub.getInstance().createSubnet(parentId, className,
                    new LocalObject(className, 0, attributeNames, attributeValues));
                
                if(createIps)
                    createIps(ipCIDR, attributeValues, className, newSubnet);
                
                if (newSubnet == null)
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, com.getError());
                else {
                    ((AbstractChildren)selectedNode.getChildren()).addNotify();
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("success"), 
                            NotificationUtil.INFO_MESSAGE, I18N.gm("subnet_created_successfully"));
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
    
    private void createIps(String ipCIDR, String[] attributeValues, String className, LocalObjectLight newSubnet){
        String[] ipAttributeNames = new String[2];
        String[] ipAttributeValues = new String[2];

        ipAttributeNames[0] = Constants.PROPERTY_NAME;
        ipAttributeNames[1] = Constants.PROPERTY_DESCRIPTION;

        String[] split = ipCIDR.split("/");
        String ip = attributeValues[3];
        switch (className) {
            case Constants.CLASS_SUBNET_IPV4:
                while(SubnetEngine.belongsTo(attributeValues[3], ip, Integer.parseInt(split[1]))){
                    ip =  SubnetEngine.nextIpv4(attributeValues[3], attributeValues[2], ip, Integer.parseInt(split[1]));
                    if(ip.trim().equals(attributeValues[2].trim()))
                        break;
                    ipAttributeValues[0] = ip;
                    ipAttributeValues[1] = "";
                    CommunicationsStub.getInstance().addIP(newSubnet.getOid(), className,
                            new LocalObject(className, 0, ipAttributeNames, ipAttributeValues));
                }   break;
            case Constants.CLASS_SUBNET_IPV6:
                ip = SubnetEngine.nextIpv6(attributeValues[3], attributeValues[2], attributeValues[3], Integer.parseInt(split[1]));
                while(SubnetEngine.belongsToIpv6(attributeValues[3], ip, Integer.parseInt(split[1]))){
                    ip =  SubnetEngine.nextIpv6(attributeValues[3], attributeValues[2], ip, Integer.parseInt(split[1]));
                    if(ip.trim().equals(attributeValues[2].trim()))
                        break;
                    ipAttributeValues[0] = ip;
                    ipAttributeValues[1] = "";
                    CommunicationsStub.getInstance().addIP(newSubnet.getOid(), className,
                            new LocalObject(className, 0, ipAttributeNames, ipAttributeValues));
            }   break;
        }
    }
}
