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
package com.neotropic.inventory.modules.ipam.nodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.applicationnodes.objectnodes.AbstractChildren;

import org.openide.nodes.Node;

/**
 * Children for subnet nodes
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class SubnetChildren extends AbstractChildren{
    
    @Override
    public void addNotify(){
        LocalObjectLight subnet = ((SubnetNode)getNode()).getObject();

        List<LocalObjectLight> ips = CommunicationsStub.getInstance().
                getSubnetUsedIps(subnet.getOid(), subnet.getClassName());
        List<LocalObjectLight> subnets = CommunicationsStub.getInstance().
                getSubnetsInSubent(subnet.getOid(), subnet.getClassName());
        
        if (ips == null)
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        
        if (subnets == null)
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        
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
    
    
    
    
    
    
    
    private List<LocalObjectLight> sortIps(List<LocalObjectLight> ips){
        
        List<LocalObjectLight> sortIps = ips;
        for (int i = 0; i < sortIps.size(); i++) {
            for (int j = i; j< sortIps.size(); j++) {
                LocalObjectLight auxIp = null;
                String[] aIp = sortIps.get(i).getName().split("\\.");
                String[] bIp = sortIps.get(j).getName().split("\\.");

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
        }
        return sortIps;
    }
    
    private List<LocalObjectLight> sortSubnets(List<LocalObjectLight> subnets){
        
        List<LocalObjectLight> sortSubnets = subnets;
            
        for (int i = 0; i < sortSubnets.size(); i++) {
            for (int j = i; j< sortSubnets.size(); j++) {
                LocalObjectLight auxSubnet = null;

                String[] aIpCIDRsplit = sortSubnets.get(i).getName().split("/");
                String[] aSubnetSplit = aIpCIDRsplit[0].split("\\.");

                String[] bIpCIDRsplit = sortSubnets.get(j).getName().split("/");
                String[] bSubnetSplit = bIpCIDRsplit[0].split("\\.");

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
        }
        
        return sortSubnets;
    }
}
