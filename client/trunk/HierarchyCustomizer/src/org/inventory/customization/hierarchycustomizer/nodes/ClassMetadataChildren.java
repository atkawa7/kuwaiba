package org.inventory.customization.hierarchycustomizer.nodes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.interfaces.LocalClassMetadataLight;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
/**
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class ClassMetadataChildren extends Children.Keys implements PropertyChangeListener{

    private boolean main;
    List<LocalClassMetadataLight> keys;

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
        if (main){ // I hate this!! please find the right way to create the node as a LEAF
            ClassMetadataNode cm = new ClassMetadataNode((LocalClassMetadataLight)t,main);
            cm.addPropertyChangeListener((ClassMetadataChildren)cm.getChildren());
            return new Node[] {cm};
        }
        else{
            return new Node[] {new ClassMetadataNode((LocalClassMetadataLight)t)};
        }
    }

    @Override
    public void addNotify(){
        if (this.getNode() instanceof ClassMetadataNode){ //Ignores the root node
            LocalClassMetadataLight lcm = ((ClassMetadataNode)this.getNode()).getObject();
            List children = CommunicationsStub.getInstance().getPossibleChildren(
                    lcm.getPackageName()+"."+lcm.getClassName());
            setKeys(children);
            
            keys = new ArrayList<LocalClassMetadataLight>();
            keys.addAll(children);
        
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getOldValue().equals("add"))
            keys.add((LocalClassMetadataLight)evt.getNewValue());
        else
            keys.remove((LocalClassMetadataLight)evt.getNewValue());
        
        setKeys(keys);
    }

    public List<LocalClassMetadataLight> getCurrentKeys(){
        return this.keys;
    }

    public void setCurrentKeys(List<LocalClassMetadataLight> _keys){
        this.keys = _keys;
    }
}