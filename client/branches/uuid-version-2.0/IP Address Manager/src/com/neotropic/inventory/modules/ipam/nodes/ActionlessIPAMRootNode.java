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
package com.neotropic.inventory.modules.ipam.nodes;

import java.util.Collections;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPool;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * The same IPAMRootNode, but without actions (useful in views inside wizards, 
 * where the selected nodes are not placed in the global lookup, so the 
 * context actions, that are dependant of the selected nodes won't crash)
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ActionlessIPAMRootNode extends AbstractNode {

    public ActionlessIPAMRootNode(actionlessIpamSubnetRootPoolChildren subnetPools) {
        super(subnetPools);
    }
    
    public static class actionlessIpamSubnetRootPoolChildren extends Children.Keys <LocalPool> {

        @Override
        public void addNotify(){
        List<LocalPool> pools = CommunicationsStub.getInstance().getSubnetPools("-1", null);
        Collections.sort(pools);
        setKeys(pools);
    }

        @Override
        protected Node[] createNodes(LocalPool key) {
            return new Node[] { new ActionlessSubnetPoolNode((LocalPool)key) };
        }
    }
    
}
