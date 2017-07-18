/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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

import java.awt.Image;
import javax.swing.Action;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalObjectListItem;
import org.inventory.customization.listmanager.nodes.actions.DeleteListTypeAction;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.inventory.navigation.navigationtree.nodes.actions.EditObjectAction;
import org.openide.util.ImageUtilities;

/**
 * Represents a single element within the list as a node
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ListTypeItemNode extends ObjectNode {

    private static Image icon = ImageUtilities.loadImage("org/inventory/customization/listmanager/res/list-type-item.png");

    public ListTypeItemNode(LocalObjectListItem lol) {
        super(lol, true);
    }
    
    public ListTypeItemNode(LocalObjectLight lol) {
        super(lol, true);
    }

    @Override
    public Action[] getActions(boolean context){
        return new Action[] { 
            EditObjectAction.getInstance(this), 
            new DeleteListTypeAction(this)};
    }

    @Override
    public void setName(String newName) {
        super.setName(newName);
        //Refresh the cache
        CommunicationsStub.getInstance().getList(getObject().getClassName(), true, true);
    }
    
    @Override
    public Image getIcon(int i){
        return icon;
    }
    
    @Override
    public Image getOpenedIcon(int i){
        return getIcon(i);
    }
}