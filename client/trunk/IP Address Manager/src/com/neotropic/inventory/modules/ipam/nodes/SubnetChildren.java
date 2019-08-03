/*
 * Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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
package com.neotropic.inventory.modules.ipam.nodes;

import com.neotropic.inventory.modules.ipam.engine.SubnetEngine;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.navigation.navigationtree.nodes.AbstractChildren;

import org.openide.nodes.Node;

/**
 * Children for subnet nodes
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class SubnetChildren extends AbstractChildren {
    
    @Override
    public void addNotify(){
        LocalObjectLight subnet = ((SubnetNode)getNode()).getObject();
        List<LocalObjectLight> ips = CommunicationsStub.getInstance().
                getSubnetUsedIps(subnet.getId(), subnet.getClassName());
        List<LocalObjectLight> subnets = CommunicationsStub.getInstance().
                getSubnetsInSubnet(subnet.getId(), subnet.getClassName());
        
        if (ips == null)
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        
        if (subnets == null)
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        else {
            List<LocalObjectLight> subnetChildren = new ArrayList<>();
             
            for (LocalObjectLight subnetChild : sortSubnets(subnets)) 
                subnetChildren.add(subnetChild);
            
            for (LocalObjectLight ip : sortIps(ips)) 
                subnetChildren.add(ip);

            setKeys(subnetChildren);
        }
    }
    
    @Override
    protected void removeNotify() {
        setKeys(Collections.EMPTY_SET);
    } 

    @Override
    protected Node[] createNodes(LocalObjectLight key) {
        
        if(key.getClassName().equals(Constants.CLASS_IP_ADDRESS))
            return new Node[] { new IPAddressNode(key) };
        else
            return new Node[] { new SubnetNode(key) };
    }
    
    /**
     * Sorts the IP addresses nodes children of a subnet
     * @param ips
     * @return 
     */
    private List<LocalObjectLight> sortIps(List<LocalObjectLight> ips){
        boolean isMalformed;
        List<LocalObjectLight> sortIps = ips;
        List<LocalObjectLight> malformedIps = new ArrayList<>();
        
        for (int i = 0; i < sortIps.size(); i++) {
            for (int j = i; j< sortIps.size(); j++) {
                isMalformed = false;
                LocalObjectLight auxIp;
                if(!sortIps.get(i).getName().contains(":")){
                    String[] aIp = sortIps.get(i).getName().split("\\.");
                    String[] bIp = sortIps.get(j).getName().split("\\.");

                    if(!isNumeric(aIp[0]) || !isNumeric(aIp[1]) || !isNumeric(aIp[2]) || !isNumeric(aIp[3]) || !isNumeric(bIp[0]) || !isNumeric(bIp[1]) || !isNumeric(bIp[2]) || !isNumeric(bIp[3]))
                        isMalformed = true;
                    
                    if(!isMalformed){
                        if(Integer.valueOf(aIp[0]) >= Integer.valueOf(bIp[0])){
                           if(Integer.valueOf(aIp[1]) >= Integer.valueOf(bIp[1])){
                               if(Integer.valueOf(aIp[2]) >= Integer.valueOf(bIp[2])){
                                   if(Integer.valueOf(aIp[3]) >= Integer.valueOf(bIp[3])){
                                       auxIp =  sortIps.get(i);
                                       sortIps.set(i, sortIps.get(j));
                                       sortIps.set(j, auxIp);
                                   }
                               }
                           }
                        }
                    }
                    else if((!isNumeric(bIp[0]) || !isNumeric(bIp[1]) || !isNumeric(bIp[2]) || !isNumeric(bIp[3])) && !malformedIps.contains(sortIps.get(j)))
                        malformedIps.add(sortIps.get(j));
                }//end if is ipv4
                else{
                    String[] aIp = SubnetEngine.completeIPv6(sortIps.get(i).getName());
                    String[] bIp = SubnetEngine.completeIPv6(sortIps.get(j).getName());

                    for(int k=0; k<8; k++){
                        if(!isHexNumber(bIp[k]))
                            isMalformed = true;
                    }
                    if(!isMalformed){
                        for(int k=0; k<8; k++){
                            if(Integer.parseInt(aIp[k],16) >= Integer.parseInt(bIp[k],16)){
                                if(k==7){
                                    auxIp =  sortIps.get(i);
                                    sortIps.set(i, sortIps.get(j));
                                    sortIps.set(j, auxIp);
                                }
                            }
                            else    
                                break;
                        }
                    }
                    else if(isMalformed && !malformedIps.contains(sortIps.get(j)))
                        malformedIps.add(sortIps.get(j));
                }
            }
        }
        
        if(!malformedIps.isEmpty()){
            LocalObjectLight[] malformedIpAddressesArray = malformedIps.toArray(new LocalObjectLight[0]);
            final JList<LocalObjectLight> lstMalformedSubnets = new JList<>(malformedIpAddressesArray);
            
            final JFrame frame = new JFrame("Malformed Subnets");
            frame.setLayout(new BorderLayout());
            frame.setSize(400, 350);
            frame.setLocationRelativeTo(null);
            JLabel lblInstructions = new JLabel("This IP addresses are malformed");
            lblInstructions.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            frame.add(lblInstructions, BorderLayout.NORTH);
            frame.add(lstMalformedSubnets, BorderLayout.CENTER);
            frame.setVisible(true);
            JPanel pnlButtons = new JPanel();
            pnlButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
            JButton btnClose = new JButton("Close");
            btnClose.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    frame.dispose();
                }
            });
            pnlButtons.add(btnClose);
            frame.add(pnlButtons, BorderLayout.SOUTH);
            //sortIps.addAll(malformedIps);
        }
        
        return sortIps;
    }
    
    /**
     * Sorts the subnet nodes children of subnets
     * @param subnets a list of subnets
     * @return a list of subnets and IPs addresses
     */
    private List<LocalObjectLight> sortSubnets(List<LocalObjectLight> subnets){
        
        List<LocalObjectLight> sortSubnets = subnets;
        List<LocalObjectLight> malformedSubnets = new ArrayList<>();
        boolean isMalformed;
        
        for (int i = 0; i < sortSubnets.size(); i++) {
            for (int j = i; j< sortSubnets.size(); j++) {
                isMalformed = false;
                LocalObjectLight auxSubnet = null;
                if(!sortSubnets.get(i).getName().contains(":")){
                    String[] aIpCIDRsplit = sortSubnets.get(i).getName().split("/");
                    String[] aSubnetSplit = aIpCIDRsplit[0].split("\\.");

                    String[] bIpCIDRsplit = sortSubnets.get(j).getName().split("/");
                    String[] bSubnetSplit = bIpCIDRsplit[0].split("\\.");
                    
                    for(String token : bSubnetSplit){
                        if(!isNumeric(aSubnetSplit[0]) || !isNumeric(aSubnetSplit[1]) || !isNumeric(aSubnetSplit[2]) || !isNumeric(aSubnetSplit[3]) ||
                                !isNumeric(bSubnetSplit[0]) || !isNumeric(bSubnetSplit[1]) || !isNumeric(bSubnetSplit[2]) || !isNumeric(bSubnetSplit[3]))
                            isMalformed = true;
                    }
                    
                    if(!isMalformed){
                        if(Integer.valueOf(aSubnetSplit[0]) >= Integer.valueOf(bSubnetSplit[0])){
                           if(Integer.valueOf(aSubnetSplit[1]) >= Integer.valueOf(bSubnetSplit[1])){
                               if(Integer.valueOf(aSubnetSplit[2]) >= Integer.valueOf(bSubnetSplit[2])){
                                   if(Integer.valueOf(aSubnetSplit[3]) >= Integer.valueOf(bSubnetSplit[3])){
                                       auxSubnet =  sortSubnets.get(i);
                                       sortSubnets.set(i, sortSubnets.get(j));
                                       sortSubnets.set(j, auxSubnet);
                                   }
                               }
                           }
                        }
                    }
                    else if( (!isNumeric(bSubnetSplit[0]) || !isNumeric(bSubnetSplit[1]) || !isNumeric(bSubnetSplit[2]) || !isNumeric(bSubnetSplit[3]))
                            && !malformedSubnets.contains(sortSubnets.get(j)))
                        malformedSubnets.add(sortSubnets.get(j));
                }//end ipv4
                else{
                    String[] aIpCIDRsplit = sortSubnets.get(i).getName().split("/");
                    String[] aSubnetSplit = SubnetEngine.completeIPv6(aIpCIDRsplit[0]);

                    String[] bIpCIDRsplit = sortSubnets.get(j).getName().split("/");
                    String[] bSubnetSplit = SubnetEngine.completeIPv6(bIpCIDRsplit[0]);
                     
                        for(int k=0; k<8; k++){
                            if(!isHexNumber(bSubnetSplit[i]))
                                isMalformed = true;
                        }              
                        if(!isMalformed){
                            for(int k=0; k<8; k++){
                                if(Integer.parseInt(aSubnetSplit[k],16) >= Integer.parseInt(bSubnetSplit[k],16)){
                                    if(k==7){
                                        auxSubnet =  sortSubnets.get(i);
                                        sortSubnets.set(i, sortSubnets.get(j));
                                        sortSubnets.set(j, auxSubnet);
                                    }
                                }
                                else    
                                    break;
                            }
                        }
                        else if(isMalformed && !malformedSubnets.contains(sortSubnets.get(j)))
                            malformedSubnets.add(sortSubnets.get(j));
                }
            }
        }
        if(!malformedSubnets.isEmpty()){
            LocalObjectLight[] malformedSubnetsArray = malformedSubnets.toArray(new LocalObjectLight[0]);
            final JList<LocalObjectLight> lstMalformedSubnets = new JList<>(malformedSubnetsArray);
            
            final JFrame frame = new JFrame("Malformed Subnets");
            frame.setLayout(new BorderLayout());
            frame.setSize(400, 350);
            frame.setLocationRelativeTo(null);
            JLabel lblInstructions = new JLabel("This subnet are malformed");
            lblInstructions.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            frame.add(lblInstructions, BorderLayout.NORTH);
            frame.add(lstMalformedSubnets, BorderLayout.CENTER);
            frame.setVisible(true);
            JPanel pnlButtons = new JPanel();
            pnlButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
            JButton btnClose = new JButton("Close");
            btnClose.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    frame.dispose();
                }
            });
            pnlButtons.add(btnClose);
            frame.add(pnlButtons, BorderLayout.SOUTH);
            
            //sortSubnets.addAll(malformedSubnets);
        }
        
        return sortSubnets;
    }
    
    private boolean isNumeric(String str) { 
        try {  
          Double.parseDouble(str);  
          return true;
        } catch(NumberFormatException e){  
          return false;  
        }  
    }
    
    private static boolean isHexNumber (String cadena) {
        try {
          Long.parseLong(cadena, 16);
          return true;
        }
        catch (NumberFormatException ex) {
          // Error handling code...
          return false;
        }
    }
}
