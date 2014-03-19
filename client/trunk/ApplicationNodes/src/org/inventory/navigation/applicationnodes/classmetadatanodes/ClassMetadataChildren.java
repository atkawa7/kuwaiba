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
package org.inventory.navigation.applicationnodes.classmetadatanodes;

import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.openide.nodes.Children.Array;
import org.openide.nodes.Node;

/**
 * Represents the children for the navigation tree
 * @author Adrian Martinez Molina <charles.bedon@kuwaiba.org>
 */
public class ClassMetadataChildren extends Array{
    
    protected List<LocalClassMetadataLight> keys;

    /**
     * This constructor is used to create a node with no children
     *  since they're going to be created on demand (see method addNotify)
     */
    public ClassMetadataChildren(){
    }   
    
    public ClassMetadataChildren(LocalClassMetadataLight[] lcls) {
        for (LocalClassMetadataLight lcml : lcls){
            ClassMetadataNode newNode = new ClassMetadataNode(lcml);
            add(new Node[]{newNode});
        }
    }
   
    /**
     * Creates children nodes on demand
     */
    @Override
    public void addNotify(){     
        
        if (!(this.getNode() instanceof ClassMetadataNode))
            return;
        
        CommunicationsStub com = CommunicationsStub.getInstance();
        ClassMetadataNode node = ((ClassMetadataNode)this.getNode());
        LocalClassMetadataLight[] subClasses = com.getLightSubclassesNoRecursive(node.getClassMetadata().getClassName(), true, false);
        if (subClasses == null)
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, "An error has occurred retrieving this Metadata Sub Class: "+com.getError());
        else{
             for (LocalClassMetadataLight subClass : subClasses)
                 add(new Node[]{new ClassMetadataNode(subClass)});
        }
    }
}
