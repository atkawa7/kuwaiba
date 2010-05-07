/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.inventory.customization.attributecustomizer.nodes;

import javax.swing.Action;
import org.inventory.core.services.interfaces.LocalClassMetadata;
import org.inventory.customization.attributecustomizer.actions.NewAttributeAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Sheet;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class ClassMetadataNode extends AbstractNode {
   static final String ICON_PATH = "org/inventory/customization/attributecustomizer/res/flag-green.png";
   private LocalClassMetadata lcm;

   public ClassMetadataNode(LocalClassMetadata lcm){
      super (new AttributeMetadataChildren(lcm.getAttributes()),Lookups.singleton(lcm));
      setIconBaseWithExtension(ICON_PATH);
      this.lcm = lcm;
   }

   @Override
   public String getDisplayName(){
     return lcm.getClassName();
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
    public Action[] getActions (boolean popup){
        return new Action[]{new NewAttributeAction(lcm)};
    }
}