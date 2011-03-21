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
package org.inventory.navigation.applicationnodes.objectnodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.openide.nodes.Children.Array;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Represents the children for the navigation tree
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class ObjectChildren extends Array{

    protected List<LocalObjectLight> keys;
    public ObjectChildren(LocalObjectLight[] _lols){
        keys = new ArrayList<LocalObjectLight>();
        keys.addAll(Arrays.asList(_lols));
    }

    /**
     * This constructor is used to create a node with no children
     *  since they're going to be created on demand (see method addNotify)
     */
    public ObjectChildren(){
        //keys = new ArrayList<LocalObjectLight>();
    }

    @Override
    protected Collection<Node> initCollection(){
        List<Node> myNodes = new ArrayList<Node>();

        if (keys == null)
            keys = new ArrayList<LocalObjectLight>();

        for (LocalObjectLight lol : keys)
            myNodes.add(new ObjectNode(lol));
        return myNodes;
    }

    /**
     * Creates children nodes on demand
     */
    @Override
    public void addNotify(){
        //The tree root is not an AbstractNode, but a RootObjectNode
        if (this.getNode() instanceof ObjectNode){

            if (keys == null)
                keys = new ArrayList<LocalObjectLight>();

            CommunicationsStub com = CommunicationsStub.getInstance();
            LocalObjectLight node = ((ObjectNode)this.getNode()).getObject();
            List <LocalObjectLight> children = com.getObjectChildren(node.getOid(),
                    com.getMetaForClass(node.getClassName(),false).getOid());
            if (children == null){
                NotificationUtil  nu = Lookup.getDefault().lookup(NotificationUtil.class);
                nu.showSimplePopup("Error", NotificationUtil.ERROR, "An error has occurred retrieving this object's children: "+com.getError());
            }else{
                for (LocalObjectLight child : children){
                    ObjectNode newNode = new ObjectNode(child);
                    // Remove it if it already exists (if this is not done,
                    // it will duplicate the nodes created when the parent was collapsed)
                    keys.remove(child);
                    keys.add(child);
                    remove(new Node[]{newNode});
                    add(new Node[]{newNode});
                }
            }
        }
    }

    @Override
    protected void removeNotify() {
        if (keys != null)
            keys.clear();
    }

    public List<LocalObjectLight> getKeys() {
        return keys;
    }

    @Override
    public boolean add(Node[] arr) {
        for (Node node : arr){
            if (node instanceof ObjectNode){
                if (keys == null)
                    keys = new ArrayList<LocalObjectLight>();

                if (!keys.contains(((ObjectNode)node).getObject()))
                    keys.add(((ObjectNode)node).getObject());
            }
        }
        return super.add(arr);
    }

    @Override
    public boolean remove(Node[] arr) {
        for (Node node : arr){
            if (node instanceof ObjectNode){
                if (keys == null)
                    keys = new ArrayList<LocalObjectLight>();
                
                keys.remove(((ObjectNode)node).getObject());
            }
        }
        return super.remove(arr);
    }

}
