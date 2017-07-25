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

import java.util.List;
import org.inventory.communications.core.LocalPool;
import org.openide.nodes.Children;

/**
 * The same IPAMRootNode, but without actions (useful in views inside wizards, where the selected nodes are not placed in the global lookup, so the the context actions,
 * that are dependant of the selected nodes won't crash)
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ActionlessIPAMRootNode extends IPAMRootNode {

    public ActionlessIPAMRootNode(List<LocalPool> subnetPools) {
        super(subnetPools);
        setChildren(new Children.Array());
        for (LocalPool subnetPool : subnetPools)
            getChildren().add(new ActionlessSubnetPoolNode[] { new ActionlessSubnetPoolNode(subnetPool) });
    }
    
}
