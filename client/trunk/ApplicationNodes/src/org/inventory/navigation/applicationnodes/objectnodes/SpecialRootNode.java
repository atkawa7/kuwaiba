/*
 *  Copyright 2010-2014 Neotropic SAS <contact@neotropic.co>.
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

import java.awt.Image;
import java.util.HashMap;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Dummy class to represent a node in the special relationships tree
 */
public class SpecialRootNode extends AbstractNode {
    
    //Icon to be used for children
    private Image childrenIcon;
    
    public SpecialRootNode() {
        super (new Children.Array());
        setDisplayName("No relationships to show");
        setIconBaseWithExtension(RootObjectNode.DEFAULT_ICON_PATH);
    }
    
    public SpecialRootNode (HashMap<String, LocalObjectLight[]> directChildren){
        this();
        if (directChildren != null) {
            for (String directChildLabel : directChildren.keySet())
                getChildren().add(new SpecialObjectNode[]{new SpecialObjectNode(directChildLabel, directChildren.get(directChildLabel))});
        }            
    }
    
    public SpecialRootNode (LocalObjectLight[] directChildren){
        this();
        if (directChildren != null) {
            for (LocalObjectLight directChild : directChildren)
                getChildren().add(new SpecialObjectNode[]{new SpecialObjectNode(directChild)});
        }            
    }
    
    public void setChildrenIcon(Image childrenIcon){
        this.childrenIcon = childrenIcon;
    }
    
    private class SpecialObjectNode extends AbstractNode {
        public SpecialObjectNode (String label, LocalObjectLight[] relatedObjects){
            super(new SpecialObjectChildren(relatedObjects));
            this.setDisplayName(label);
        }
        
        public SpecialObjectNode (LocalObjectLight anObject){
            super(new SpecialObjectChildren(anObject));
            this.setDisplayName(anObject.getName());
        }

        @Override
        public Image getIcon(int type) {
            if (childrenIcon == null)
                return super.getIcon(type);
            return childrenIcon;
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }
    }
    private class SpecialObjectChildren extends Children.Array {
        private LocalObjectLight[] relatedObjects;
        private LocalObjectLight anObject;

        public SpecialObjectChildren(LocalObjectLight[] relatedObjects){
            this.relatedObjects = relatedObjects;
        }

        public SpecialObjectChildren(LocalObjectLight anObject){
            this.anObject = anObject;
        }
        
        @Override
        protected void addNotify() {
            if (anObject != null)
                relatedObjects = CommunicationsStub.getInstance().
                        getObjectSpecialChildren(anObject.getClassName(), anObject.getOid());
            
            for (LocalObjectLight item : relatedObjects){
                ObjectNode newNode = new ObjectNode(item, true);
                add(new Node[]{newNode});
            }
        }
    }
}
    
