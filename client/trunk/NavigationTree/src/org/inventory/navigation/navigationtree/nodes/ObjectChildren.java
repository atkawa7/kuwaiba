package org.inventory.navigation.navigationtree.nodes;

import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.interfaces.LocalObjectLight;
import org.openide.nodes.Children.Keys;
import org.openide.nodes.Node;

/**
 * Representa los hijos del árbol de navegación
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class ObjectChildren extends Keys<LocalObjectLight> {

    public ObjectChildren(LocalObjectLight[] _lols){
        setKeys(_lols);
    }

    /*
     * Este constructor sirve para representar los hijos que serán solicitados
     * por demanda, por eso las keys son vacías
     */
    public ObjectChildren(){
        setKeys(new LocalObjectLight[0]);
    }

    @Override
    protected Node[] createNodes(LocalObjectLight t) {
        return new Node[]{new ObjectNode(t)};
    }

    /*
     * Genera los hijos por demanda
     */
    @Override
    public void addNotify(){
        super.addNotify();
        //El root del árbol es un AbstractNode, no un ObjectNode
        if (this.getNode() instanceof ObjectNode){
            LocalObjectLight node = ((ObjectNode)this.getNode()).getObject();
            LocalObjectLight[] children = CommunicationsStub.getInstance().getObjectChildren(node.getOid(),node.getClassName());
            this.setKeys(children);
            this.refresh();
        }
    }

    public void setNewKeys(LocalObjectLight[] objs){
        super.setKeys(objs);
    }
}
