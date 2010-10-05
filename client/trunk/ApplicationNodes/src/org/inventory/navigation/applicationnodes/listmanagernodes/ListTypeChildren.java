/*
 *  Copyright 2010 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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

package org.inventory.navigation.applicationnodes.listmanagernodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.inventory.core.services.interfaces.LocalClassMetadataLight;
import org.openide.nodes.Children.Array;
import org.openide.nodes.Node;

/**
 * Represents the children corresponding to list type classes
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class ListTypeChildren extends Array{
    private List<LocalClassMetadataLight> keys;
    public ListTypeChildren(LocalClassMetadataLight[] classes) {
        keys = new ArrayList<LocalClassMetadataLight>();
        keys.addAll(Arrays.asList(classes));
    }

    @Override
    protected Collection<Node> initCollection(){
        List<Node> myNodes = new ArrayList<Node>();
        for (LocalClassMetadataLight myClass : keys)
            myNodes.add(new ListTypeNode(myClass));
        return myNodes;
    }
}
