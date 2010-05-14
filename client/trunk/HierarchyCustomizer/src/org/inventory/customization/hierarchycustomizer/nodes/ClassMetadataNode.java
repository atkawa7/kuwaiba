package org.inventory.customization.hierarchycustomizer.nodes;

import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.Arrays;
import javax.swing.Action;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.interfaces.LocalClassMetadataLight;
import org.inventory.core.services.interfaces.NotificationUtil;
import org.inventory.customization.hierarchycustomizer.actions.Delete;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.Lookups;

/**
 * A node representing a ClassMetadataLight
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class ClassMetadataNode extends AbstractNode {
   static final String PARENT_ICON_PATH = "org/inventory/customization/hierarchycustomizer/res/flag-green.png";
   static final String CHILDREN_ICON_PATH = "org/inventory/customization/hierarchycustomizer/res/flag-black.png";
   private LocalClassMetadataLight object;
   private String displayName;
   
   public ClassMetadataNode(LocalClassMetadataLight _lcm, boolean isMain){
      super (new ClassMetadataChildren(),Lookups.singleton(_lcm));
      setIconBaseWithExtension(PARENT_ICON_PATH);
      this.object = _lcm;
   }

   // TODO: I hate this!! please find the right way to create the node as a LEAF
   // withouth duplicate the code, using that joker parameter (isMain)
   public ClassMetadataNode(LocalClassMetadataLight _lcm){
      super (Children.LEAF,Lookups.singleton(_lcm));
      setIconBaseWithExtension(CHILDREN_ICON_PATH);
      this.object = _lcm;
   }

   public ClassMetadataNode(String _name){
       super (Children.LEAF);
       this.displayName = _name;
       setIconBaseWithExtension(CHILDREN_ICON_PATH);
   }

    public LocalClassMetadataLight getObject() {
        return object;
    }

   @Override
   public String getDisplayName(){
       if (object!=null)
            return object.getClassName();
       else
           return displayName;
   }

    @Override
   public Action[] getActions(boolean context){
        if(this.isLeaf()) //return actions only for the nodes representing possible children
            return new Action[]{new Delete(this)};
        else
            return new Action[0];
   }

   //Este método expone las propiedades del nodo, de tal manera que pueda ser mapeadas por el
   //treetable para las columnas
   @Override
   protected Sheet createSheet() {
        Sheet s = super.createSheet();
        Sheet.Set ss = s.get(Sheet.PROPERTIES);
        if (ss == null) {
            ss = Sheet.createPropertiesSet();
            s.put(ss);
        }
        return s;
   }


   @Override
   public PasteType getDropType(final Transferable obj, int action, int index){
            return new PasteType() {
                @Override
                public Transferable paste() throws IOException {
                    try {
                        String[] tokens = ((String)obj.getTransferData(
                                obj.getTransferDataFlavors()[3])).split("\n");


                        if (CommunicationsStub.getInstance().addPossibleChildren(object.getId(),
                                Arrays.asList(tokens))){
                            for (String token : tokens)
                                getChildren().add(new Node[]{new ClassMetadataNode(token)});
                        }
                        else
                            System.out.println("Problema modificando la jerarquía: "+CommunicationsStub.getInstance().getError());
                    }catch (Exception ex) {
                            NotificationUtil nu = Lookup.getDefault().
                                lookup(NotificationUtil.class);
                            nu.showSimplePopup("Error en Modificación de continencia",
                                    NotificationUtil.ERROR,ex.getMessage());
                    }
                    return null;
                }
            };
   }
}