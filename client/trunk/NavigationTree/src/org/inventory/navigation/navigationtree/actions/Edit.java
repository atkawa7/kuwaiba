package org.inventory.navigation.navigationtree.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.inventory.navigation.navigationtree.ObjectEditorTopComponent;
import org.openide.nodes.Node;

/*
 * Provides the necessary functionality to show a dedicated editor (using PropertySheetView)
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public final class Edit extends AbstractAction {
    private Node node;

    public Edit(Node _node) {
        this.node = _node;
        putValue(NAME, java.util.ResourceBundle.getBundle("org/inventory/navigation/navigationtree/Bundle").getString("LBL_EDIT"));
    }

    public void actionPerformed(ActionEvent ev) {
        ObjectEditorTopComponent component = new ObjectEditorTopComponent(new Node[]{node});
        component.open();
        component.requestActive();
    }
}