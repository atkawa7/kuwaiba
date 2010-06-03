/*
 *  Copyright 2010 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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
 */
package org.inventory.customization.attributecustomizer.nodes;

import javax.swing.Action;
import org.inventory.core.services.interfaces.LocalClassMetadataLight;
import org.inventory.customization.attributecustomizer.actions.NewAttributeAction;
import org.openide.nodes.AbstractNode;

/**
 * A node to represent an class
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class ClassMetadataNode extends AbstractNode {
   static final String ICON_PATH = "org/inventory/customization/attributecustomizer/res/flag-green.png";
   private LocalClassMetadataLight object;

   public ClassMetadataNode(LocalClassMetadataLight _lcml){
      super (new AttributeMetadataChildren(_lcml));
      setIconBaseWithExtension(ICON_PATH);
      this.object = _lcml;
   }

   @Override
   public String getDisplayName(){
     return object.getClassName();
   }

    @Override
    public Action[] getActions (boolean popup){
        return new Action[]{new NewAttributeAction(this)};
    }

    public LocalClassMetadataLight getObject(){
        return object;
    }
}