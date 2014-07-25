/*
 *  Copyright 2010 - 2014 Neotropic SAS <contact@neotropic.co>.
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
 * Node representing a customer
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class CustomerChildren extends Children.Array {
    private LocalObjectLight customer;
    private boolean collapsed;
    
    public CustomerChildren(LocalObjectLight customers) {
        this.customer = customers;
        collapsed = true;
    }

    @Override
    protected void addNotify() {
        collapsed = false;
        LocalObjectLight[] services = CommunicationsStub.getInstance().getServices(customer.getClassName(), customer.getOid());
        
        if (services == null)
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        else{
            for (LocalObjectLight service : services)
                add(new ServiceNode[] {new ServiceNode(service)});
        }
        
        List<LocalObjectLight> servicesPools = CommunicationsStub.getInstance().getPools(customer.getOid(), "GenericService");
        if (servicesPools == null)
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        else{
            for (LocalObjectLight servicePool : servicesPools) 
                add(new ServicesPoolNode[]{new ServicesPoolNode(servicePool)});
        }
    }

    public boolean isCollapsed() {
        return collapsed;
    }
}