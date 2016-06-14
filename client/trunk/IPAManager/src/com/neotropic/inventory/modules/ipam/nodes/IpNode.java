/**
 * Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.inventory.modules.ipam.nodes;

import org.inventory.communications.core.LocalObjectLight;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;


/**
 * Represents an IPv4 or an IPv6 inside of a subnet
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class IpNode extends ObjectNode{

    public IpNode(LocalObjectLight lol) {
        super(lol, true);
    }
    
//    public IpNode(LocalObjectLight lol) {
//        super(lol, i);
//    }
    
    @Override
    public boolean canRename() {
        return false;
    }
}
