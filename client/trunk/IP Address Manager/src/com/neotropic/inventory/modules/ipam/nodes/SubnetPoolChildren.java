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
package com.neotropic.inventory.modules.ipam.nodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPool;
import org.inventory.navigation.navigationtree.nodes.AbstractChildren;
import org.openide.nodes.Node;

/**
 * Children for subnet pool nodes
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class SubnetPoolChildren extends AbstractChildren {

    public SubnetPoolChildren() {
        setKeys(Collections.EMPTY_SET);
    }

    public SubnetPoolChildren(List<LocalPool> subnetPools) {
        setKeys(subnetPools);
    }
    
    @Override
    public void addNotify(){
        LocalPool selectedPool = ((SubnetPoolNode)getNode()).getSubnetPool();
        
        List<LocalPool> pools = CommunicationsStub.getInstance().getSubnetPools(selectedPool.getId(), selectedPool.getClassName());
        List<LocalObjectLight> subnets = CommunicationsStub.getInstance().getSubnets(selectedPool.getId());
        List<LocalObjectLight> all = new ArrayList<>();
        if (subnets != null){
            for (LocalObjectLight subnet : subnets) 
                all.add(subnet);
        }
        
        if (pools != null){
            for (LocalPool pool : pools) 
                all.add(pool);
        }
        Collections.sort(all);
        setKeys(all);
    }
    
    @Override
    protected void removeNotify() {
        setKeys(Collections.EMPTY_SET);
    } 

    @Override
    protected Node[] createNodes(LocalObjectLight key) {
        if(key instanceof LocalPool)
            return new Node[] { new SubnetPoolNode((LocalPool)key) };
        else
            return new Node[] { new SubnetNode(key) };
    }
}
