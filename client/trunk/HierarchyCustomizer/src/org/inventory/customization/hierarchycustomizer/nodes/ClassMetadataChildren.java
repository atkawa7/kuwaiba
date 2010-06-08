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
 *  under the License.
 */
package org.inventory.customization.hierarchycustomizer.nodes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.interfaces.LocalClassMetadataLight;
import org.openide.nodes.Children.Array;
import org.openide.nodes.Node;
/**
 * Represents a class node.
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class ClassMetadataChildren extends Array{

    private boolean main;
    List<LocalClassMetadataLight> keys;

    public ClassMetadataChildren(List<LocalClassMetadataLight> lcm){
        this.main = true;
        this.keys= new ArrayList<LocalClassMetadataLight>(lcm);
    }

    public ClassMetadataChildren(){
        this.main = false;
        this.keys= new ArrayList<LocalClassMetadataLight>();
    }

    @Override
    protected Collection<Node> initCollection (){
        List<Node> myNodes = new ArrayList<Node>();
        for (LocalClassMetadataLight lcml : keys)
            if (main) // This is kinda weird, because
                myNodes.add(new ClassMetadataNode(lcml,main));
            else
                myNodes.add(new ClassMetadataNode(lcml));
        return myNodes;
    }

    @Override
    public void addNotify(){
        if (this.getNode() instanceof ClassMetadataNode){ //Ignores the root node
            LocalClassMetadataLight lcm = ((ClassMetadataNode)this.getNode()).getObject();
            List children = CommunicationsStub.getInstance().getPossibleChildrenNoRecursive(
                    lcm.getPackageName()+"."+lcm.getClassName());
            
            keys = new ArrayList<LocalClassMetadataLight>();
            keys.addAll(children);
        
        }
    }

    public LocalClassMetadataLight[] getKeys(){
        LocalClassMetadataLight[] myKeys = new LocalClassMetadataLight[getNodes().length];

        int i = 0;
        for (Node n : getNodes()){
            myKeys[i] = ((ClassMetadataNode)n).getObject();
            i++;
        }

        return myKeys;
    }
}