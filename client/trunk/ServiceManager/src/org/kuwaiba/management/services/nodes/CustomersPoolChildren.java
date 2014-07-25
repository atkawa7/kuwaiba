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

/**
 * Children for CustomerPoolNode
 * @author adrian martinez molina <adrian.martinez@kuwaiba.org>
 */
public class CustomersPoolChildren extends Children.Array{
    
    private LocalObjectLight customer;
    private boolean collapsed;

    public CustomersPoolChildren(LocalObjectLight customer) {
        this.customer = customer;
        collapsed = true;
    }
    
    @Override
    public void addNotify(){
        collapsed = false;
        List<LocalObjectLight> customersPool = CommunicationsStub.getInstance().getPoolItems(customer.getOid());
        if (customersPool == null)
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        else{
            for (LocalObjectLight aCustomer : customersPool)
                add(new CustomerNode[]{ new CustomerNode(aCustomer)});
        }
    }

    public boolean isCollapsed() {
        return collapsed;
    }
}
