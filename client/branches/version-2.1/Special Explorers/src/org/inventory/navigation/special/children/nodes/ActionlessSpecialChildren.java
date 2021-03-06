/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 */
package org.inventory.navigation.special.children.nodes;

import org.inventory.communications.core.LocalObjectLight;
import org.openide.nodes.Node;

/**
 * The same SpecialChildren, but creates ActionlessSpecialObjectNodes instead of SpecialObjectNodes
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ActionlessSpecialChildren extends SpecialChildren {

    public ActionlessSpecialChildren() {
        super();
    }
    
    @Override
    protected Node[] createNodes(LocalObjectLight key) {
        return new Node[]  { new ActionlessSpecialObjectNode(key)};
    }
}
