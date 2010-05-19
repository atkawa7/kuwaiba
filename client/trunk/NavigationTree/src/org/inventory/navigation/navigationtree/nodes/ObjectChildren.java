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
package org.inventory.navigation.navigationtree.nodes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.interfaces.LocalObjectLight;
import org.openide.nodes.Children.Keys;
import org.openide.nodes.Node;

/**
 * Represents the children for the navigation tree
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class ObjectChildren extends Keys<LocalObjectLight> implements PropertyChangeListener{

    private List<LocalObjectLight> keys;
    public ObjectChildren(LocalObjectLight[] _lols){
        setKeys(_lols);
        keys = new ArrayList<LocalObjectLight>();
        for (LocalObjectLight lol : _lols)
            keys.add(lol);
    }

    /*
     * This constructor is used to create a node with no children
     *  since they're going to be created on demand (see method addNotify)
     */
    public ObjectChildren(){
        setKeys(new LocalObjectLight[0]);
        keys = new ArrayList<LocalObjectLight>();
    }

    @Override
    protected Node[] createNodes(LocalObjectLight t) {
        return new Node[]{new ObjectNode(t)};
    }

    /*
     * Creates children nodes on demand
     */
    @Override
    public void addNotify(){
        super.addNotify();
        //The tree root is not an AbstractNode, but a RootObjectNode
        if (this.getNode() instanceof ObjectNode){
            LocalObjectLight node = ((ObjectNode)this.getNode()).getObject();
            List <LocalObjectLight> children = CommunicationsStub.getInstance().getObjectChildren(node.getOid(),node.getClassName());
            setKeys(children);
            
            keys.addAll(children);
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        LocalObjectLight obj = (LocalObjectLight)evt.getNewValue();
        if (evt.getOldValue().equals("add")) // If a new child has been added
            keys.add(obj);
        else{
            if (evt.getOldValue().equals("remove")){ // If a child has been deleted
                keys.remove(obj);
            }else{
                

            }
        }
        setKeys(keys);
    }
}
