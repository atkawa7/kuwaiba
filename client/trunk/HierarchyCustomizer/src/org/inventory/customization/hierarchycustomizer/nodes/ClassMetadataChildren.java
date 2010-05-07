package org.inventory.customization.hierarchycustomizer.nodes;

import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.interfaces.LocalClassMetadata;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
/**
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class ClassMetadataChildren extends Children.Keys {

    public ClassMetadataChildren(LocalClassMetadata[] lcm){
        setKeys(lcm);
    }

    public ClassMetadataChildren(){
        setKeys(new LocalClassMetadata[0]);
    }

    @Override
    protected Node[] createNodes(Object t) {
        /*if(isMain)
        return new ClassMetadataNode(lcm,this);
        else{
        LocalClassMetadata [] possibleChildren =
        new LocalClassMetadata[t.getPossibleChildren().length];
        int i=0;
        for (String str: t.getPossibleChildren()){
        possibleChildren[i] = (LocalClassMetadata)Cache.getInstace().getMetaForClass(str);
        i++;
        }
        return new ClassMetadataNode(possibleChildren);
        }*/
        if (t instanceof LocalClassMetadata)
            return new Node[] {new ClassMetadataNode((LocalClassMetadata)t)};
        else
            return new Node[] {new ClassMetadataNode((String)t)};
    }

    @Override
    public void addNotify(){
        if (this.getNode() instanceof ClassMetadataNode){ //Ignores the root node
            LocalClassMetadata lcm = ((ClassMetadataNode)this.getNode()).getObject();
            List children = CommunicationsStub.getInstance().getPossibleChildren(
                    lcm.getPackageName()+"."+lcm.getClassName());
            setKeys(children);
            this.refresh();
            super.addNotify();
        }
    }
}