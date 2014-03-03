/*
 *  Copyright 2010-2014 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kuwaiba.management.services.nodes;

import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Children for CustomerPoolNode
 * @author adrian martinez molina <adrian.martinez@kuwaiba.org>
 */
public class CustomersPoolChildren extends Children.Array{
    
    private LocalObjectLight customer;

    public CustomersPoolChildren(LocalObjectLight customer) {
        this.customer = customer;
    }
    
    @Override
    public void addNotify(){
        List<LocalObjectLight> customersPool = CommunicationsStub.getInstance().getPoolItems(customer.getOid());
        if (customersPool == null)
            Lookup.getDefault().lookup(NotificationUtil.class).
                        showSimplePopup("Error", NotificationUtil.ERROR, CommunicationsStub.getInstance().getError());
        else{
            for (LocalObjectLight c : customersPool){
                CustomerNode newNode = new CustomerNode(c);
                remove(new Node[]{newNode});
                add(new Node[]{newNode});
           }
        }
    }
}
