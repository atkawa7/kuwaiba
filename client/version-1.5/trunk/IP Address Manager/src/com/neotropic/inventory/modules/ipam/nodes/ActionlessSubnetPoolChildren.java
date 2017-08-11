/**
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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

import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPool;
import org.openide.nodes.Node;

/**
 * The same SubnetPoolChildren, but creates ActionlessSubnetPoolNodes or 
 * ActionlessSubnetNodes instead of SubnetPoolNodes or SubnetNodes
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ActionlessSubnetPoolChildren extends SubnetPoolChildren {
    
    @Override
    protected Node[] createNodes(LocalObjectLight key) {
        
        if(key instanceof LocalPool)
            return new Node[] { new ActionlessSubnetPoolNode((LocalPool) key) };
        else
            return new Node[] { new ActionlessSubnetNode(key) };
    }

    
}
