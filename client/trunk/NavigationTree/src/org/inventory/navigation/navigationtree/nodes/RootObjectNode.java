package org.inventory.navigation.navigationtree.nodes;

import javax.swing.Action;
import org.inventory.navigation.navigationtree.actions.Create;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;

/**
 * Simple class to represent the root node
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class RootObjectNode extends AbstractNode{

    public RootObjectNode(Children _children) {
        super(_children);
    }

    @Override
    public Action[] getActions(boolean context){
        Create createAction = new Create(this);
        createAction.addPropertyChangeListener((ObjectChildren)this.getChildren());
        return new Action[]{createAction};
    }

}
