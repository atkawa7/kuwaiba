package org.inventory.customization.hierarchycustomizer.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.interfaces.NotificationUtil;
import org.inventory.customization.hierarchycustomizer.nodes.ClassMetadataChildren;
import org.inventory.customization.hierarchycustomizer.nodes.ClassMetadataNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Implements the "remove a class from container hierarchy" action
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class Remove extends AbstractAction{

    ClassMetadataNode node;

    public Remove(){}
    public Remove(ClassMetadataNode _node){
        putValue(NAME, java.util.ResourceBundle.getBundle("org/inventory/customization/hierarchycustomizer/Bundle").getString("LBL_REMOVE"));
        this.node = _node;
    }

    public void actionPerformed(ActionEvent e) {
        List<Long> oids = new ArrayList<Long>();
        NotificationUtil nu = Lookup.getDefault().lookup(NotificationUtil.class);
        CommunicationsStub com = CommunicationsStub.getInstance();
        oids.add(node.getObject().getOid());
        if (com.removePossibleChildren(
                ((ClassMetadataNode)node.getParentNode()).getObject().getOid(),oids)){

            ((ClassMetadataChildren)node.getParentNode().getChildren()).remove(new Node[]{node});
            com.refreshCache(false, false, false, true);

            nu.showSimplePopup(java.util.ResourceBundle.getBundle("org/inventory/customization/hierarchycustomizer/Bundle").getString("LBL_HIERARCHY_UPDATE_TITLE"),
                    NotificationUtil.INFO,
                    java.util.ResourceBundle.getBundle("org/inventory/customization/hierarchycustomizer/Bundle").getString("LBL_HIERARCHY_UPDATE_TEXT"));
        }
        else
            nu.showSimplePopup(java.util.ResourceBundle.getBundle("org/inventory/customization/hierarchycustomizer/Bundle").getString("LBL_HIERARCHY_UPDATE_TITLE"),
                    NotificationUtil.INFO,com.getError());
    }
}