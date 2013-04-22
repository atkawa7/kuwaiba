/*
 *  Copyright 2010, 2011, 2012, 2013 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.navigation.applicationnodes.metadataclassnodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.metadata.LocalClassMetadataLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.openide.nodes.Children.Array;
import org.openide.nodes.Node;
import org.openide.util.Lookup;


/**
 * Represents the children for the navigation tree
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class ClassMetadataChildren extends Array{
    
    protected List<LocalClassMetadataLight> keys;

    /**
     * This constructor is used to create a node with no children
     *  since they're going to be created on demand (see method addNotify)
     */
    public ClassMetadataChildren(){
    }
    
    public ClassMetadataChildren(LocalClassMetadataLight[] _lcls) {
        keys = new ArrayList<LocalClassMetadataLight>();
        keys.addAll(Arrays.asList(_lcls));
    }

    @Override
    protected Collection<Node> initCollection(){
        List<Node> myNodes = new ArrayList<Node>();
        if (keys == null){
            keys = new ArrayList<LocalClassMetadataLight>();
        }
        for (LocalClassMetadataLight lol : keys){
            myNodes.add(new ClassMetadataNode(lol));
        }
        return myNodes;
    }
    
    /**
     * Creates children nodes on demand
     */
    @Override
    public void addNotify(){
        //The tree root is not an AbstractNode, but a RootObjectNode
        if (this.getNode() instanceof ClassMetadataNode){
            if (keys == null){
                keys = new ArrayList<LocalClassMetadataLight>();
            }
            CommunicationsStub com = CommunicationsStub.getInstance();
            LocalClassMetadataLight node = ((ClassMetadataNode)this.getNode()).getClassMetadata();
            List <LocalClassMetadataLight> children = com.getLightSubclassesNoRecursive(node.getClassName(), true, false);
             if (children == null){
                NotificationUtil  nu = Lookup.getDefault().lookup(NotificationUtil.class);
                nu.showSimplePopup("Error", NotificationUtil.ERROR, "An error has occurred retrieving this Metadata Sub Class: "+com.getError());
            }else{
                 for (LocalClassMetadataLight child : children){
                     ClassMetadataNode newNode = new ClassMetadataNode(child);
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
        if (keys != null){
            keys.clear();
        }
    }

    public List<LocalClassMetadataLight> getKeys() {
        return keys;
    }

    @Override
    public boolean add(Node[] arr) {
        for (Node node : arr){
            if (node instanceof ClassMetadataNode){
                if (keys == null){
                    keys = new ArrayList<LocalClassMetadataLight>();
                }
                if (!keys.contains(((ClassMetadataNode)node).getClassMetadata())){
                    keys.add(((ClassMetadataNode)node).getClassMetadata());
                }
            }
        }
        return super.add(arr);
    }
    
    @Override
    public boolean remove(Node[] arr) {
        for (Node node : arr){
            if (node instanceof ClassMetadataNode){
                if (keys == null){
                    keys = new ArrayList<LocalClassMetadataLight>();
                }
                keys.remove(((ClassMetadataNode)node).getClassMetadata());
            }
        }
        return super.remove(arr);
    }

}
