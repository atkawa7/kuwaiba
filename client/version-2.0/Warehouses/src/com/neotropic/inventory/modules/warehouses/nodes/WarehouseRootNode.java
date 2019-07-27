/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.inventory.modules.warehouses.nodes;

import java.util.Collections;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPool;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class WarehouseRootNode extends AbstractNode {
    
    public WarehouseRootNode(WarehouseRootNodeChildren children) {
        super(children);
    }
    
    public static class WarehouseRootNodeChildren extends Children.Keys<LocalPool> {
        
        public void addNotify() {
            List<LocalPool> pools = CommunicationsStub.getInstance().getWarehouseRootPool();
            Collections.sort(pools);
            setKeys(pools);
        }
        
        @Override
        protected Node[] createNodes(LocalPool localPool) {
            return new Node[] {new WarehouseRootPoolNode(localPool)};
        }
    }
}
