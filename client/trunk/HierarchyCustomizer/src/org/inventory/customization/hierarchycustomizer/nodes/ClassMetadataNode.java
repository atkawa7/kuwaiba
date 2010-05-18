package org.inventory.customization.hierarchycustomizer.nodes;

import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.Action;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.interfaces.LocalClassMetadataLight;
import org.inventory.core.services.interfaces.NotificationUtil;
import org.inventory.customization.hierarchycustomizer.actions.Delete;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.Lookups;

/**
 * A node wrapping a ClassMetadataLight
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class ClassMetadataNode extends AbstractNode {
   static final String PARENT_ICON_PATH = "org/inventory/customization/hierarchycustomizer/res/flag-green.png";
   static final String ROOT_PARENT_ICON_PATH = "org/inventory/customization/hierarchycustomizer/res/flag-red.png";
   static final String CHILDREN_ICON_PATH = "org/inventory/customization/hierarchycustomizer/res/flag-black.png";
   private LocalClassMetadataLight object;
   
   
   public ClassMetadataNode(LocalClassMetadataLight _lcm, boolean isMain){
      super (new ClassMetadataChildren(),Lookups.singleton(_lcm));
      if (_lcm.getClassName().equals(CommunicationsStub.getInstance().getRootClass()))
          setIconBaseWithExtension(ROOT_PARENT_ICON_PATH);
      else
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

    public LocalClassMetadataLight getObject() {
        return object;
    }

   @Override
   public String getDisplayName(){
       if (object!=null){
            if (object.getClassName().equals(CommunicationsStub.getInstance().getRootClass()))
                return java.util.ResourceBundle.getBundle("org/inventory/customization/hierarchycustomizer/Bundle").getString("LBL_ROOTNODE_TEXT");
            else
                return object.getClassName();
                
       }
       else
           return java.util.ResourceBundle.getBundle("org/inventory/customization/hierarchycustomizer/Bundle").getString("LBL_NONAME");
   }

    @Override
   public Action[] getActions(boolean context){
        if(this.isLeaf()){ //return actions only for the nodes representing possible children
            Delete deleteAction;
            deleteAction = new Delete(this);
            deleteAction.addPropertyChangeListener((ClassMetadataChildren)this.getParentNode().getChildren());
            return new Action[]{deleteAction};
        }
        else
            return new Action[0];
   }

   @Override
   public PasteType getDropType(final Transferable obj, int action, int index){
            return new PasteType() {
                @Override
                public Transferable paste() throws IOException {
                    //Only can be dropped into a parent node (the ones marked with a green flag)
                    if (isLeaf())
                        return null;
                    NotificationUtil nu = Lookup.getDefault().
                                lookup(NotificationUtil.class);
                    try {
                        LocalClassMetadataLight data = (LocalClassMetadataLight)obj.getTransferData(
                                LocalClassMetadataLight.DATA_FLAVOR);

                        ArrayList<Long> tokens = new ArrayList<Long>();
                        tokens.add(data.getId());

                        //This is supposed to support multiple object drags,
                        //but as long as I can't make it work, It'll be commented out
//                        if (CommunicationsStub.getInstance().addPossibleChildren(object.getId(),
//                                data)){
//                            for (Object obj : data)
//                                getChildren().add(new Node[]{new ClassMetadataNode((LocalClassMetadataLight)data)});
                        if (CommunicationsStub.getInstance().addPossibleChildren(object.getId(),
                                  tokens)){

                            //This raises a IllegalStateException that can be ignore, since is a warning related to 
                            //firing a change event about a property that doesn't belong to the object
                            //fixes on this are welcome
                             firePropertyChange(PROP_PARENT_NODE, "add", data);

                             nu.showSimplePopup(java.util.ResourceBundle.getBundle("org/inventory/customization/hierarchycustomizer/Bundle").getString("LBL_HIERARCHY_UPDATE_TITLE"),
                                    NotificationUtil.INFO,java.util.ResourceBundle.getBundle("org/inventory/customization/hierarchycustomizer/Bundle").getString("LBL_HIERARCHY_UPDATE_TEXT"));
                        }
                        else
                            nu.showSimplePopup(java.util.ResourceBundle.getBundle("org/inventory/customization/hierarchycustomizer/Bundle").getString("LBL_HIERARCHY_UPDATE_TITLE"),
                                    NotificationUtil.ERROR,CommunicationsStub.getInstance().getError());
                    }catch (Exception ex) {
                            nu.showSimplePopup(java.util.ResourceBundle.getBundle("org/inventory/customization/hierarchycustomizer/Bundle").getString("LBL_HIERARCHY_UPDATE_TITLE"),
                                    NotificationUtil.ERROR,ex.getMessage());
                    }
                    return null;
                }
            };
   }
}