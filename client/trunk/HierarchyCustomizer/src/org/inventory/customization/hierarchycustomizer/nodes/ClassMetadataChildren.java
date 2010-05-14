package org.inventory.customization.hierarchycustomizer.nodes;

import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.interfaces.LocalClassMetadataLight;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
/**
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class ClassMetadataChildren extends Children.Keys {

    private boolean main;

    public ClassMetadataChildren(LocalClassMetadataLight[] lcm){
        this.main = true;
        setKeys(lcm);
    }

    public ClassMetadataChildren(){
        this.main = false;
        setKeys(new LocalClassMetadataLight[0]);
    }

    @Override
    protected Node[] createNodes(Object t) {
        if (t instanceof LocalClassMetadataLight){
            if (main) // I hate this!! please find the right way to create the node as a LEAF
                return new Node[] {new ClassMetadataNode((LocalClassMetadataLight)t,main)};
            else
                return new Node[] {new ClassMetadataNode((LocalClassMetadataLight)t)};
        }
        else
            return new Node[] {new ClassMetadataNode((String)t)};
    }

    @Override
    public void addNotify(){
        if (this.getNode() instanceof ClassMetadataNode){ //Ignores the root node
            LocalClassMetadataLight lcm = ((ClassMetadataNode)this.getNode()).getObject();
            List children = CommunicationsStub.getInstance().getPossibleChildren(
                    lcm.getPackageName()+"."+lcm.getClassName());
            setKeys(children);
            this.refresh();
            super.addNotify();
        }
    }
}