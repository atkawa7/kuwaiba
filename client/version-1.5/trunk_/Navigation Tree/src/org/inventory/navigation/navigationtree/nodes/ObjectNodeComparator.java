/*
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
 *  under the License.
 */
package org.inventory.navigation.navigationtree.nodes;

import java.util.Comparator;
import org.openide.nodes.Node;

/**
 * This comparator helps to sort alphabetically the object nodes
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public final class ObjectNodeComparator implements Comparator<Node>{
    private static ObjectNodeComparator instance;
      
    private ObjectNodeComparator() {}
    
    public static ObjectNodeComparator getInstance() {
        if (instance == null)
            instance = new ObjectNodeComparator();
        return instance;
    }
    
    @Override
    public int compare(Node node1, Node node2) {
        return node1.getName().compareTo(node2.getName());
    }
    
}
