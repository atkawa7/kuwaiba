/*
 * Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>
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
package com.neotropic.inventory.modules.ipam.windows;

import com.neotropic.inventory.modules.ipam.nodes.IPAMRootNode;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPool;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.utils.ExplorablePanel;
import org.openide.explorer.view.BeanTreeView;

/**
 * Show the existing generic subnets that can be associated to communications elements
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class SubnetsFrame extends JFrame{
    private List<LocalObjectLight> selectedPorts;
    private ExplorablePanel pnlSubnets;

    public SubnetsFrame(List<LocalPool> subnets, List<LocalObjectLight> selectedPorts) {
        this.selectedPorts = selectedPorts;
        setLayout(new BorderLayout());
        setTitle(java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_TITLE_AVAILABLE_SUBNETS"));
        setSize(410, 600);
        setLocationRelativeTo(null);
        JLabel lblInstructions = new JLabel(java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_INSTRUCTIONS_SELECT_AN_IPADDRESS"));
        lblInstructions.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        pnlSubnets = new ExplorablePanel();
        pnlSubnets.setSize(300, 400);
        BeanTreeView treeSubnets = new BeanTreeView();
        treeSubnets.setRootVisible(false);
        JPanel pnlInstructions = new JPanel();
        pnlInstructions.setLayout(new GridLayout(1, 1));
        
        pnlSubnets.setViewportView(treeSubnets);
        pnlSubnets.getExplorerManager().setRootContext(new IPAMRootNode(subnets.toArray(new LocalPool[0])));
        add(pnlSubnets, BorderLayout.CENTER);
        
        JPanel pnlButtons = new JPanel();
        pnlButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton btnRelate = new JButton("Create relationship");
        pnlButtons.add(btnRelate);
        btnRelate.addActionListener(new SubnetsFrame.BtnConnectActionListener());
        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        pnlInstructions.add(lblInstructions);
        add(pnlInstructions,BorderLayout.NORTH);
        pnlButtons.add(btnClose);
        add(pnlButtons, BorderLayout.SOUTH);
        
    }
    
    private class BtnConnectActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            LocalObjectLight selectedIPAddress = pnlSubnets.getLookup().lookup(LocalObjectLight.class);
            if (selectedIPAddress == null || !CommunicationsStub.getInstance().isSubclassOf(selectedIPAddress.getClassName(), Constants.CLASS_IP_ADDRESS))
                JOptionPane.showMessageDialog(null, "You have to select an IP address");
            else { 
                
                List<LocalObjectLight> relatedPortsToIPAddresses = CommunicationsStub.getInstance().getSpecialAttribute(Constants.CLASS_IP_ADDRESS, 
                selectedIPAddress.getOid(), Constants.RELATIONSHIP_IPAMHASADDRESS);
                
                if(relatedPortsToIPAddresses.size()>0){
                    List<LocalObjectLight> parents = CommunicationsStub.getInstance().getParents(selectedPorts.get(0).getClassName(), selectedPorts.get(0).getOid());
                    String location= "";
                    
                    for (int i = 0; i < parents.size() - 1; i ++)
                        location += parents.get(i).toString() + " | ";
                    
                    JOptionPane.showMessageDialog(null, String.format("The IP %s is already related to port %s located in %s", selectedIPAddress.getName(), relatedPortsToIPAddresses.toString(), location), 
                                "Error", JOptionPane.ERROR_MESSAGE);
                }
                
                else if (CommunicationsStub.getInstance().relateIPtoPort(selectedIPAddress.getOid(), 
                        selectedPorts.get(0).getClassName(),
                        selectedPorts.get(0).getOid())){
                    JOptionPane.showMessageDialog(null, String.format("The IP %s  was related to port %s", 
                            selectedIPAddress.getName(), selectedPorts.get(0).toString()));
                        dispose();
                }else 
                    JOptionPane.showMessageDialog(null, CommunicationsStub.getInstance().getError(), 
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
}
