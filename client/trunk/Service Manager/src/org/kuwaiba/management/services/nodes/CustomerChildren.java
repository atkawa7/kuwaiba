/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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

import java.util.Collections;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPool;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Node representing a customer
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class CustomerChildren extends Children.Keys<LocalPool> {
    
    @Override
    public void addNotify() {
        LocalObjectLight customer = ((CustomerNode)this.getNode()).getObject();
        
        List<LocalPool> servicePools = CommunicationsStub.getInstance().
                getPoolsInObject(customer.getClassName(), customer.getId(), Constants.CLASS_GENERICSERVICE);

        if (servicePools == null) {
            setKeys(Collections.EMPTY_LIST);
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, 
                    CommunicationsStub.getInstance().getError());
        }
        else {
            Collections.sort(servicePools);
            setKeys(servicePools);
        }
    }
    
    @Override
    public void removeNotify() {
        setKeys(Collections.EMPTY_SET);
    }
    
    @Override
    protected Node[] createNodes(LocalPool key) {
        return new Node[] { new ServicePoolNode(new LocalPool(key.getId(), key.getName(), key.getClassName(), null, -1))};
  
    }
}