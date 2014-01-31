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

import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

/**
 * Node representing a customer
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ServiceChildren extends Children.Array {
    LocalObjectLight service;
    public ServiceChildren(LocalObjectLight service) {
        this.service = service;
    }
    
    @Override
    protected void addNotify() {
        NotificationUtil nu = Lookup.getDefault().lookup(NotificationUtil.class);
        LocalObjectLight[] resources = CommunicationsStub.getInstance().
                getServiceResources(service.getClassName(), service.getOid());
        
        if (resources == null)
            nu.showSimplePopup("Error", NotificationUtil.ERROR, CommunicationsStub.getInstance().getError());
        else{
            for (LocalObjectLight resource : resources){
                ObjectNode[] node = new ObjectNode[] {new ObjectNode(resource)};
                remove(node);
                add(node);
            }
        }
    }
    
    
    
}
