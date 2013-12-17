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
package org.inventory.customization.attributecustomizer.nodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.metadata.LocalAttributeMetadata;
import org.inventory.core.services.api.metadata.LocalClassMetadata;
import org.inventory.core.services.api.metadata.LocalClassMetadataLight;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * The children of a class node representing an attribute
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class AttributeMetadataChildren extends Children.Array{
    List<LocalAttributeMetadata> keys;
    LocalClassMetadataLight lcml;
    
    public AttributeMetadataChildren(LocalClassMetadataLight _lcml){
        this.lcml = _lcml;
        keys = new ArrayList<LocalAttributeMetadata>();
    }

    @Override
    public Collection<Node> initCollection(){
        List<Node> myNodes = new ArrayList<Node>();
        for (LocalAttributeMetadata lam : keys)
            myNodes.add(new AttributeMetadataNode(lam));
        return myNodes;
    }

    @Override
    public void addNotify(){
        LocalClassMetadata lcm = CommunicationsStub.getInstance().getMetaForClass(lcml.getClassName(),true);
        keys.addAll(Arrays.asList(lcm.getAttributes()));
        initCollection();
    }
}
