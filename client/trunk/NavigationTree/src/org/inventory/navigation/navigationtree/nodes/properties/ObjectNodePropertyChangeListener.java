package org.inventory.navigation.navigationtree.nodes.properties;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;

/**
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class ObjectNodePropertyChangeListener implements PropertyChangeListener{
    
    private ObjectNode node;
    
    public ObjectNodePropertyChangeListener(ObjectNode _node){
        this.node = _node;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
