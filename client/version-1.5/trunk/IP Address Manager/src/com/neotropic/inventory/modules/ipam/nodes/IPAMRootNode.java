/*
 * Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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

import java.awt.Image;
import java.util.Collections;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPool;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * This is the root node for all the IPAM Nodes, this is not visible
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class IPAMRootNode extends AbstractNode {
    
    /**
     * Main constructor
     * @param subnetPools The list of subnet pools to be displayed as roots of the tree
     */
    public IPAMRootNode(IpamSubnetRootPoolChildren subnetPools) {
        super (subnetPools);
    }
    
    @Override
    public Image getOpenedIcon(int i){
        return getIcon(i);
    }
    
    public static class IpamSubnetRootPoolChildren extends Children.Keys <LocalPool> {

        @Override
        public void addNotify(){
        List<LocalPool> pools = CommunicationsStub.getInstance().getSubnetPools(-1, null);
        Collections.sort(pools);
        setKeys(pools);
    }

        @Override
        protected Node[] createNodes(LocalPool key) {
            return new Node[] { new SubnetPoolNode((LocalPool)key) };
        }
    }
}
