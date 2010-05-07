/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.inventory.customization.attributecustomizer.nodes;

import org.inventory.core.services.interfaces.LocalClassMetadata;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
/**
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class ClassMetadataChildren extends Children.Keys<LocalClassMetadata> {
    
    public ClassMetadataChildren(LocalClassMetadata[] lcm){
        setKeys(lcm);
    }

    @Override
    protected Node[] createNodes(LocalClassMetadata t) {
        return new Node[]{new ClassMetadataNode(t)};
    }
}
