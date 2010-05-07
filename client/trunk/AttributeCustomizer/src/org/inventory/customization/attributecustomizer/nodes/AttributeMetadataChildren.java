/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.inventory.customization.attributecustomizer.nodes;

import org.inventory.core.services.interfaces.LocalAttributeMetadata;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class AttributeMetadataChildren extends Children.Keys<LocalAttributeMetadata>{
    public AttributeMetadataChildren(LocalAttributeMetadata[] lam){
        setKeys(lam);
    }

    @Override
    protected Node[] createNodes(LocalAttributeMetadata t) {
       return new Node[]{new AttributeMetadataNode(t)};
    }
}
