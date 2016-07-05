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

import java.util.Collections;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
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
        
        List<LocalObjectLight> ips = CommunicationsStub.getInstance().getObjectSpecialChildren(subnet.getClassName(), subnet.getOid());
        
        if (ips == null)
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        else {
            Collections.sort(ips);
            setKeys(ips);
        }
    }
    
    @Override
    protected void removeNotify() {
        setKeys(Collections.EMPTY_SET);
    } 

    @Override
    protected Node[] createNodes(LocalObjectLight key) {
        return new Node[] { new IPAddressNode(key) };
    }
}
