/**
 * Copyright 2010-2015 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.navigation.applicationnodes.pools;

import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Children for pool nodes
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class PoolChildren extends Children.Array{

    private LocalObjectLight pool;
    private boolean collapsed;
    
    public PoolChildren(LocalObjectLight pool) {
        this.pool = pool;
        collapsed = true;
    }
    
    @Override
    public void addNotify(){
        collapsed = false;
        List<LocalObjectLight> items = CommunicationsStub.getInstance().getPoolItems(pool.getOid());
        if (items == null)
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        else{
            for (LocalObjectLight item : items)
                add(new Node[]{new ObjectNode(item)});
        }
    }

    public boolean isCollapsed() {
        return collapsed;
    }
}
