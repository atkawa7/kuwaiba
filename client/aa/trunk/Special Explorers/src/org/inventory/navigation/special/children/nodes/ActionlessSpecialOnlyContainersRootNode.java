/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>
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

import java.util.List;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Is a hidden root node for SpecialObjectNode, this node has no actions 
 * (is used in the wizard physical link connection to show the existing wire containers)
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class ActionlessSpecialOnlyContainersRootNode extends AbstractNode{

    public ActionlessSpecialOnlyContainersRootNode(ActionlessSpecialOnlyContainersRootChildren children) {
        super(children);
    }
    
    public static class ActionlessSpecialOnlyContainersRootChildren extends Children.Keys <LocalObjectLight> {
        public ActionlessSpecialOnlyContainersRootChildren(List<LocalObjectLight> lols){
            setKeys(lols);
        }
        
        @Override
        public void addNotify(){}

        @Override
        protected Node[] createNodes(LocalObjectLight key) {
            return new Node[] { new ActionlessSpecialFilteredObjectNode(key, Constants.CLASS_WIRECONTAINER) };
        }
    }
}
