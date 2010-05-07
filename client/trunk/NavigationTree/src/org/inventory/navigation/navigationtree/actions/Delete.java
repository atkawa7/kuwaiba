package org.inventory.navigation.navigationtree.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.interfaces.LocalObjectLight;
import org.inventory.core.services.interfaces.NotificationUtil;
import org.inventory.navigation.navigationtree.nodes.ObjectChildren;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.openide.util.Lookup;

public final class Delete extends AbstractAction {

    private ObjectNode node;

    public Delete(ObjectNode _node) {
        putValue(NAME, "Eliminar");
        this.node = _node;
    }

    public void actionPerformed(ActionEvent ev) {

        if(JOptionPane.showConfirmDialog(null, "¿Está seguro que desea eliminar este objeto?",
                "Confirmación",JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION){

            NotificationUtil nu = Lookup.getDefault().lookup(NotificationUtil.class);
            if (CommunicationsStub.getInstance().removeObject(node.getObject().getPackageName()+"."+node.getObject().getClassName(),
                    node.getObject().getOid())){

                LocalObjectLight[] lols = new LocalObjectLight[node.getChildren().getNodesCount()-1];

                for (int i = 0; i< node.getChildren().getNodesCount();i++){
                    /*ObjectNode nod = ((ObjectNode)node.getChildren().getNodes()[i]).getObject();
                    if (!nod.equals(node))
                    lols[i] = node;*/

                }

                //((ObjectChildren)node.getChildren()).setNewKeys(lols);


                nu.showSimplePopup("Eliminación de Objeto", NotificationUtil.INFO, "El objeto fue eliminado exitosamente");
                
            }
            else
                nu.showSimplePopup("Error eliminando objeto",
                        NotificationUtil.ERROR, CommunicationsStub.getInstance().getError());
        }
    }
}
