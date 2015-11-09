/*
 *  Copyright 2010-2015 Neotropic SAS <contact@neotropic.co>.
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
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.openide.nodes.Children;

/**
 * All children under a Service node
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ServiceChildren extends Children.Array {
    private LocalObjectLight service;
    private boolean collapsed;
    
    public ServiceChildren(LocalObjectLight service) {
        this.service = service;
        collapsed = true;
    }
    
    @Override
    protected void addNotify() {
        collapsed = false;
        if(!service.getClassName().equals(Constants.CLASS_GENERICSERVICE)){
            LocalObjectLight[] resources = CommunicationsStub.getInstance().
                    getServiceResources(service.getClassName(), service.getOid());
            if (resources == null)
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            else{
                for (LocalObjectLight resource : resources)
                    add(new ObjectNode[] {new ObjectNode(resource)});
            }
        }
        else{
            List<LocalObjectLight> items = CommunicationsStub.getInstance().getPoolItems(service.getOid());
            if (items == null)
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            else{
                for (LocalObjectLight item : items)
                    add(new ServiceNode[]{new ServiceNode(item)});
            }
        }
    }

    public boolean isCollapsed() {
        return collapsed;
    }
}
