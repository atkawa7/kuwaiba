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
package com.neotropic.inventory.modules.ipam.nodes;

import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPool;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Children for subnet pool nodes
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class SubnetPoolChildren extends Children.Array{
    
    private LocalObjectLight subnetPool;
    

    public SubnetPoolChildren(LocalObjectLight subnetPool) {
        this.subnetPool = subnetPool;
    }
    
    @Override
    public void addNotify(){
        List<LocalPool> pools = CommunicationsStub.getInstance().getSubnetPools(subnetPool.getOid(), subnetPool.getClassName());
        if (pools == null)
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        else{
            for (LocalPool item : pools)
                add(new Node[]{new SubnetPoolNode(item)});
        }
        List<LocalObjectLight> subnets = CommunicationsStub.getInstance().getSubnets(subnetPool.getOid());
        if (pools == null)
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        else{
            for (LocalObjectLight item : subnets)
                add(new Node[]{new SubnetNode(item)});
        }
    }
}
