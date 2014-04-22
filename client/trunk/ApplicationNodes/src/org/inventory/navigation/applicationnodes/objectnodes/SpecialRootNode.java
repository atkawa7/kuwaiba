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

import java.util.HashMap;
import org.inventory.communications.core.LocalObjectLight;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;

/**
 * Dummy class to represent a node in the special relationships tree
 */
public class SpecialRootNode extends AbstractNode {  
    /**
     * Current Object being displayed
     */
    private LocalObjectLight currentObject;
    
    public SpecialRootNode() {
        super (new Children.Array());
        setIconBaseWithExtension(RootObjectNode.DEFAULT_ICON_PATH);
        setDisplayName("Nothing to show");
    }
    
    public SpecialRootNode (LocalObjectLight rootObject, HashMap<String, LocalObjectLight[]> children){
        this();
        currentObject = rootObject;
        for (String label : children.keySet())
            getChildren().add(new LabelNode[]{new LabelNode(label, children.get(label))});
    }
    
    public SpecialRootNode (LocalObjectLight rootObject){
        this();
        currentObject = rootObject;
        getChildren().add(new SpecialObjectNode[]{new SpecialObjectNode(rootObject)});
    }

    public LocalObjectLight getCurrentObject() {
        return currentObject;
    }
}