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

package org.inventory.customization.listmanager.nodes;

import javax.swing.Action;
import org.inventory.core.services.interfaces.LocalClassMetadataLight;
import org.inventory.customization.listmanager.actions.Create;
import org.openide.nodes.AbstractNode;

/**
 * Node representing a list type class
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class ListTypeNode extends AbstractNode{
    private LocalClassMetadataLight object;
    public static final String ICON_PATH="org/inventory/customization/listmanager/res/list-type.png";

    public ListTypeNode(LocalClassMetadataLight lcm) {
        super(new ListElementChildren());
        this.object = lcm;
        setIconBaseWithExtension(ICON_PATH);
    }

    public LocalClassMetadataLight getObject() {
        return object;
    }

    @Override
    public String getDisplayName(){
        return object.getClassName();
    }

    @Override
    public Action[] getActions(boolean context){
        return new Action[]{new Create(this)};
    }
}
