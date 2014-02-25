/*
 *  Copyright 2010-2014 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.inventory.customization.hierarchycustomizer.nodes;

import java.awt.datatransfer.Transferable;
import java.io.IOException;
import javax.swing.Action;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.customization.hierarchycustomizer.actions.Remove;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.Lookups;

/**
 * A node wrapping a ClassMetadataLight
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ClassMetadataNode extends AbstractNode {
   static final String PARENT_ICON_PATH = "org/inventory/customization/hierarchycustomizer/res/flag-green.png";
   static final String ROOT_PARENT_ICON_PATH = "org/inventory/customization/hierarchycustomizer/res/flag-red.png";
   static final String CHILDREN_ICON_PATH = "org/inventory/customization/hierarchycustomizer/res/flag-black.png";
   private LocalClassMetadataLight object;
   
   
   public ClassMetadataNode(LocalClassMetadataLight lcm, boolean isMain){
      super (new ClassMetadataChildren(),Lookups.singleton(lcm));
      if (lcm.getClassName() == null)
          setIconBaseWithExtension(ROOT_PARENT_ICON_PATH);
      else
        setIconBaseWithExtension(PARENT_ICON_PATH);
      this.object = lcm;
   }

   public ClassMetadataNode(LocalClassMetadataLight lcm){
      super (Children.LEAF,Lookups.singleton(lcm));
      setIconBaseWithExtension(CHILDREN_ICON_PATH);
      this.object = lcm;
   }

    public LocalClassMetadataLight getObject() {
        return object;
    }

   @Override
   public String getDisplayName(){
       if (object!=null){
            if (object.getClassName() == null)
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
            Remove deleteAction;
            deleteAction = new Remove(this);
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

                    long[] tokens = new long[]{data.getOid()};

                    //This is supposed to support multiple object drags,
                    //but since I can't make it work, It'll be commented out
//                        if (CommunicationsStub.getInstance().addPossibleChildren(object.getId(),
//                                data)){
//                            for (Object obj : data)
//                                getChildren().add(new Node[]{new ClassMetadataNode((LocalClassMetadataLight)data)});
                    if (CommunicationsStub.getInstance().addPossibleChildren(object.getOid(),
                              tokens)){

                        ((ClassMetadataChildren)getChildren()).add(new ClassMetadataNode[]{new ClassMetadataNode(data)});
                        CommunicationsStub.getInstance().refreshCache(false, false, false, true);

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