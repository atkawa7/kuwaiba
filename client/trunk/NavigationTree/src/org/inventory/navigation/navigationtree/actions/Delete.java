package org.inventory.navigation.navigationtree.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.interfaces.NotificationUtil;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.openide.util.Lookup;

public final class Delete extends AbstractAction {

    private ObjectNode node;

    public Delete(ObjectNode _node) {
        putValue(NAME, java.util.ResourceBundle.getBundle("org/inventory/navigation/navigationtree/Bundle").getString("LBL_DELETE"));
        this.node = _node;
    }

    public void actionPerformed(ActionEvent ev) {

        if(JOptionPane.showConfirmDialog(null, java.util.ResourceBundle.getBundle("org/inventory/navigation/navigationtree/Bundle").getString("LBL_SURE"),
                java.util.ResourceBundle.getBundle("org/inventory/navigation/navigationtree/Bundle").getString("LBL_CONFIRMATIOn"),JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION){

            NotificationUtil nu = Lookup.getDefault().lookup(NotificationUtil.class);
            if (CommunicationsStub.getInstance().removeObject(node.getObject().getPackageName()+"."+node.getObject().getClassName(),
                    node.getObject().getOid())){
                firePropertyChange(NAME, "remove", node.getObject());
                nu.showSimplePopup(java.util.ResourceBundle.getBundle("org/inventory/navigation/navigationtree/Bundle").getString("LBL_DELETION_TITLE"), NotificationUtil.INFO, java.util.ResourceBundle.getBundle("org/inventory/navigation/navigationtree/Bundle").getString("LBL_DELETION_TEXT_OK"));                
            }
            else
                nu.showSimplePopup(java.util.ResourceBundle.getBundle("org/inventory/navigation/navigationtree/Bundle").getString("LBL_DELETION_TEXT_ERROR"),
                        NotificationUtil.ERROR, CommunicationsStub.getInstance().getError());
        }
    }
}
