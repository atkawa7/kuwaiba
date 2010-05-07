package org.inventory.customization.hierarchycustomizer.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.inventory.customization.hierarchycustomizer.nodes.ClassMetadataNode;

/**
 * Implements the "remove a class from container hierarchy" action
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class Delete extends AbstractAction{

    ClassMetadataNode node;

    public Delete(){}
    public Delete(ClassMetadataNode _node){
        putValue(NAME, "Remove");
        this.node = _node;
    }

    public void actionPerformed(ActionEvent e) {
        
    }
}