/**
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
package org.inventory.navigation.pools.nodes;

import java.util.Collections;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPool;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.navigationtree.nodes.AbstractChildren;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.openide.nodes.Node;

/**
 * Children for pool nodes
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class PoolChildren extends AbstractChildren {

    private LocalPool pool;
    
    public PoolChildren(LocalPool pool) {
        this.pool = pool;
    }
    
    @Override
    public void addNotify(){
        List<LocalObjectLight> items = CommunicationsStub.getInstance().getPoolItems(pool.getId());
        if (items == null) {
            setKeys(Collections.EMPTY_SET);
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        }
        else {
            Collections.sort(items);
            setKeys(items);
        }
    }
    
    @Override
    protected Node[] createNodes(LocalObjectLight key) {
        return new Node[] { new ObjectNode(key) };
    }
}
